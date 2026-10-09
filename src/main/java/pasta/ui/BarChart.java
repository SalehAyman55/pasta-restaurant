package pasta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

public final class BarChart extends JComponent {

    private Map<String, Integer> data = new LinkedHashMap<>();

    public BarChart() {
        setPreferredSize(new Dimension(340, 220));
    }

    public void setData(Map<String, Integer> values) {
        data = new LinkedHashMap<>(values);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Theme.PANEL);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.setFont(Theme.font(Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        if (data.isEmpty()) {
            g2.setColor(Theme.MUTED);
            String text = "No sales data";
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }
        int max = 1;
        for (int v : data.values()) {
            max = Math.max(max, v);
        }
        int pad = 14;
        int labelWidth = 104;
        int valueWidth = 36;
        int rows = data.size();
        int rowHeight = Math.min(38, (getHeight() - pad * 2) / rows);
        int barX = pad + labelWidth;
        int barMax = getWidth() - barX - valueWidth - pad;
        int y = pad;
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            g2.setColor(Color.WHITE);
            g2.drawString(ellipsize(e.getKey(), fm, labelWidth - 8), pad, y + rowHeight / 2 + fm.getAscent() / 2 - 2);
            int w = Math.max(4, (int) ((long) barMax * e.getValue() / max));
            g2.setColor(Theme.GOLD);
            g2.fillRoundRect(barX, y + 6, w, rowHeight - 12, 8, 8);
            g2.setColor(Theme.MUTED);
            g2.drawString(String.valueOf(e.getValue()), barX + w + 6, y + rowHeight / 2 + fm.getAscent() / 2 - 2);
            y += rowHeight;
        }
        g2.dispose();
    }

    private static String ellipsize(String text, FontMetrics fm, int width) {
        if (fm.stringWidth(text) <= width) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && fm.stringWidth(cut + "...") > width) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }
}
