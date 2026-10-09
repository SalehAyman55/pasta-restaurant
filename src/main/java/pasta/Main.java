package pasta;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import pasta.ui.AppFrame;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Unexpected error: " + error.getMessage(), "Pasta Restaurant", JOptionPane.ERROR_MESSAGE)));
        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}
