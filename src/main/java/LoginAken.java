import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class LoginAken extends JDialog {
    private final JTextField kasutajaVali;
    private final JPasswordField parooliVali;
    private boolean loginOk = false;

    public LoginAken(JFrame parent) {
        super(parent, true);
        JPanel paneel = new JPanel(new GridLayout(2, 1));

        JPanel üleminePaneel = new JPanel(new GridLayout(2, 2));
        üleminePaneel.add(new JLabel("Kasutajanimi:"));
        kasutajaVali = new JTextField(20);
        üleminePaneel.add(kasutajaVali);
        üleminePaneel.add(new JLabel("Parool:"));
        parooliVali = new JPasswordField(20);
        üleminePaneel.add(parooliVali);

        kasutajaVali.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (kasutajaVali.getText().length() >= 12)
                    evt.consume();
            }
        });

        JPanel aluminePaneel = new JPanel(new GridLayout(1, 3));

        JButton loginNupp = new JButton("Logi sisse");
        loginNupp.addActionListener(e -> {
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
        });
        aluminePaneel.add(loginNupp);

        JButton registerNupp = new JButton("Loo kasutaja");
        registerNupp.addActionListener(e -> {
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
        });
        aluminePaneel.add(registerNupp);

        JButton sulgeNupp = new JButton("Sulge rakendus");
        sulgeNupp.addActionListener(e -> System.exit(0));
        aluminePaneel.add(sulgeNupp);

        paneel.add(üleminePaneel);
        paneel.add(aluminePaneel);

        getContentPane().add(paneel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parent);
    }

    public String getKasutajanimi() {
        return kasutajaVali.getText().trim();
    }

    public boolean isLoginOk() {
        return loginOk;
    }
}