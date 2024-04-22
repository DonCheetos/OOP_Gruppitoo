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
            System.out.println("----------------");
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
            int jälgimiskes = 0;
            //for (int i = 0; i < sõnumiteArv / 2; i++) {
            while(sõnumiteArv != jälgimiskes){
                System.out.println();
                ResponseCodes sõnumiTüüp = ResponseCodes.getCode(in.readInt()); jälgimiskes++;

                switch (sõnumiTüüp) {
                    case SEND_ECHO:
                        System.out.println(ResponseCodes.SEND_ECHO);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kinnitab, et kõik on korras, võib jätkata

                        String echoSõnum = in.readUTF(); jälgimiskes++;
                        System.out.println(mitmesKlient + 1 + ". klient saatis sõnumi: \"" + echoSõnum + "\".");

                        out.writeUTF(echoSõnum);
                        break;

                    case GET_FILE:
                        System.out.println(ResponseCodes.GET_FILE);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));

                        String failiNimi = in.readUTF(); jälgimiskes++;//faili path+nimi
                        System.out.println(mitmesKlient + 1 + ". klient tahab saada faili: \"" + failiNimi + "\".");

                        try (InputStream failStream = new FileInputStream(failiNimi)) {
                            out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // fail leitud
                            byte[] fail = failStream.readAllBytes();
                            out.writeInt(fail.length);
                            out.write(fail);
                        } catch (FileNotFoundException e) {
                            System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateade, kuna faili (\"" + failiNimi + "\") ei leitud.");
                            out.writeInt(ResponseCodes.getValue(ResponseCodes.FILE_NOT_FOUND)); // saadab kliendile teate, et faili ei leitud
                        }
                        break;

                    case GET_MESSAGE_BACKLOG:
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

                        String kasutajaID = in.readUTF(); jälgimiskes++;
                        System.out.println("Edastan kliendile (" + kasutajaID + ") sõnumi(d), mis teised on talle vahepeal saatnud.");
                        List<String> kasutajaleSaadetudSõnumid = sõnumidKasutajale.get(kasutajaID);
                        if (kasutajaleSaadetudSõnumid == null) {
                            out.writeInt(0);
                            break;
                        }

                        int sõnumiteKogus = kasutajaleSaadetudSõnumid.size();
                        out.writeInt(sõnumiteKogus);
                        System.out.println("kliendile on saadetud " + sõnumiteKogus + " sõnum(it).");

                        for (String sõnum : kasutajaleSaadetudSõnumid)
                            out.writeUTF(sõnum);

                        kasutajaleSaadetudSõnumid.clear(); // puhastab listi, kuna kõik sõnumid on loetud
                        break;

                    case SEND_MESSAGE_TO_BACKLOG:
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

                        String sihtKasutaja = in.readUTF(); jälgimiskes++; // Loeb sihtkasutaja nime
                        System.out.println("Sihkasutaja: " + sihtKasutaja + ".");

                        if (!sõnumidKasutajale.containsKey(sihtKasutaja)) { // !!! kas siin ei peaks mitte uut kasutajat looma ja talle jätma !!!
                            out.writeInt(ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND)); // kasutajat ei leitud
                            jälgimiskes++;
                            break;
                        }

                        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras, kasutaja olemas
                        String sõnum = in.readUTF(); jälgimiskes++;

                        sõnumidKasutajale.get(sihtKasutaja).add(sõnum);
                        System.out.println("Salvestasin tekstisisu.");
                        System.out.println(sihtKasutaja + ", sõnum: \"" + sõnumidKasutajale.get(sihtKasutaja) + "\".");
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
                        System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateate, kuna request tüüp on vale: " + sõnumiTüüp + ".");
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.RESPONSE_CODE_NOT_FOUND)); // antud tüüp oli vale
                        in.readUTF(); jälgimiskes++;// paari välja puhastamiseks
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println();
            System.out.println(mitmesKlient + 1 + ". klient lõpetab ühenduse.");
            System.out.println("----------------");
        }
    }
}
