package pasta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import pasta.model.Order;
import pasta.model.OrderLine;
import pasta.service.OrderService;
import pasta.util.Money;

public final class SalesPanel extends JPanel implements Screen {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    private final Nav nav;
    private final JComboBox<String> day = new JComboBox<>();
    private final JComboBox<String> month = new JComboBox<>();
    private final JComboBox<String> year = new JComboBox<>();
    private final JTextField search = Theme.field(14);
    private final DefaultTableModel receiptsModel = new DefaultTableModel(new String[] {"ID", "Customer", "Phone", "Date", "Type", "Payment", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel detailsModel = new DefaultTableModel(new String[] {"Item", "Price", "Qty", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable receipts = new JTable(receiptsModel);
    private final JTable details = new JTable(detailsModel);
    private final JLabel breakdown = Theme.text(" ", 14, Theme.MUTED);
    private final JLabel ordersValue = Theme.label("0", 26, Color.WHITE);
    private final JLabel revenueValue = Theme.label("0.00 EGP", 20, Color.WHITE);
    private final JLabel averageValue = Theme.label("0.00 EGP", 20, Color.WHITE);
    private final JLabel bestValue = Theme.label("-", 20, Color.WHITE);
    private final BarChart chart = new BarChart();
    private List<Order> current = List.of();
    private boolean updating;

    public SalesPanel(Nav nav) {
        this.nav = nav;
        setLayout(new BorderLayout(16, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(14, 20, 16, 20));
        add(top(), BorderLayout.NORTH);
        add(center(), BorderLayout.CENTER);
        add(right(), BorderLayout.EAST);
    }

    private JPanel top() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        RoundedButton back = Theme.button("BACK");
        back.addActionListener(e -> nav.go(Nav.DASHBOARD));
        RoundedButton export = Theme.button("EXPORT CSV");
        export.addActionListener(e -> export());
        JLabel title = Theme.title("SALES");
        title.setFont(Theme.font(Font.BOLD, 34));
        bar.add(back, BorderLayout.WEST);
        bar.add(title, BorderLayout.CENTER);
        bar.add(export, BorderLayout.EAST);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filters.setOpaque(false);
        fillDays();
        fillMonths();
        Theme.styleCombo(day);
        Theme.styleCombo(month);
        Theme.styleCombo(year);
        day.addActionListener(e -> reload());
        month.addActionListener(e -> reload());
        year.addActionListener(e -> reload());
        Theme.onChange(search, this::reload);
        RoundedButton searchButton = Theme.button("SEARCH");
        RoundedButton reset = Theme.button("RESET", new Color(70, 70, 70), Color.WHITE);
        searchButton.addActionListener(e -> reload());
        reset.addActionListener(e -> resetFilters());
        filters.add(Theme.label("Day", 15, Theme.GOLD));
        filters.add(day);
        filters.add(Theme.label("Month", 15, Theme.GOLD));
        filters.add(month);
        filters.add(Theme.label("Year", 15, Theme.GOLD));
        filters.add(year);
        filters.add(Theme.label("Find", 15, Theme.GOLD));
        filters.add(search);
        filters.add(searchButton);
        filters.add(reset);
        panel.add(bar, BorderLayout.NORTH);
        panel.add(filters, BorderLayout.SOUTH);
        return panel;
    }

    private JSplitPane center() {
        Theme.styleTable(receipts);
        Theme.styleTable(details);
        Theme.alignRight(receipts, 6);
        receipts.getColumnModel().getColumn(3).setPreferredWidth(160);
        receipts.getColumnModel().getColumn(0).setPreferredWidth(80);
        Theme.alignRight(details, 1, 2, 3);
        receipts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(receiptsModel);
        sorter.setComparator(6, Comparator.comparing((Object o) -> new BigDecimal(o.toString())));
        receipts.setRowSorter(sorter);
        receipts.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetails();
            }
        });
        RoundedButton view = Theme.button("VIEW RECEIPT");
        view.addActionListener(e -> viewReceipt());
        JPanel detailsBody = new JPanel(new BorderLayout(0, 6));
        detailsBody.setOpaque(false);
        detailsBody.add(Theme.scroll(details), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(10, 0));
        south.setOpaque(false);
        south.add(breakdown, BorderLayout.CENTER);
        south.add(view, BorderLayout.EAST);
        detailsBody.add(south, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                Theme.section("RECEIPTS", Theme.scroll(receipts)),
                Theme.section("RECEIPT DETAILS", detailsBody));
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setOpaque(false);
        split.setBackground(Theme.BG);
        return split;
    }

    private JPanel right() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(360, 0));
        JPanel stats = new JPanel(new GridLayout(2, 2, 10, 10));
        stats.setOpaque(false);
        stats.add(statCard("ORDERS", ordersValue));
        stats.add(statCard("REVENUE", revenueValue));
        stats.add(statCard("AVERAGE", averageValue));
        stats.add(statCard("BEST SELLER", bestValue));
        panel.add(stats, BorderLayout.NORTH);
        panel.add(Theme.section("TOP ITEMS BY QUANTITY", chart), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel statCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.PANEL);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.add(Theme.label(title, 12, Theme.GOLD), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private void fillDays() {
        day.addItem("All");
        for (int i = 1; i <= 31; i++) {
            day.addItem(String.valueOf(i));
        }
    }

    private void fillMonths() {
        month.addItem("All");
        for (Month m : Month.values()) {
            month.addItem(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
    }

    private void fillYears() {
        Object selected = year.getSelectedItem();
        TreeSet<Integer> years = new TreeSet<>(Comparator.reverseOrder());
        years.add(LocalDate.now().getYear());
        for (Order o : OrderService.all()) {
            years.add(o.time().getYear());
        }
        year.removeAllItems();
        year.addItem("All");
        for (int y : years) {
            year.addItem(String.valueOf(y));
        }
        year.setSelectedItem(selected == null ? "All" : selected);
        if (year.getSelectedItem() == null) {
            year.setSelectedIndex(0);
        }
    }

    private void resetFilters() {
        updating = true;
        day.setSelectedIndex(0);
        month.setSelectedIndex(0);
        year.setSelectedIndex(0);
        search.setText("");
        updating = false;
        reload();
    }

    private void reload() {
        if (updating) {
            return;
        }
        Integer d = day.getSelectedIndex() <= 0 ? null : day.getSelectedIndex();
        Integer m = month.getSelectedIndex() <= 0 ? null : month.getSelectedIndex();
        Object selectedYear = year.getSelectedItem();
        Integer y = selectedYear == null || "All".equals(selectedYear) ? null : Integer.valueOf(selectedYear.toString());
        current = OrderService.filter(d, m, y, search.getText());
        receiptsModel.setRowCount(0);
        for (Order o : current) {
            receiptsModel.addRow(new Object[] {o.id(), o.customer(), o.phone(), o.time().format(FORMAT), o.type(), o.payment(), Money.plain(o.total())});
        }
        detailsModel.setRowCount(0);
        breakdown.setText(" ");
        BigDecimal revenue = OrderService.revenue(current);
        int count = current.size();
        BigDecimal average = count == 0 ? Money.zero() : revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        Map<String, Integer> top = OrderService.topItems(current, 5);
        ordersValue.setText(String.valueOf(count));
        revenueValue.setText(Money.format(revenue));
        averageValue.setText(Money.format(average));
        bestValue.setText(top.isEmpty() ? "-" : top.keySet().iterator().next());
        chart.setData(top);
    }

    private Order selected() {
        int row = receipts.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int index = receipts.convertRowIndexToModel(row);
        return index >= 0 && index < current.size() ? current.get(index) : null;
    }

    private void showDetails() {
        detailsModel.setRowCount(0);
        Order order = selected();
        if (order == null) {
            breakdown.setText(" ");
            return;
        }
        for (OrderLine l : order.lines()) {
            detailsModel.addRow(new Object[] {l.name(), Money.plain(l.price()), String.valueOf(l.qty()), Money.plain(l.total())});
        }
        breakdown.setText("Sub " + Money.plain(order.subtotal()) + "  |  Disc " + Money.plain(order.discount()) + "  |  Tax " + Money.plain(order.tax())
                + "  |  Del " + Money.plain(order.delivery()) + "  |  Total " + Money.plain(order.total()));
    }

    private void viewReceipt() {
        Order order = selected();
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Select a receipt first.", "Sales", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new ReceiptDialog(SwingUtilities.getWindowAncestor(this), order).setVisible(true);
    }

    private void export() {
        if (current.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There is nothing to export.", "Sales", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("sales_export.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            OrderService.exportCsv(current, chooser.getSelectedFile().toPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Sales", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onShow() {
        updating = true;
        fillYears();
        updating = false;
        reload();
    }
}
