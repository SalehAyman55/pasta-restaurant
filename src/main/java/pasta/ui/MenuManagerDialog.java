package pasta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import pasta.model.Dish;
import pasta.service.MenuService;
import pasta.service.Session;
import pasta.util.Money;

public final class MenuManagerDialog extends JDialog {

    private final DefaultTableModel model = new DefaultTableModel(new String[] {"Category", "Name", "Price (EGP)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextField name = Theme.field(16);
    private final JTextField price = Theme.field(8);
    private List<Dish> items = new ArrayList<>();

    public MenuManagerDialog(Window owner) {
        super(owner, "Manage Menu", ModalityType.APPLICATION_MODAL);
        Theme.styleTable(table);
        Theme.alignRight(table, 2);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillForm();
            }
        });
        category.setEditable(true);
        Theme.styleCombo(category);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.add(Theme.label("Category", 15, Color.WHITE), cell(0, 0));
        form.add(category, cell(0, 1));
        form.add(Theme.label("Name", 15, Color.WHITE), cell(1, 0));
        form.add(name, cell(1, 1));
        form.add(Theme.label("Price", 15, Color.WHITE), cell(2, 0));
        form.add(price, cell(2, 1));
        RoundedButton add = Theme.button("ADD");
        RoundedButton update = Theme.button("UPDATE");
        RoundedButton delete = Theme.button("DELETE", Theme.DANGER, Color.WHITE);
        RoundedButton close = Theme.button("CLOSE", new Color(70, 70, 70), Color.WHITE);
        add.addActionListener(e -> add());
        update.addActionListener(e -> update());
        delete.addActionListener(e -> delete());
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(close);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(form, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.add(Theme.scroll(table), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        setPreferredSize(new Dimension(760, 560));
        reload();
        pack();
        setLocationRelativeTo(owner);
    }

    private static GridBagConstraints cell(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = x == 1 ? 1 : 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 4, 2, 4);
        return c;
    }

    private void reload() {
        items = MenuService.all();
        model.setRowCount(0);
        for (Dish d : items) {
            model.addRow(new Object[] {d.category(), d.name(), Money.plain(d.price())});
        }
        category.removeAllItems();
        for (String c : MenuService.categories()) {
            category.addItem(c);
        }
        name.setText("");
        price.setText("");
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= items.size()) {
            return;
        }
        Dish d = items.get(row);
        category.setSelectedItem(d.category());
        name.setText(d.name());
        price.setText(Money.plain(d.price()));
    }

    private Object[] read() {
        String cat = String.valueOf(category.getEditor().getItem()).trim();
        String dish = name.getText().trim();
        if (cat.length() < 2 || cat.length() > 30) {
            warn("Category must be 2-30 characters.");
            return null;
        }
        if (dish.length() < 2 || dish.length() > 40) {
            warn("Name must be 2-40 characters.");
            return null;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(price.getText().trim());
        } catch (NumberFormatException ex) {
            warn("Enter a valid price.");
            return null;
        }
        if (value.signum() <= 0 || value.compareTo(new BigDecimal("100000")) > 0) {
            warn("Price must be between 0.01 and 100000.");
            return null;
        }
        return new Object[] {cat, dish, Money.round(value)};
    }

    private void add() {
        if (!Session.isAdmin()) {
            return;
        }
        Object[] v = read();
        if (v != null) {
            MenuService.add((String) v[0], (String) v[1], (BigDecimal) v[2]);
            reload();
        }
    }

    private void update() {
        int row = table.getSelectedRow();
        if (!Session.isAdmin() || row < 0) {
            warn("Select an item first.");
            return;
        }
        Object[] v = read();
        if (v != null) {
            MenuService.update(items.get(row).id(), (String) v[0], (String) v[1], (BigDecimal) v[2]);
            reload();
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (!Session.isAdmin() || row < 0) {
            warn("Select an item first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + items.get(row).name() + "?", "Delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            MenuService.delete(items.get(row).id());
            reload();
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Menu", JOptionPane.WARNING_MESSAGE);
    }
}
