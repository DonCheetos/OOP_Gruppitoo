import javax.swing.*;

public class GUIManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame login = new JFrame("Logi sisse");
            LoginAken loginDialog = new LoginAken(login);
            loginDialog.setVisible(true);

            if (!loginDialog.isLoginOk()) {
                System.err.println("Kasutaja ei eksisteeri või parool on vale");
                System.exit(0);
            }

            JFrame rakendus = new JFrame("Sõnumi rakendus");
            GUI gui = new GUI(rakendus, loginDialog.getKasutajanimi());
            rakendus.setVisible(true);

            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                if (throwable instanceof LogiVälja) {
                    rakendus.setVisible(false);
                    gui.peata();
                    loginDialog.setVisible(true);

                    if (!loginDialog.isLoginOk()) {
                        System.err.println("Kasutaja ei eksisteeri või parool on vale");
                        System.exit(0);
                    }

                    rakendus.getContentPane().removeAll();
                    rakendus.revalidate();
                    new GUI(rakendus, loginDialog.getKasutajanimi());
                    rakendus.setVisible(true);
                } else if (throwable instanceof Sulge) {
                    System.exit(0);
                } else {
                    System.err.println("Uncaught exception on " + thread + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            });
        });
    }
}
