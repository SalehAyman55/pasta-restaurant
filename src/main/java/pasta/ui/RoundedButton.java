package pasta.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class RoundedButton extends JButton {

    private static final Color INACTIVE = new Color(70, 70, 70);
    private Color fill = Theme.GOLD;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(Color.BLACK);
        setFont(Theme.font(Font.BOLD, 16));
        setMargin(new Insets(8, 18, 8, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setFill(Color color, Color textColor) {
        fill = color;
        setForeground(textColor);
        repaint();
    }

    public void setActive(boolean active) {
        if (active) {
            setFill(Theme.GOLD, Color.BLACK);
        } else {
            setFill(INACTIVE, Color.WHITE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = fill;
        if (!isEnabled()) {
            color = new Color(90, 90, 90);
        } else if (getModel().isPressed()) {
            color = fill.darker();
        } else if (getModel().isRollover()) {
            color = fill.equals(Theme.GOLD) ? new Color(255, 221, 77) : fill.brighter();
        }
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }
}
