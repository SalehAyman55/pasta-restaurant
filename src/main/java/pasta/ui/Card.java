package pasta.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Card extends JPanel {

    public Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(28, 44, 28, 44));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 195));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
        g2.setColor(Theme.GOLD);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
        g2.dispose();
        super.paintComponent(g);
    }
}
