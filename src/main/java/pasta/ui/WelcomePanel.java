package pasta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public final class WelcomePanel extends BackGround {

    public WelcomePanel(Nav nav) {
        Card card = new Card(new GridBagLayout());
        JLabel title = Theme.title("PASTA RESTAURANT");
        title.setFont(Theme.font(Font.BOLD, 52));
        JLabel subtitle = Theme.text("Fresh Italian flavors, served fast", 18, Theme.MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        RoundedButton login = big("LOG IN", Theme.GOLD, Color.BLACK);
        RoundedButton signup = big("SIGN UP", Theme.GOLD, Color.BLACK);
        RoundedButton exit = big("EXIT", new Color(70, 70, 70), Color.WHITE);
        login.addActionListener(e -> nav.go(Nav.LOGIN));
        signup.addActionListener(e -> nav.go(Nav.SIGNUP));
        exit.addActionListener(e -> System.exit(0));
        card.add(title, Theme.gbc(0, 0, 1));
        card.add(subtitle, Theme.gbc(0, 1, 1));
        card.add(login, spaced(2, 28));
        card.add(signup, spaced(3, 12));
        card.add(exit, spaced(4, 12));
        add(card);
    }

    private static RoundedButton big(String text, Color fill, Color fg) {
        RoundedButton button = Theme.button(text, fill, fg);
        button.setFont(Theme.font(Font.BOLD, 22));
        button.setPreferredSize(new Dimension(420, 54));
        return button;
    }

    private static GridBagConstraints spaced(int row, int top) {
        GridBagConstraints c = Theme.gbc(0, row, 1);
        c.insets = new Insets(top, 6, 0, 6);
        return c;
    }
}
