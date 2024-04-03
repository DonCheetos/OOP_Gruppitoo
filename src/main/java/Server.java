import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 1337;
        Map<String, ArrayList<String>> sõnumidKasutajale = new HashMap<>(); // salvestada sõnumeid vastavale kasutajale
        sõnumidKasutajale.put("Kasutaja1", new ArrayList<>());
        sõnumidKasutajale.put("Kasutaja2", new ArrayList<>());


        ExecutorService threads = Executors.newCachedThreadPool();
        int mitmesKlient = 0; // mingi ID viis kuidas aru saada, kellega tegemist

        try (ServerSocket ss = new ServerSocket(portNumber)) { // hõivab porti, kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + portNumber);
            while (true) {
                Socket socket = ss.accept();
                System.out.println(mitmesKlient + ". Klient on ühenduses serveriga.");

                threads.execute(new ParalleelTöötlemiseks(socket, mitmesKlient++, sõnumidKasutajale));
            }
        }
    }
}

class ParalleelTöötlemiseks implements Runnable {
    private Socket socket;
    private int mitmesKlient;

    public Map<String, ArrayList<String>> sõnumidKasutajale;

    public ParalleelTöötlemiseks(Socket socket, int mitmesKlient, Map<String, ArrayList<String>> sõnumidKasutajale) {
        this.socket = socket;
        this.mitmesKlient = mitmesKlient;
        this.sõnumidKasutajale = sõnumidKasutajale;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int sõnumeid = in.readInt();
            System.out.println(mitmesKlient + ". Klient, Oodatud sõnumi kogus: " + sõnumeid);

            for (int i = 0; i < sõnumeid / 2; i++) {
                int sõnumTüüp = in.readInt();

                if (sõnumTüüp == 2) {
                    out.writeInt(0); // kinnitab, et kõik on korras, võib jätkata

                    String echoSõnum = in.readUTF();

                    System.out.println(mitmesKlient + ". Klient, Saadab sõnumi: " + echoSõnum);

                    out.writeUTF(echoSõnum);
                } else if (sõnumTüüp == 1) {
                    out.writeInt(0);

                    String filenimi = in.readUTF();
                    System.out.println(mitmesKlient + ". Klient, Tahab saada faili: " + filenimi);

                    try (InputStream failStream = new FileInputStream(filenimi)) {
                        byte[] fileSisu = failStream.readAllBytes();
                        out.writeInt(fileSisu.length);
                        out.write(fileSisu);
                    } catch (FileNotFoundException e) {
                        System.out.println(mitmesKlient + ". Klient, Saab veateate, kuna faili ei leitud");
                        out.writeInt(-1); // saadab kliendile teate, et faili ei leitud
                    }
                } else if (sõnumTüüp == 3) { // klient küsib sõnumeid andes enda nime
                    out.writeInt(0); // kõik korras

                    System.out.println("Edastan kliendile sõnumeid teiste klientide poolt");

                    String kasutaja = in.readUTF();
                    List<String> kasutajaSõnumid = sõnumidKasutajale.get(kasutaja);
                    if (kasutajaSõnumid == null)  out.writeInt(0);
                    else {
                        int sõnumiteKogus = kasutajaSõnumid.size();
                        out.writeInt(sõnumiteKogus);
                        System.out.println("Kasutajal sõnumeid: " + sõnumiteKogus);
                        for (String sõnum : kasutajaSõnumid) {
                            out.writeUTF(sõnum);
                        }
                        kasutajaSõnumid.clear(); // puhastab listi, kuna kõik sõnumid on loetud
                    }
                } else if (sõnumTüüp == 4) { // sõnumitüüp 4 näitab soovi saata sõnum mingile kasutajale, kui on offline
                    out.writeInt(0); // kõik korras

                    String sihtKasutaja = in.readUTF(); // Loeb sihtkasutaja nime

                    System.out.println("Sihkasutaja: " +sihtKasutaja);
                    if (sõnumidKasutajale.containsKey(sihtKasutaja)) {
                        out.writeInt(0); // kõik korras, kasutaja olemas
                        String sõnum = in.readUTF();

                        sõnumidKasutajale.get(sihtKasutaja).add(sõnum);
                        System.out.println("Salvestasin tekstisisu");
                        System.out.println(sihtKasutaja + ", sõnum: " + sõnumidKasutajale.get(sihtKasutaja));
                    } else {
                        out.writeInt(-1); // kasutajat ei leitud
                    }
                }

                else {
                    System.out.println(mitmesKlient + ". Klient, Saab veateate, kuna request tüüp on vale");
                    out.writeInt(-1); // antud tüüp oli vale
                    in.readUTF(); // paari välja puhastamiseks
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println(mitmesKlient + ". Klient, Lahti ühendamine");
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
