package pasta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pasta.model.User;
import pasta.service.AuthService;
import pasta.service.Session;

public final class LoginPanel extends BackGround implements Screen {

    private final Nav nav;
    private final JTextField username = Theme.field(22);
    private final JPasswordField password = Theme.password(22);
    private final JCheckBox show = Theme.check("Show password");
    private final JLabel error = Theme.label(" ", 14, Theme.DANGER);
    private final RoundedButton login = Theme.button("LOG IN");
    private final char defaultEcho = password.getEchoChar();

    public LoginPanel(Nav nav) {
        this.nav = nav;
        Card card = new Card(new GridBagLayout());
        JLabel title = Theme.title("LOG IN");
        login.setFont(Theme.font(Font.BOLD, 20));
        login.setPreferredSize(new Dimension(380, 48));
        RoundedButton back = Theme.button("BACK", new Color(70, 70, 70), Color.WHITE);
        RoundedButton create = Theme.button("CREATE ACCOUNT", new Color(70, 70, 70), Color.WHITE);
        login.addActionListener(e -> submit());
        back.addActionListener(e -> nav.go(Nav.WELCOME));
        create.addActionListener(e -> nav.go(Nav.SIGNUP));
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : defaultEcho));
        card.add(title, Theme.gbc(0, 0, 2));
        card.add(Theme.label("User Name", 18, Theme.GOLD), Theme.gbc(0, 1, 2));
        card.add(username, Theme.gbc(0, 2, 2));
        card.add(Theme.label("Password", 18, Theme.GOLD), Theme.gbc(0, 3, 2));
        card.add(password, Theme.gbc(0, 4, 2));
        card.add(show, Theme.gbc(0, 5, 2));
        card.add(error, Theme.gbc(0, 6, 2));
        GridBagConstraints loginConstraints = Theme.gbc(0, 7, 2);
        loginConstraints.insets = new Insets(10, 6, 4, 6);
        card.add(login, loginConstraints);
        GridBagConstraints left = Theme.gbc(0, 8, 1);
        GridBagConstraints right = Theme.gbc(1, 8, 1);
        card.add(back, left);
        card.add(create, right);
        add(card);
    }

    private void submit() {
        char[] chars = password.getPassword();
        try {
            User user = AuthService.login(username.getText(), chars);
            Session.set(user);
            reset();
            nav.go(Nav.DASHBOARD);
        } catch (IllegalArgumentException ex) {
            error.setText(ex.getMessage());
            password.setText("");
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private void reset() {
        username.setText("");
        password.setText("");
        error.setText(" ");
        show.setSelected(false);
        password.setEchoChar(defaultEcho);
    }

    @Override
    public void onShow() {
        reset();
        SwingUtilities.getRootPane(this).setDefaultButton(login);
        username.requestFocusInWindow();
    }
}
