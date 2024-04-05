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
    protected Map<Integer, ArrayList<String>> laekunudSõnumid;
    private int pordiNumber;

    public Server(int pordiNumber) {
        this.pordiNumber = pordiNumber;
        laekunudSõnumid = new HashMap<>();
    }

    public static void main(String[] args) throws IOException {
//        int pordiNumber = 1337;
//        Map<String, ArrayList<String>> sõnumidKasutajale = new HashMap<>(); // salvestada sõnumeid vastavale kasutajale
//        sõnumidKasutajale.put("Kasutaja1", new ArrayList<>());
//        sõnumidKasutajale.put("Kasutaja2", new ArrayList<>());

        ExecutorService threads = Executors.newCachedThreadPool();
//        int kliendiID = 0; // mingi ID viis kuidas aru saada, kellega tegemist

        Server server = new Server(1337); // loob uue serveri
        try (ServerSocket ss = new ServerSocket(server.pordiNumber)) { // hõivab porti, kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + server.pordiNumber + ".");
            System.out.println("----------------");
            while (true) {
                Socket socket = ss.accept();
                System.out.println("Klient on serveriga ühendatud.");

                threads.execute(new ParalleelTöötlemiseks(socket, server.laekunudSõnumid));
            }
        }
    }
}

class ParalleelTöötlemiseks implements Runnable {
    private final Socket socket;
    public Map<Integer, ArrayList<String>> laekunudSõnumid;
//    private int mitmesKlient;

    public ParalleelTöötlemiseks(Socket socket, Map<Integer, ArrayList<String>> laekunudSõnumid) {
        this.socket = socket;
//        this.mitmesKlient = mitmesKlient;
        this.laekunudSõnumid = laekunudSõnumid;
    }

    @Override
    public void run() {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int kliendiID = in.readInt(); // Serveriga ühendunud kliendi ID
            int sõnumiteArv = in.readInt(); // Kliendi saadetavate sõnumite koguarv
            System.out.println("Kliendi ID=" + kliendiID + ".");
            System.out.println("Kliendilt oodatud sõnumite arv: " + sõnumiteArv + ".");

            int jälgimiskes = 0;
            while (jälgimiskes < sõnumiteArv - 1) {
                System.out.println();
                ResponseCodes sõnumiTüüp = ResponseCodes.getCode(in.readInt());
                jälgimiskes++;

                switch (sõnumiTüüp) {
                    case SEND_ECHO:
                        System.out.println(ResponseCodes.SEND_ECHO);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kinnitab, et kõik on korras, võib jätkata

                        String echoSõnum = in.readUTF();
                        jälgimiskes++;
                        System.out.println("Klient saatis sõnumi: \"" + echoSõnum + "\".");

                        out.writeUTF(echoSõnum);
                        break;

                    case GET_FILE:
                        System.out.println(ResponseCodes.GET_FILE);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));

                        String failiNimi = in.readUTF();
                        jälgimiskes++;
                        System.out.println("Klient tahab saada faili: \"" + failiNimi + "\".");

                        try (InputStream failStream = new FileInputStream("src/main/resources/server/" + failiNimi)) {
                            out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // fail leitud
                            System.out.print("Fail leitud... ");
                            byte[] fail = failStream.readAllBytes();
                            out.writeInt(fail.length);
                            out.write(fail);
                            System.out.println("saadetud.");
                        } catch (FileNotFoundException e) {
                            System.out.println("Kliendile saadetakse veateade, kuna faili (\"" + failiNimi + "\") ei leitud.");
                            out.writeInt(ResponseCodes.getValue(ResponseCodes.FILE_NOT_FOUND)); // saadab kliendile teate, et faili ei leitud
                        }
                        break;

                    case GET_MESSAGE_BACKLOG:
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

                        System.out.println("Edastan kliendile (ID=" + kliendiID + ") sõnumi(d), mis teised on talle vahepeal saatnud.");
                        List<String> kasutajaleSaadetudSõnumid = laekunudSõnumid.get(kliendiID);
                        if (kasutajaleSaadetudSõnumid == null) {
                            System.out.println("Kliendile pole sõnumeid saadetud.");
                            out.writeInt(0);
                            break;
                        }

                        int sõnumiteKogus = kasutajaleSaadetudSõnumid.size();
                        out.writeInt(sõnumiteKogus);
                        System.out.println("Kliendile on saadetud " + sõnumiteKogus + " sõnum(it): " + laekunudSõnumid.get(kliendiID));

                        for (String sõnum : kasutajaleSaadetudSõnumid)
                            out.writeUTF(sõnum);

                        kasutajaleSaadetudSõnumid.clear(); // puhastab listi, kuna kõik sõnumid on loetud
                        break;

                    case SEND_MESSAGE_TO_BACKLOG:
                        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

                        int sihtKasutajaID = in.readInt();
                        jälgimiskes++;
                        System.out.println("Sihkasutaja: ID=" + sihtKasutajaID + ".");

                        if (!laekunudSõnumid.containsKey(sihtKasutajaID)) { // !!! kas siin ei peaks mitte uut kasutajat looma ja talle jätma !!!
                            System.out.println("Kasutajat (ID=" + sihtKasutajaID + ") ei leitud. Loon uue kasutaja sõnumite loendi.");
                            laekunudSõnumid.put(sihtKasutajaID, new ArrayList<>());
//                            out.writeInt(ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND)); // kasutajat ei leitud
//                            break;
                        }

//                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras, kasutaja olemas
                        String sõnum = in.readUTF();
                        jälgimiskes++;

                        laekunudSõnumid.get(sihtKasutajaID).add(sõnum);
                        System.out.println("Salvestasin tekstisisu.");
                        System.out.println("Sõnumid kasutajale (ID=" + sihtKasutajaID + "): \"" + laekunudSõnumid.get(sihtKasutajaID) + "\".");
                        break;
                    case SEND_FILE_TO_SERVER:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras
                        String sisenevaFailiNimi = in.readUTF(); jälgimiskes++;
                        System.out.println("sain faili:"+sisenevaFailiNimi+" kirjutan kausta");
                        // Find the last index of '/' or '\\'
                        int lastIndex = sisenevaFailiNimi.lastIndexOf('/');
                        if (lastIndex == -1) {
                            lastIndex = sisenevaFailiNimi.lastIndexOf('\\');
                        }
                        // Extract the filename
                        String filename = sisenevaFailiNimi.substring(lastIndex + 1);

                        int failisuurus=in.readInt();
                        try(FileOutputStream fos = new FileOutputStream("received/"+filename)){
                            //System.out.println("failisuurus on:"+failisuurus);
                            fos.write(in.readNBytes(failisuurus));
                            out.writeInt(0);
                        }catch (IOException e){
                            System.out.println("ei kirjutanud edukalt");
                            System.out.println(e.getMessage());
                            out.writeInt(-5);
                        }
                        break;

                    default:
                        System.out.println(ResponseCodes.RESPONSE_CODE_NOT_FOUND);
                        System.out.println("Kliendile saadetakse veateate, kuna request tüüp on vale: " + sõnumiTüüp + ".");
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.RESPONSE_CODE_NOT_FOUND)); // antud tüüp oli vale
                        in.readUTF();
                        jälgimiskes++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println();
            System.out.println("Klient lõpetab ühenduse.");
            System.out.println("----------------");
        }
    }
}
