import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GUI {
    // Deklareerime GUI komponendid
    private JFrame frame;
    private JTextField kasutaja; // Tekstiväli kasutaja nime jaoks
    private JTextField sonumivali; // Tekstiväli sõnumi sisestamiseks
    private JTextField saajavali; // Tekstiväli sõnumi saaja nime jaoks
    private JButton saadanupp; // Nupp sõnumi saatmiseks
    private JButton loesõnumeid; // Nupp saadud sõnumite lugemiseks
    private JButton failinupp; // Nupp faili valimiseks
    private JTextArea sõnumiKuva; // Tekstiala sõnumite kuvamiseks

    private File valitudfail; // Valitud faili salvestamiseks

    // GUI konstruktor
    public GUI() {
        frame = new JFrame("Sõnumi rakendus");
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(600, 300)); // Minimaalne suurus vastavalt nuppudele ja "Sõnum:" lahter

        // Peapaneel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240));

        // Ülemine paneel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(230, 230, 230));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

        // Silt "Kasutaja:" ja tekstiväli kasutaja nime jaoks
        JLabel nameLabel = new JLabel("Kasutaja:");
        kasutaja = new JTextField(12); // Piirame kasutaja nime lahtri suurust 12 tähemärgini
        topPanel.add(nameLabel);
        topPanel.add(kasutaja);

        // Silt "Saaja:" ja tekstiväli saaja nime jaoks
        JLabel saajaLabel = new JLabel("Saaja:");
        saajavali = new JTextField(12); // Piirame saaja nime lahtri suurust 12 tähemärgini
        topPanel.add(saajaLabel);
        topPanel.add(saajavali);

        panel.add(topPanel, BorderLayout.NORTH);

        // Keskpärane paneel sõnumite kuvamiseks
        JPanel centerPanel = new JPanel(new BorderLayout());
        sõnumiKuva = new JTextArea();
        sõnumiKuva.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(sõnumiKuva);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Alumine paneel nuppude ja tekstiväljaga
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(230, 230, 230));

        // Nuppude paneel vasakul
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(new Color(230, 230, 230));

        // Nupp sõnumi saatmiseks
        try {
            Image sendIcon = ImageIO.read(new File("icons","send-button.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            saadanupp = new JButton(new ImageIcon(sendIcon));
        } catch (IOException e) {
            saadanupp = new JButton("Saada");
        }
        saadanupp.addActionListener(e -> sendMessage());
        buttonsPanel.add(saadanupp);

        // Nupp saadud sõnumite lugemiseks
        try {
            Image readIcon = ImageIO.read(new File("icons","read-message-icon.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            loesõnumeid = new JButton(new ImageIcon(readIcon));
        } catch (IOException e) {
            loesõnumeid = new JButton("Loe sõnumeid");
        }
        loesõnumeid.addActionListener(e -> getMessage());
        buttonsPanel.add(loesõnumeid);

        // Nupp faili valimiseks
        try {
            Image fileIcon = ImageIO.read(new File("icons","folder-icon.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            failinupp = new JButton(new ImageIcon(fileIcon));
        } catch (IOException e) {
            failinupp = new JButton("Vali fail");
        }
        failinupp.addActionListener(e -> selectFile());
        buttonsPanel.add(failinupp);

        bottomPanel.add(buttonsPanel, BorderLayout.WEST);

        // Tekstiväli sõnumi sisestamiseks
        sonumivali = new JTextField(20);
        bottomPanel.add(sonumivali, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Piirame kasutaja ja saaja väljade pikkust
        kasutaja.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (kasutaja.getText().length() >= 12)
                    evt.consume();
            }
        });

        saajavali.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (saajavali.getText().length() >= 12)
                    evt.consume();
            }
        });
    }

    // Sõnumi saatmise meetod
    private void sendMessage() {
        String name = kasutaja.getText();
        if (name.isEmpty()) { // kui kasutajate ei sisestatud
            name = "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedDateTime = now.format(formatter);

        String message = sonumivali.getText().strip();
        String receiver = saajavali.getText().strip();
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
        } else {
            System.err.println("Saaja ja sõnumi lahter peab olema täidetud!"); // teade
        }
    }

    // Saadud sõnumite lugemise meetod
    private void getMessage() {
        String receiver = kasutaja.getText();

        if (receiver.isEmpty()) System.err.println("Kasutaja lahter ei tohi olla tühi");
        else {
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
    }

    // Faili valimise meetod
    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            valitudfail = fileChooser.getSelectedFile();
        }
    }

    // Main meetod
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
