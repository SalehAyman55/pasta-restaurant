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
import pasta.util.Validator;

public final class SignUpPanel extends BackGround implements Screen {

    private final Nav nav;
    private final JTextField username = Theme.field(20);
    private final JTextField phone = Theme.field(20);
    private final JTextField address = Theme.field(20);
    private final JPasswordField password = Theme.password(20);
    private final JPasswordField confirm = Theme.password(20);
    private final JCheckBox show = Theme.check("Show passwords");
    private final JLabel strength = Theme.label(" ", 14, Theme.MUTED);
    private final JLabel error = Theme.label(" ", 14, Theme.DANGER);
    private final RoundedButton signup = Theme.button("SIGN UP");
    private final char defaultEcho = password.getEchoChar();

    public SignUpPanel(Nav nav) {
        this.nav = nav;
        Card card = new Card(new GridBagLayout());
        JLabel title = Theme.title("SIGN UP");
        signup.setFont(Theme.font(Font.BOLD, 20));
        signup.setPreferredSize(new Dimension(300, 48));
        RoundedButton back = Theme.button("BACK", new Color(70, 70, 70), Color.WHITE);
        signup.addActionListener(e -> submit());
        back.addActionListener(e -> nav.go(Nav.WELCOME));
        show.addActionListener(e -> {
            char echo = show.isSelected() ? (char) 0 : defaultEcho;
            password.setEchoChar(echo);
            confirm.setEchoChar(echo);
        });
        Theme.onChange(password, () -> updateStrength());
        card.add(title, Theme.gbc(0, 0, 2));
        field(card, 0, 1, "User Name", username);
        field(card, 0, 3, "Phone Number", phone);
        field(card, 0, 5, "Address", address);
        field(card, 1, 1, "Password", password);
        card.add(strength, Theme.gbc(1, 4, 1));
        field(card, 1, 5, "Confirm Password", confirm);
        card.add(show, Theme.gbc(1, 7, 1));
        card.add(error, Theme.gbc(0, 8, 2));
        GridBagConstraints action = Theme.gbc(0, 9, 2);
        action.fill = GridBagConstraints.NONE;
        action.anchor = GridBagConstraints.CENTER;
        action.insets = new Insets(8, 6, 4, 6);
        card.add(signup, action);
        GridBagConstraints backConstraints = Theme.gbc(0, 10, 2);
        backConstraints.fill = GridBagConstraints.NONE;
        backConstraints.anchor = GridBagConstraints.CENTER;
        card.add(back, backConstraints);
        add(card);
    }

    private static void field(Card card, int col, int row, String label, java.awt.Component input) {
        card.add(Theme.label(label, 18, Theme.GOLD), Theme.gbc(col, row, 1));
        card.add(input, Theme.gbc(col, row + 1, 1));
    }

    private void updateStrength() {
        char[] chars = password.getPassword();
        String label = Validator.passwordLabel(chars);
        strength.setText(label);
        strength.setForeground(switch (label) {
            case "Weak" -> Theme.DANGER;
            case "Medium" -> Theme.GOLD;
            case "Strong" -> Theme.OK;
            default -> Theme.MUTED;
        });
        Arrays.fill(chars, '\0');
    }

    private void submit() {
        char[] pw = password.getPassword();
        char[] cf = confirm.getPassword();
        try {
            if (!Arrays.equals(pw, cf)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            User user = AuthService.register(username.getText(), pw, phone.getText(), address.getText());
            Session.set(user);
            reset();
            nav.go(Nav.DASHBOARD);
        } catch (IllegalArgumentException ex) {
            error.setText(ex.getMessage());
        } finally {
            Arrays.fill(pw, '\0');
            Arrays.fill(cf, '\0');
        }
    }

    private void reset() {
        username.setText("");
        phone.setText("");
        address.setText("");
        password.setText("");
        confirm.setText("");
        error.setText(" ");
        show.setSelected(false);
        password.setEchoChar(defaultEcho);
        confirm.setEchoChar(defaultEcho);
    }

    @Override
    public void onShow() {
        reset();
        SwingUtilities.getRootPane(this).setDefaultButton(signup);
        username.requestFocusInWindow();
    }
}
