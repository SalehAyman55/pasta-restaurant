package pasta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import pasta.model.Order;
import pasta.model.User;
import pasta.service.OrderService;
import pasta.service.Session;
import pasta.util.Money;

public final class DashboardPanel extends BackGround implements Screen {

    private final Nav nav;
    private final JLabel welcome = Theme.label(" ", 20, Color.WHITE);
    private final JLabel today = Theme.text(" ", 16, Theme.MUTED);
    private final RoundedButton sales = big("SALES", Theme.GOLD, Color.BLACK);
    private final RoundedButton menu = big("MANAGE MENU", Theme.GOLD, Color.BLACK);

    public DashboardPanel(Nav nav) {
        this.nav = nav;
        Card card = new Card(new GridBagLayout());
        JLabel title = Theme.title("PASTA RESTAURANT");
        title.setFont(Theme.font(Font.BOLD, 46));
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        today.setHorizontalAlignment(SwingConstants.CENTER);
        RoundedButton order = big("PLACE ORDER", Theme.GOLD, Color.BLACK);
        RoundedButton logout = big("LOG OUT", new Color(70, 70, 70), Color.WHITE);
        order.addActionListener(e -> nav.go(Nav.ORDER));
        sales.addActionListener(e -> nav.go(Nav.SALES));
        menu.addActionListener(e -> new MenuManagerDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        logout.addActionListener(e -> {
            Session.clear();
            nav.go(Nav.WELCOME);
        });
        card.add(title, Theme.gbc(0, 0, 1));
        card.add(welcome, Theme.gbc(0, 1, 1));
        card.add(today, Theme.gbc(0, 2, 1));
        card.add(order, spaced(3, 24));
        card.add(sales, spaced(4, 12));
        card.add(menu, spaced(5, 12));
        card.add(logout, spaced(6, 12));
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

    @Override
    public void onShow() {
        User user = Session.user();
        if (user == null) {
            return;
        }
        welcome.setText("Welcome, " + user.username() + "  |  " + user.role());
        boolean admin = Session.isAdmin();
        sales.setVisible(admin);
        menu.setVisible(admin);
        LocalDate now = LocalDate.now();
        List<Order> todays = OrderService.all().stream().filter(o -> o.time().toLocalDate().equals(now)).toList();
        today.setText("Today: " + todays.size() + " orders  |  " + Money.format(OrderService.revenue(todays)));
    }
}
