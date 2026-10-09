package pasta.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import pasta.model.Order;
import pasta.service.ReceiptService;

public final class ReceiptDialog extends JDialog {

    private final Order order;
    private final JTextArea area;

    public ReceiptDialog(Window owner, Order order) {
        super(owner, "Receipt " + order.id(), ModalityType.APPLICATION_MODAL);
        this.order = order;
        area = new JTextArea(ReceiptService.format(order));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setBackground(new Color(255, 253, 240));
        area.setForeground(Color.BLACK);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        RoundedButton print = Theme.button("PRINT");
        RoundedButton save = Theme.button("SAVE AS TXT");
        RoundedButton close = Theme.button("CLOSE", new Color(70, 70, 70), Color.WHITE);
        print.addActionListener(e -> print());
        save.addActionListener(e -> save());
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.setBackground(Theme.BG);
        buttons.add(print);
        buttons.add(save);
        buttons.add(close);
        setLayout(new BorderLayout());
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void print() {
        try {
            area.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void save() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("receipt_" + order.id() + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            ReceiptService.save(order, chooser.getSelectedFile().toPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save the receipt: " + ex.getMessage(), "Save", JOptionPane.ERROR_MESSAGE);
        }
    }
}
