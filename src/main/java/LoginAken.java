import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Set;


public class LoginAken extends JDialog {
    private final JTextField kasutajaVali;
    private final JPasswordField parooliVali;
    private boolean loginOk = false;

    public LoginAken(JFrame parent) {
        super(parent, true);
        JPanel paneel = new JPanel(new GridLayout(2, 1));

//        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TAVERSAL_KEYS, Set.of(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
//        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Set.of(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));

        JPanel üleminePaneel = new JPanel(new GridLayout(2, 2));
        üleminePaneel.add(new JLabel("Kasutajanimi:"));
        kasutajaVali = new JTextField(20);
        üleminePaneel.add(kasutajaVali);
        üleminePaneel.add(new JLabel("Parool:"));
        parooliVali = new JPasswordField(20);
        üleminePaneel.add(parooliVali);

        kasutajaVali.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                if (kasutajaVali.getText().length() >= 12)
                    evt.consume();
            }

            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    kontrolliSisseLogimist();
            }
        });

        parooliVali.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    kontrolliSisseLogimist();
            }
        });

        JPanel aluminePaneel = new JPanel(new GridLayout(1, 3));

        JButton loginNupp = new JButton("Logi sisse");
        loginNupp.addActionListener(e -> kontrolliSisseLogimist());
        loginNupp.addKeyListener(looEnterKuulaja(this::kontrolliSisseLogimist));
        aluminePaneel.add(loginNupp);

        JButton looKasutajaNupp = new JButton("Loo kasutaja");
        looKasutajaNupp.addActionListener(e -> looKasutaja());
        looKasutajaNupp.addKeyListener(looEnterKuulaja(this::looKasutaja));
        aluminePaneel.add(looKasutajaNupp);

        JButton sulgeNupp = new JButton("Sulge rakendus");
        sulgeNupp.addActionListener(e -> System.exit(0));
        sulgeNupp.addKeyListener(looEnterKuulaja(() -> System.exit(0)));
        aluminePaneel.add(sulgeNupp);

        paneel.add(üleminePaneel);
        paneel.add(aluminePaneel);

        getContentPane().add(paneel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parent);
    }

    public KeyAdapter looEnterKuulaja(Runnable r) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    r.run();
            }
        };
    }

    public void looKasutaja() {
        if (kasutajaVali.getText().isEmpty() || new String(parooliVali.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Palun sisestage kasutajanimi ja parool", "Viga", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kasutajanimi = kasutajaVali.getText();
        String parool = new String(parooliVali.getPassword());

        try {
            int response = ClientOperations.createUser(kasutajanimi, parool);
            if (ResponseCodes.getValue(ResponseCodes.OK) == response) {
                JOptionPane.showMessageDialog(this, "Kasutaja loodud. Nüüd logige sisse.", "Edu", JOptionPane.INFORMATION_MESSAGE);
            } else if (ResponseCodes.getValue(ResponseCodes.USER_TAKEN) == response) {
                JOptionPane.showMessageDialog(this, "Kasutajanimi on võetud, proovige muu nimi", "Viga", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Kasutaja loomine ebaõnnestus", "Viga", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Serveriga ühenduse loomine ebaõnnestus", "Viga", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void kontrolliSisseLogimist() {
        if (kasutajaVali.getText().isEmpty() || new String(parooliVali.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Palun sisestage kasutajanimi ja parool", "Viga", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kasutajanimi = kasutajaVali.getText();
        String parool = new String(parooliVali.getPassword());

        try {
            int response = ClientOperations.checkUser(kasutajanimi, parool);
            if (ResponseCodes.getValue(ResponseCodes.OK) == response) {
                loginOk = true;
                dispose();
            } else if (ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND) == response) {
                JOptionPane.showMessageDialog(this, "Kasutaja ei eksisteeri", "Viga", JOptionPane.ERROR_MESSAGE);
            } else if (ResponseCodes.getValue(ResponseCodes.FALSE_PASSWORD) == response) {
                JOptionPane.showMessageDialog(this, "Vale parool", "Viga", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Serveriga ühenduse loomine ebaõnnestus", "Viga", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getKasutajanimi() {
        return kasutajaVali.getText().trim();
    }

    public boolean isLoginOk() {
        return loginOk;
    }
}