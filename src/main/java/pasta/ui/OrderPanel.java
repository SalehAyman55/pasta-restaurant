package pasta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import pasta.model.Dish;
import pasta.model.Order;
import pasta.model.OrderLine;
import pasta.model.Totals;
import pasta.model.User;
import pasta.service.MenuService;
import pasta.service.OrderService;
import pasta.service.PricingService;
import pasta.service.Session;
import pasta.util.Money;
import pasta.util.Validator;

public final class OrderPanel extends JPanel implements Screen {

    private final Nav nav;
    private final JTextField name = Theme.field(14);
    private final JTextField phone = Theme.field(12);
    private final JTextField address = Theme.field(26);
    private final JRadioButton delivery = Theme.radio("Delivery", true);
    private final JRadioButton dineIn = Theme.radio("Dine-in", false);
    private final JRadioButton takeaway = Theme.radio("Takeaway", false);
    private final DefaultTableModel cartModel = new DefaultTableModel(new String[] {"Item", "Price", "Qty", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final List<OrderLine> cart = new ArrayList<>();
    private final JTextField code = Theme.field(10);
    private final JLabel codeStatus = Theme.text(" ", 13, Theme.MUTED);
    private final JLabel subtotal = value(15, Color.WHITE);
    private final JLabel discount = value(15, Color.WHITE);
    private final JLabel tax = value(15, Color.WHITE);
    private final JLabel deliveryFee = value(15, Color.WHITE);
    private final JLabel total = value(24, Theme.GOLD);
    private final JComboBox<String> payment = new JComboBox<>(new String[] {"Cash", "Card", "Mobile Wallet"});
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JPanel categoryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel itemGrid = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JLabel cashier = Theme.label(" ", 16, Theme.MUTED);
    private String appliedCode = "";
    private String selectedCategory;

    public OrderPanel(Nav nav) {
        this.nav = nav;
        setLayout(new BorderLayout(16, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(14, 20, 16, 20));
        add(header(), BorderLayout.NORTH);
        add(west(), BorderLayout.WEST);
        add(menuPane(), BorderLayout.CENTER);
        refreshTotals();
    }

    private static JLabel value(int size, Color color) {
        JLabel label = Theme.label("0.00 EGP", size, color);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        RoundedButton back = Theme.button("BACK");
        back.addActionListener(e -> nav.go(Nav.DASHBOARD));
        JLabel title = Theme.title("NEW ORDER");
        title.setFont(Theme.font(Font.BOLD, 34));
        panel.add(back, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        panel.add(cashier, BorderLayout.EAST);
        return panel;
    }

    private JPanel west() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(520, 0));
        panel.add(customerPanel(), BorderLayout.NORTH);
        panel.add(cartPanel(), BorderLayout.CENTER);
        panel.add(summaryPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel customerPanel() {
        JPanel panel = Theme.gridPanel();
        Theme.addRow(panel, 0, "Name", name);
        Theme.addRow(panel, 1, "Phone", phone);
        Theme.addRow(panel, 2, "Address", address);
        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        types.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton radio : new JRadioButton[] {delivery, dineIn, takeaway}) {
            group.add(radio);
            types.add(radio);
            radio.addActionListener(e -> typeChanged());
        }
        Theme.addRow(panel, 3, "Type", types);
        return panel;
    }

    private JPanel cartPanel() {
        Theme.styleTable(cartTable);
        Theme.alignRight(cartTable, 1, 2, 3);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(190);
        for (int i = 1; i < 4; i++) {
            cartTable.getColumnModel().getColumn(i).setPreferredWidth(70);
        }
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(Theme.scroll(cartTable), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        RoundedButton minus = Theme.button("-");
        RoundedButton plus = Theme.button("+");
        RoundedButton remove = Theme.button("REMOVE");
        RoundedButton clear = Theme.button("CLEAR", new Color(70, 70, 70), Color.WHITE);
        minus.addActionListener(e -> changeQuantity(-1));
        plus.addActionListener(e -> changeQuantity(1));
        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> clearCart());
        buttons.add(minus);
        buttons.add(plus);
        buttons.add(remove);
        buttons.add(clear);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel summaryPanel() {
        JPanel panel = Theme.gridPanel();
        JPanel codeRow = new JPanel(new BorderLayout(8, 0));
        codeRow.setOpaque(false);
        RoundedButton apply = Theme.button("APPLY");
        apply.addActionListener(e -> applyCode());
        code.addActionListener(e -> applyCode());
        codeRow.add(code, BorderLayout.CENTER);
        codeRow.add(apply, BorderLayout.EAST);
        Theme.addRow(panel, 0, "Code", codeRow);
        Theme.addRow(panel, 1, "", codeStatus);
        Theme.styleCombo(payment);
        Theme.addRow(panel, 2, "Payment", payment);
        JPanel totals = new JPanel(new GridLayout(0, 2, 0, 3));
        totals.setOpaque(false);
        totals.setBorder(new EmptyBorder(6, 0, 6, 0));
        totals.add(Theme.text("Subtotal", 15, Theme.MUTED));
        totals.add(subtotal);
        totals.add(Theme.text("Discount", 15, Theme.MUTED));
        totals.add(discount);
        totals.add(Theme.text("Tax 14%", 15, Theme.MUTED));
        totals.add(tax);
        totals.add(Theme.text("Delivery", 15, Theme.MUTED));
        totals.add(deliveryFee);
        totals.add(Theme.label("TOTAL", 24, Color.WHITE));
        totals.add(total);
        GridBagConstraints totalsConstraints = Theme.gbc(0, 3, 2);
        totalsConstraints.insets = new Insets(4, 0, 4, 0);
        panel.add(totals, totalsConstraints);
        RoundedButton save = Theme.button("SAVE ORDER");
        save.setFont(Theme.font(Font.BOLD, 22));
        save.setPreferredSize(new Dimension(0, 52));
        save.addActionListener(e -> submit());
        GridBagConstraints saveConstraints = Theme.gbc(0, 4, 2);
        saveConstraints.insets = new Insets(4, 0, 0, 0);
        panel.add(save, saveConstraints);
        return panel;
    }

    private JPanel menuPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        categoryBar.setOpaque(false);
        itemGrid.setOpaque(false);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(itemGrid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel quantityRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quantityRow.setOpaque(false);
        quantity.setFont(Theme.font(Font.BOLD, 18));
        quantity.setPreferredSize(new Dimension(90, 36));
        quantityRow.add(Theme.label("Quantity per tap", 16, Color.WHITE));
        quantityRow.add(quantity);
        panel.add(categoryBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(quantityRow, BorderLayout.SOUTH);
        return panel;
    }

    private void rebuildMenu() {
        List<String> categories = MenuService.categories();
        if (selectedCategory == null || !categories.contains(selectedCategory)) {
            selectedCategory = categories.isEmpty() ? null : categories.get(0);
        }
        categoryBar.removeAll();
        for (String category : categories) {
            RoundedButton button = Theme.button(category);
            button.setActive(category.equals(selectedCategory));
            button.addActionListener(e -> {
                selectedCategory = category;
                rebuildMenu();
            });
            categoryBar.add(button);
        }
        itemGrid.removeAll();
        if (selectedCategory != null) {
            for (Dish dish : MenuService.byCategory(selectedCategory)) {
                itemGrid.add(itemButton(dish));
            }
        }
        categoryBar.revalidate();
        categoryBar.repaint();
        itemGrid.revalidate();
        itemGrid.repaint();
    }

    private RoundedButton itemButton(Dish dish) {
        String html = "<html><div style='text-align:center'><span style='font-size:16px'>" + Theme.esc(dish.name()) + "</span><br>"
                + Money.format(dish.price()) + "</div></html>";
        RoundedButton button = Theme.button(html);
        button.setPreferredSize(new Dimension(150, 96));
        button.addActionListener(e -> addToCart(dish, (Integer) quantity.getValue()));
        return button;
    }

    private void addToCart(Dish dish, int qty) {
        for (int i = 0; i < cart.size(); i++) {
            OrderLine line = cart.get(i);
            if (line.name().equals(dish.name()) && line.category().equals(dish.category())) {
                cart.set(i, new OrderLine(line.name(), line.category(), line.price(), Math.min(99, line.qty() + qty)));
                refreshCart(i);
                return;
            }
        }
        cart.add(new OrderLine(dish.name(), dish.category(), dish.price(), qty));
        refreshCart(cart.size() - 1);
    }

    private void changeQuantity(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            return;
        }
        OrderLine line = cart.get(row);
        int next = line.qty() + delta;
        if (next <= 0) {
            cart.remove(row);
            refreshCart(Math.min(row, cart.size() - 1));
        } else if (next <= 99) {
            cart.set(row, new OrderLine(line.name(), line.category(), line.price(), next));
            refreshCart(row);
        }
    }

    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cart.size()) {
            cart.remove(row);
            refreshCart(Math.min(row, cart.size() - 1));
        }
    }

    private void clearCart() {
        cart.clear();
        refreshCart(-1);
    }

    private void refreshCart(int select) {
        cartModel.setRowCount(0);
        for (OrderLine line : cart) {
            cartModel.addRow(new Object[] {line.name(), Money.plain(line.price()), String.valueOf(line.qty()), Money.plain(line.total())});
        }
        if (select >= 0 && select < cart.size()) {
            cartTable.setRowSelectionInterval(select, select);
        }
        refreshTotals();
    }

    private String currentType() {
        if (dineIn.isSelected()) {
            return "Dine-in";
        }
        return takeaway.isSelected() ? "Takeaway" : "Delivery";
    }

    private void typeChanged() {
        address.setEnabled(delivery.isSelected());
        refreshTotals();
    }

    private void applyCode() {
        String entered = code.getText().trim();
        if (entered.isEmpty()) {
            appliedCode = "";
            codeStatus.setText(" ");
        } else if (PricingService.validCode(entered)) {
            appliedCode = entered.toUpperCase(Locale.ROOT);
            codeStatus.setForeground(Theme.OK);
            codeStatus.setText(PricingService.percent(entered) + "% off applied");
        } else {
            appliedCode = "";
            codeStatus.setForeground(Theme.DANGER);
            codeStatus.setText("Invalid code");
        }
        refreshTotals();
    }

    private void refreshTotals() {
        Totals t = PricingService.calculate(cart, currentType(), appliedCode);
        subtotal.setText(Money.format(t.subtotal()));
        discount.setText(t.discount().signum() > 0 ? "-" + Money.format(t.discount()) : Money.format(t.discount()));
        tax.setText(Money.format(t.tax()));
        deliveryFee.setText(Money.format(t.delivery()));
        total.setText(Money.format(t.total()));
    }

    private void submit() {
        User user = Session.user();
        if (user == null) {
            nav.go(Nav.LOGIN);
            return;
        }
        String customer = name.getText().trim();
        String normalizedPhone = Validator.normalizePhone(phone.getText());
        String where = address.getText().trim();
        String type = currentType();
        if (cart.isEmpty()) {
            warn("Add at least one item to the order.");
            return;
        }
        if (customer.length() < 2) {
            warn("Enter the customer name.");
            name.requestFocusInWindow();
            return;
        }
        if (!Validator.phone(normalizedPhone)) {
            warn("Enter a valid Egyptian mobile number.");
            phone.requestFocusInWindow();
            return;
        }
        if ("Delivery".equals(type) && where.length() < 5) {
            warn("Enter the delivery address.");
            address.requestFocusInWindow();
            return;
        }
        Order order = OrderService.save(user.username(), customer, normalizedPhone, "Delivery".equals(type) ? where : "", type,
                (String) payment.getSelectedItem(), appliedCode, cart);
        new ReceiptDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);
        resetForm();
    }

    private void resetForm() {
        name.setText("");
        phone.setText("");
        address.setText("");
        code.setText("");
        codeStatus.setText(" ");
        appliedCode = "";
        payment.setSelectedIndex(0);
        quantity.setValue(1);
        clearCart();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Order", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void onShow() {
        User user = Session.user();
        cashier.setText(user == null ? " " : "Cashier: " + user.username());
        rebuildMenu();
        refreshTotals();
    }
}
