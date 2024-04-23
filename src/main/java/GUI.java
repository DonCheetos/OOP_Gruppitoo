import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GUI {
    private JFrame frame;
    private JTextField kasutaja;
    private JTextField sonumivali;
    private JTextField saajavali;
    private JButton saadanupp;
    private JButton loesõnumeid;
    private JButton failinupp;
    private JTextArea sõnumiKuva; // tegevuse kuvamiseks

    private File valitudfail; // fail mida hakata saatma

    public GUI() { // TODO: GUI stiili võiks ilusamaks teha(Optional). GUI võiks tuleviku mõttes kasutda ka otse direct/group chat jaoks
        frame = new JFrame("Sõnumi rakendus");
        frame.setSize(800, 600); // Suurendatud raami suurus
        frame.setMinimumSize(new Dimension(400, 300)); // Minimaalne akna suurus

        // Paneeli stiili muutmine
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Lisatud tühjad piirid
        panel.setBackground(Color.lightGray);

        JLabel nameLabel = new JLabel("Kasutaja:");
        Font labelFont = nameLabel.getFont(); // Hangi praegune font. NB! taaskasutan seda teiste fontide muutmise jaoks ka
        nameLabel.setForeground(Color.BLUE);
        int newSize = 16; // Uue suuruse määramine
        nameLabel.setFont(labelFont.deriveFont((float)newSize)); // uus font
        kasutaja = new JTextField();
        panel.add(nameLabel);
        panel.add(kasutaja);

        JLabel sonumLabel = new JLabel("Sõnum:");
        sonumLabel.setForeground(Color.BLUE);
        sonumLabel.setFont(labelFont.deriveFont((float)newSize)); // uus font
        sonumivali = new JTextField();
        panel.add(sonumLabel);
        panel.add(sonumivali);

        JLabel saajaLabel = new JLabel("Saaja:");
        saajaLabel.setForeground(Color.BLUE);
        saajaLabel.setFont(labelFont.deriveFont((float)newSize)); // uus font
        saajavali = new JTextField();
        panel.add(saajaLabel);
        panel.add(saajavali);

        saadanupp = new JButton("Saada");
        saadanupp.setForeground(Color.BLACK); // Muudame nupu teksti värvi mustaks
        saadanupp.setBackground(Color.GREEN.darker());
        saadanupp.setFont(labelFont.deriveFont((float)newSize)); // uus font
        saadanupp.addActionListener(e -> sendMessage());
        panel.add(saadanupp);

        loesõnumeid = new JButton("Loe sõnumeid");
        loesõnumeid.setForeground(Color.BLACK); // Muudame nupu teksti värvi mustaks
        loesõnumeid.setBackground(Color.ORANGE.darker());
        loesõnumeid.setFont(labelFont.deriveFont((float)newSize)); // uus font
        loesõnumeid.addActionListener(e -> getMessage());
        panel.add(loesõnumeid);

        sõnumiKuva = new JTextArea();
        sõnumiKuva.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(sõnumiKuva);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane);

        failinupp = new JButton("Saa fail");
        failinupp.setForeground(Color.BLACK); // Muudame nupu teksti värvi mustaks
        failinupp.setBackground(Color.RED.darker());
        failinupp.setFont(labelFont.deriveFont((float)newSize)); // uus font
        failinupp.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                valitudfail = fileChooser.getSelectedFile();
            }
        });
        panel.add(failinupp);

        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Sulgemisel lõpetab programmi töö

        // Sündmusekuulaja JTextArea suuruse muutmiseks
        sõnumiKuva.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTextSize();
            }
        });

        // Sündmusekuulaja akna suuruse muutmiseks
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateAllTextSizes();
            }
        });
    }

    private void sendMessage() {
        String name = kasutaja.getText();
        if (name.isEmpty()) { // kui kasutajate ei sisestatud
            name = "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedDateTime = now.format(formatter);

        String message = sonumivali.getText();
        String receiver = saajavali.getText();
        String fullSõnum = '(' + formattedDateTime + ") " + name + " : " + message;

        if (!receiver.isEmpty() && !message.isEmpty()) { // receiver ja sõnum peab olema täidetud
            try {
                String[] command;
                if (valitudfail != null) {
                    command = new String[]{"writesonum", receiver, fullSõnum, "sendfile", valitudfail.getAbsolutePath()};
                } else {
                    command = new String[]{"writesonum", receiver, fullSõnum};
                }
                Client.main(command);

                sõnumiKuva.append("Saadetud sõnum: " + '"' + message + '"' + ", saajale: " + "'" + receiver + "'" + "\n");
                sõnumiKuva.setCaretPosition(sõnumiKuva.getDocument().getLength());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else System.err.println("Kasutaja ja sõnumi lahter peab olema täidetud!"); // teade
    }

    private void getMessage() { // sõnumite sisse lugemiseks serverilt
        String receiver = kasutaja.getText();

        try {
            String[] command;
            command = new String[]{"getsonum", receiver};

            Client.main(command); // päringu tegemine

            try {
                List<String> messages = FileUtil.readFromFile(receiver + "_msg.txt"); // salvestab listi kõik saadud sõnumid
                for (String message : messages) {
                    sõnumiKuva.append(message + "\n");
                }
            } catch (IOException e) {
                System.err.println("Sõnumeid polnud");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void updateTextSize() { // uus GUI teksti suurus
        Font currentFont = sõnumiKuva.getFont();
        float newSize = Math.max(sõnumiKuva.getWidth() / 80.0f, 12.0f);
        sõnumiKuva.setFont(currentFont.deriveFont(newSize));
    }

    private void updateAllTextSizes() { // proovisin teha dünaamiliselt muutuva teksti suuruse NB! see ei tööta TODO: Kui pole liiga keeruline võiks ära parandada
        float newSize = Math.max(frame.getWidth() / 80.0f, 12.0f);
        Font newFont = new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(newSize));

        Component[] components = frame.getRootPane().getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                ((JLabel) component).setFont(newFont);
            } else if (component instanceof JTextField) {
                ((JTextField) component).setFont(newFont);
            } else if (component instanceof JTextArea) {
                ((JTextArea) component).setFont(newFont);
            } else if (component instanceof JButton) {
                ((JButton) component).setFont(newFont);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
