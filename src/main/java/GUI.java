import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GUI extends JDialog {
    private final JTextField kasutaja;
    private final JTextField sonumivali;
    private final JTextField saajaVäli;
    private final JTextArea sõnumiKuva;
    private final Set<String> loetudSonumid = new HashSet<>(); // hoidla loetud sõnumite ID-de jaoks
    private final ScheduledExecutorService scheduler;
    private File valitudfail;

    public GUI(JFrame frame, String kasutajanimi) {
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(600, 300));

        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Set.of(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Set.of(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(230, 230, 230));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

        JLabel nameLabel = new JLabel("Kasutaja:");
        kasutaja = new JTextField(12);
        kasutaja.setText(kasutajanimi);
        kasutaja.setEditable(false);
        topPanel.add(nameLabel);
        topPanel.add(kasutaja);

        JLabel saajaLabel = new JLabel("Saaja:");
        saajaVäli = new JTextField(12);
        saajaVäli.addKeyListener(looEnterKuulaja(this::sendMessage));
        topPanel.add(saajaLabel);
        topPanel.add(saajaVäli);

        JButton logiVäljaNupp = new JButton("Logi välja");
        logiVäljaNupp.addActionListener(e -> {
            throw new LogiVälja("Rakendusest logiti välja!");
        });
        logiVäljaNupp.addKeyListener(looEnterKuulaja(() -> {
            throw new LogiVälja("Rakendusest logiti välja!");
        }));
        topPanel.add(logiVäljaNupp, BorderLayout.EAST);

        JButton sulgeNupp = new JButton("Sulge");
        sulgeNupp.addActionListener(e -> {
            throw new Sulge("Rakendus sulgeti!");
        });
        sulgeNupp.addKeyListener(looEnterKuulaja(() -> {
            throw new Sulge("Rakendus sulgeti!");
        }));
        topPanel.add(sulgeNupp, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        sõnumiKuva = new JTextArea();
        sõnumiKuva.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(sõnumiKuva);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(230, 230, 230));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(new Color(230, 230, 230));

        JButton saadaNupp;
        try {
            Image sendIcon = ImageIO.read(new File("icons", "send-button.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            saadaNupp = new JButton(new ImageIcon(sendIcon));
        } catch (IOException e) {
            saadaNupp = new JButton("Saada");
        }
        saadaNupp.addActionListener(e -> sendMessage());
        saadaNupp.addKeyListener(looEnterKuulaja(this::sendMessage));
        buttonsPanel.add(saadaNupp);

        JButton failiNupp;
        try {
            Image fileIcon = ImageIO.read(new File("icons", "folder-icon.png")).getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            failiNupp = new JButton(new ImageIcon(fileIcon));
        } catch (IOException e) {
            failiNupp = new JButton("Vali fail");
        }
        failiNupp.addActionListener(e -> selectFile(frame));
        failiNupp.addKeyListener(looEnterKuulaja(() -> selectFile(frame)));
        buttonsPanel.add(failiNupp);

        bottomPanel.add(buttonsPanel, BorderLayout.WEST);

        sonumivali = new JTextField(20);
        sonumivali.addKeyListener(looEnterKuulaja(this::sendMessage));
        bottomPanel.add(sonumivali, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        kasutaja.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (kasutaja.getText().length() >= 12)
                    evt.consume();
            }
        });

        saajaVäli.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (saajaVäli.getText().length() >= 12)
                    evt.consume();
            }
        });

        // Laadi eelnevad sõnumid
        laadieelsedSonumid();

        // automaatne pollimise süsteem, täidab ülesannet iga 1 sekundi tagant
        scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = this::getMessage;
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
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

    public void peata() {
        scheduler.shutdown();
    }

    private void sendMessage() {
        String saatja = kasutaja.getText();
        if (saatja.isEmpty()) {
            saatja = "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedDateTime = now.format(formatter);

        String sõnum = sonumivali.getText().strip();
        String saaja = saajaVäli.getText().strip();
        String koguSõnum = '(' + formattedDateTime + ") " + saatja + " : " + sõnum;

        if (!saaja.isEmpty() && !sõnum.isEmpty()) {
            try {
                String[] command;
                if (valitudfail != null) {
                    command = new String[]{"writesonum", saaja, koguSõnum, "sendfile", valitudfail.getAbsolutePath()};
                } else {
                    command = new String[]{"writesonum", saaja, koguSõnum};
                }
                Client.main(command);

                if (!saaja.equals(saatja)) {
                    sõnumiKuva.append(koguSõnum + "\n");
                    sõnumiKuva.setCaretPosition(sõnumiKuva.getDocument().getLength());
                }
                sonumivali.setText("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Saaja ja sõnumi lahter peavad olema täidetud!");
            JOptionPane.showMessageDialog(this, "Saaja ja sõnumi lahter peavad olema täidetud!", "Viga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getMessage() {
        String saaja = kasutaja.getText();

        if (saaja.isEmpty()) {
            System.err.println("Kasutaja lahter ei tohi olla tühi!");
            JOptionPane.showMessageDialog(this, "Kasutaja lahter ei tohi olla tühi!", "Viga", JOptionPane.ERROR_MESSAGE);
        } else if (saaja.equals("Unknown")) {
            System.err.println("Külalisena sõnumeid ei saa lugeda, ainult saata!");
            JOptionPane.showMessageDialog(this, "Külalisena sõnumeid ei saa lugeda, ainult saata!", "Viga", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                String[] command = {"getsonum", saaja};
                Client.main(command);

                try {
                    List<String> messages = FileUtil.readFromFile(saaja + "_msg.txt");
                    for (String sõnum : messages) {
                        if (loetudSonumid.add(sõnum)) { // lisa ja kontrolli kas uus sõnum
                            String saatja = sõnum.split(" ")[2];
                            System.out.println("." + saatja + ".");
                            sõnumiKuva.append(sõnum + (saaja.equals(saatja) ? " (edukalt iseendale saadetud)\n" : "\n"));
                        }
                    }
                    sõnumiKuva.setCaretPosition(sõnumiKuva.getDocument().getLength());
                } catch (IOException e) {
                    System.out.println("Sõnumeid polnud");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void laadieelsedSonumid() {
        String receiver = kasutaja.getText();
        if (!receiver.isEmpty()) {
            try {
                List<String> messages = FileUtil.readFromFile(receiver + "_msg.txt");
                for (String message : messages) {
                    if (loetudSonumid.add(message)) { // laadi eelnevad sõnumid ja lisa need loetud sõnumite hulka
                        sõnumiKuva.append(message + "\n");
                    }
                }
                sõnumiKuva.setCaretPosition(sõnumiKuva.getDocument().getLength());
            } catch (IOException e) {
                System.err.println("Eelnevaid sõnumeid ei õnnestunud laadida!");
                JOptionPane.showMessageDialog(this, "Eelnevaid sõnumeid ei õnnestunud laadida!", "Viga", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectFile(JFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            valitudfail = fileChooser.getSelectedFile();
        }
    }
}
