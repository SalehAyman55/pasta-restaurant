package pasta.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

public final class Theme {

    public static final Color GOLD = new Color(255, 204, 0);
    public static final Color BG = new Color(18, 18, 18);
    public static final Color PANEL = new Color(38, 38, 38);
    public static final Color GRID = new Color(64, 64, 64);
    public static final Color MUTED = new Color(176, 176, 176);
    public static final Color DANGER = new Color(235, 87, 87);
    public static final Color OK = new Color(88, 200, 124);

    private Theme() {
    }

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, size));
        label.setForeground(color);
        return label;
    }

    public static JLabel text(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    public static JLabel title(String text) {
        JLabel label = label(text, 44, GOLD);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        styleInput(field);
        return field;
    }

    public static JPasswordField password(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInput(field);
        return field;
    }

    private static void styleInput(JTextField field) {
        field.setFont(font(Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(6, 10, 6, 10)));
    }

    public static RoundedButton button(String text) {
        return new RoundedButton(text);
    }

    public static RoundedButton button(String text, Color fill, Color textColor) {
        RoundedButton button = new RoundedButton(text);
        button.setFill(fill, textColor);
        return button;
    }

    public static JCheckBox check(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setOpaque(false);
        box.setForeground(Color.WHITE);
        box.setFont(font(Font.PLAIN, 14));
        box.setFocusPainted(false);
        return box;
    }

    public static JRadioButton radio(String text, boolean selected) {
        JRadioButton radio = new JRadioButton(text, selected);
        radio.setOpaque(false);
        radio.setForeground(Color.WHITE);
        radio.setFont(font(Font.BOLD, 15));
        radio.setFocusPainted(false);
        return radio;
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(font(Font.PLAIN, 15));
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
    }

    public static void styleTable(JTable table) {
        table.setBackground(PANEL);
        table.setForeground(Color.WHITE);
        table.setGridColor(GRID);
        table.setRowHeight(30);
        table.setFont(font(Font.PLAIN, 14));
        table.setSelectionBackground(GOLD);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        JTableHeader header = table.getTableHeader();
        header.setBackground(GOLD);
        header.setForeground(Color.BLACK);
        header.setFont(font(Font.BOLD, 14));
        header.setReorderingAllowed(false);
    }

    public static void alignRight(JTable table, int... columns) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int column : columns) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
    }

    public static JScrollPane scroll(JComponent view) {
        JScrollPane pane = new JScrollPane(view);
        pane.getViewport().setBackground(PANEL);
        pane.setBorder(new LineBorder(GOLD, 1));
        return pane;
    }

    public static JPanel section(String title, Component body) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(label(title, 16, GOLD), java.awt.BorderLayout.NORTH);
        panel.add(body, java.awt.BorderLayout.CENTER);
        return panel;
    }

    public static GridBagConstraints gbc(int x, int y, int width) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 6, 4, 6);
        return c;
    }

    public static void addRow(JPanel panel, int row, String label, Component field) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = row;
        l.anchor = GridBagConstraints.WEST;
        l.insets = new Insets(4, 0, 4, 12);
        panel.add(label(label, 16, Color.WHITE), l);
        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 1;
        f.gridy = row;
        f.weightx = 1;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(4, 0, 4, 0);
        panel.add(field, f);
    }

    public static JPanel gridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    public static void onChange(JTextComponent component, Runnable action) {
        component.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    public static String esc(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
