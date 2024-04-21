import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class GUI {
    private JFrame frame;
    private JTextField saatjavali;
    private JTextField sonumivali;
    private JTextField saajavali;
    private JButton saadanupp;
    private JButton failinupp;
    private File valitudfail;//fail mida hakata saatma

    public GUI() {
        frame = new JFrame("Sõnumi rakendus");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JLabel nameLabel = new JLabel("Name:");
        saatjavali = new JTextField();
        panel.add(nameLabel);
        panel.add(saatjavali);

        JLabel sonumLabel = new JLabel("Sõnum:");
        sonumivali = new JTextField();
        panel.add(sonumLabel);
        panel.add(sonumivali);

        JLabel saajaLabel = new JLabel("Saaja:");
        saajavali = new JTextField();
        panel.add(saajaLabel);
        panel.add(saajavali);

        saadanupp = new JButton("Saada");
        saadanupp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        panel.add(saadanupp);

        failinupp = new JButton("Get File");
        failinupp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    valitudfail = fileChooser.getSelectedFile();
                }
            }
        });
        panel.add(failinupp);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String name = saatjavali.getText();
        String message = sonumivali.getText();
        String receiver = saajavali.getText();

        try {
            String[] command;
            if (valitudfail != null) {
                command = new String[]{"writesonum", receiver, message, "sendfile", valitudfail.getAbsolutePath()};
            } else {
                command = new String[]{"writesonum", receiver, message};
            }
            Client.main(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI();
            }
        });
    }
}
