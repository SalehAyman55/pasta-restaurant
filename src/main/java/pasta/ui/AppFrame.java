package pasta.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import pasta.service.Session;

public final class AppFrame extends JFrame implements Nav {

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final Map<String, JComponent> screens = new HashMap<>();

    public AppFrame() {
        super("Pasta Restaurant");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        register(WELCOME, new WelcomePanel(this));
        register(LOGIN, new LoginPanel(this));
        register(SIGNUP, new SignUpPanel(this));
        register(DASHBOARD, new DashboardPanel(this));
        register(ORDER, new OrderPanel(this));
        register(SALES, new SalesPanel(this));
        setContentPane(root);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        go(WELCOME);
    }

    private void register(String name, JComponent component) {
        screens.put(name, component);
        root.add(component, name);
    }

    @Override
    public void go(String screen) {
        String target = screen;
        boolean open = WELCOME.equals(target) || LOGIN.equals(target) || SIGNUP.equals(target);
        if (!open && Session.user() == null) {
            target = WELCOME;
        }
        if (SALES.equals(target) && !Session.isAdmin()) {
            target = DASHBOARD;
        }
        getRootPane().setDefaultButton(null);
        JComponent component = screens.get(target);
        if (component instanceof Screen s) {
            s.onShow();
        }
        layout.show(root, target);
    }
}
