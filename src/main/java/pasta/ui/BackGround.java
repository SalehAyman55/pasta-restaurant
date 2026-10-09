package pasta.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackGround extends JPanel {

    private final Image background;

    public BackGround() {
        super(new GridBagLayout());
        URL url = BackGround.class.getResource("/image/background.jpeg");
        background = url == null ? null : new ImageIcon(url).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        if (background != null && background.getWidth(null) > 0) {
            double scale = Math.max((double) w / background.getWidth(null), (double) h / background.getHeight(null));
            int dw = (int) Math.ceil(background.getWidth(null) * scale);
            int dh = (int) Math.ceil(background.getHeight(null) * scale);
            g2.drawImage(background, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRect(0, 0, w, h);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(40, 28, 0), w, h, new Color(8, 8, 8)));
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
    }
}
