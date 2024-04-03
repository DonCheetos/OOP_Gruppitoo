import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        Map<String, ArrayList<String>> sõnumidKasutajale = new HashMap<>(); // salvestada sõnumeid vastavale kasutajale
        sõnumidKasutajale.put("Kasutaja1", new ArrayList<>());
        sõnumidKasutajale.put("Kasutaja2", new ArrayList<>());


        ExecutorService threads = Executors.newCachedThreadPool();
        int kliendiID = 0; // mingi ID viis kuidas aru saada, kellega tegemist

        try (ServerSocket ss = new ServerSocket(pordiNumber)) { // hõivab porti, kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + pordiNumber + ".");
            while (true) {
                Socket socket = ss.accept();
                System.out.println(kliendiID + 1 + ". klient on serveriga ühendatud.");

                threads.execute(new ParalleelTöötlemiseks(socket, kliendiID++, sõnumidKasutajale));
            }
        }
    }
}

class ParalleelTöötlemiseks implements Runnable {
    private final Socket socket;
    public Map<String, ArrayList<String>> sõnumidKasutajale;
    private int mitmesKlient;

    public ParalleelTöötlemiseks(Socket socket, int mitmesKlient, Map<String, ArrayList<String>> sõnumidKasutajale) {
        this.socket = socket;
        this.mitmesKlient = mitmesKlient;
        this.sõnumidKasutajale = sõnumidKasutajale;
    }

    @Override
    public void run() {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int sõnumiteArv = in.readInt();
            System.out.println(mitmesKlient + 1 + ". kliendilt oodatud sõnumite arv: " + sõnumiteArv + ".");

            for (int i = 0; i < sõnumiteArv / 2; i++) {
                ResponseCodes sõnumiTüüp = ResponseCodes.getCode(in.readInt());

                switch (sõnumiTüüp) {
                    case SEND_ECHO:
                        out.writeInt(ResponseCodes.OK.ordinal()); // kinnitab, et kõik on korras, võib jätkata

                        String echoSõnum = in.readUTF();
                        System.out.println(mitmesKlient + 1 + ". klient saatis sõnumi: \"" + echoSõnum + "\".");

                        out.writeUTF(echoSõnum);
                        break;

                    case GET_FILE:
                        out.writeInt(ResponseCodes.OK.ordinal());

                        String failiNimi = in.readUTF();
                        System.out.println(mitmesKlient + 1 + ". klient tahab saada faili: \"" + failiNimi + "\".");

                        try (InputStream failStream = new FileInputStream(failiNimi)) {
                            out.writeInt(ResponseCodes.OK.ordinal()); // fail leitud
                            byte[] fail = failStream.readAllBytes();
                            out.writeInt(fail.length);
                            out.write(fail);
                        } catch (FileNotFoundException e) {
                            System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateade, kuna faili (\"" + failiNimi + "\") ei leitud.");
                            out.writeInt(ResponseCodes.FILE_NOT_FOUND.ordinal()); // saadab kliendile teate, et faili ei leitud
                        }
                        break;

                    case GET_MESSAGE_BACKLOG:
                        out.writeInt(ResponseCodes.OK.ordinal()); // kõik korras

                        System.out.println("Edastan kliendile sõnumid, mis teised on talle vahepeal saatnud.");

                        String kasutajaID = in.readUTF();
                        List<String> kasutajaleSaadetudSõnumid = sõnumidKasutajale.get(kasutajaID);
                        if (kasutajaleSaadetudSõnumid == null) {
                            out.writeInt(0);
                            break;
                        }

                        int sõnumiteKogus = kasutajaleSaadetudSõnumid.size();
                        out.writeInt(sõnumiteKogus);
                        System.out.println("Kasutajale on saadetud " + sõnumiteKogus + " sõnumit.");

                        for (String sõnum : kasutajaleSaadetudSõnumid)
                            out.writeUTF(sõnum);

                        kasutajaleSaadetudSõnumid.clear(); // puhastab listi, kuna kõik sõnumid on loetud
                        break;

                    case SEND_MESSAGE_TO_BACKLOG:
                        out.writeInt(ResponseCodes.OK.ordinal()); // kõik korras

                        String sihtKasutaja = in.readUTF(); // Loeb sihtkasutaja nime
                        System.out.println("Sihkasutaja: " + sihtKasutaja + ".");

                        if (!sõnumidKasutajale.containsKey(sihtKasutaja)) { // !!! kas siin ei peaks mitte uut kasutajat looma ja talle jätma !!!
                            out.writeInt(ResponseCodes.USER_NOT_FOUND.ordinal()); // kasutajat ei leitud
                            break;
                        }

                        out.writeInt(ResponseCodes.OK.ordinal()); // kõik korras, kasutaja olemas
                        String sõnum = in.readUTF();

                        sõnumidKasutajale.get(sihtKasutaja).add(sõnum);
                        System.out.println("Salvestasin tekstisisu.");
                        System.out.println(sihtKasutaja + ", sõnum: \"" + sõnumidKasutajale.get(sihtKasutaja) + "\".");
                        break;

                    default:
                        System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateate, kuna request tüüp on vale: " + sõnumiTüüp + ".");
                        out.writeInt(ResponseCodes.RESPONSE_CODE_NOT_FOUND.ordinal()); // antud tüüp oli vale
                        in.readUTF(); // paari välja puhastamiseks
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println(mitmesKlient + 1 + ". klient lõpetab ühenduse.");
        }
    }
}
