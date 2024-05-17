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
        Map<String, String> kasutajaInfo = new HashMap<>(); // sisaldab kasutajanime ja parooli räsi
        Map<String, ArrayList<String>> sõnumidKasutajale = new HashMap<>(); // salvestada sõnumeid vastavale kasutajale

        ExecutorService threads = Executors.newCachedThreadPool();
        int kliendiID = 0; // mingi ID viis kuidas aru saada, kellega tegemist

        try (ServerSocket ss = new ServerSocket(pordiNumber)) { // hõivab porti, kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + pordiNumber + ".");
            System.out.println("-".repeat(50));
            while (true) {
                Socket socket = ss.accept();
                System.out.println(kliendiID + 1 + ". klient on serveriga ühendatud.");

                threads.execute(new ParalleelTöötlemiseks(socket, kliendiID++, sõnumidKasutajale,kasutajaInfo));
            }
        }
    }
}

class ParalleelTöötlemiseks implements Runnable {
    private final Socket socket;
    public Map<String, ArrayList<String>> sõnumidKasutajale;
    private int mitmesKlient;
    Map<String, String> kasutajaInfo;

    public ParalleelTöötlemiseks(Socket socket, int mitmesKlient, Map<String, ArrayList<String>> sõnumidKasutajale, Map<String, String> kasutajaInfo) {
        this.socket = socket;
        this.mitmesKlient = mitmesKlient;
        this.sõnumidKasutajale = sõnumidKasutajale;
        this.kasutajaInfo = kasutajaInfo;
    }

    @Override
    public void run() {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int sõnumiteArv = in.readInt();
            System.out.println(mitmesKlient + 1 + ". kliendilt oodatud sõnumite arv: " + sõnumiteArv + ".");
            int jälgimiskes = 0;
            while(sõnumiteArv != jälgimiskes){
                System.out.println();
                ResponseCodes sõnumiTüüp = ResponseCodes.getCode(in.readInt()); jälgimiskes++;

                switch (sõnumiTüüp) { // teostatakse serverile saadetud päringuid
                    case SEND_ECHO:
                        Server_Operations.sendEcho(in, out, mitmesKlient);
                        jälgimiskes++;
                        break;

                    case GET_FILE:
                        Server_Operations.getFile(in, out, mitmesKlient);
                        jälgimiskes++;
                        break;

                    case GET_MESSAGE_BACKLOG:
                        Server_Operations.getMessageBacklog(in, out, mitmesKlient, sõnumidKasutajale);
                        jälgimiskes++;
                        break;

                    case SEND_MESSAGE_TO_BACKLOG:
                        Server_Operations.sendMessageToBacklog(in, out, mitmesKlient, sõnumidKasutajale);
                        jälgimiskes+=2;
                        break;

                    case SEND_FILE_TO_SERVER:
                        Server_Operations.sendFileToServer(in, out, mitmesKlient);
                        jälgimiskes++;
                        break;
                    case CREATE_USER:
                        Server_Operations.createUser(in,out,mitmesKlient,kasutajaInfo);
                        break;
                    case CHECK_USER:
                        Server_Operations.checkUser(in,out,mitmesKlient,kasutajaInfo);
                        break;

                    default:
                        System.out.println(ResponseCodes.RESPONSE_CODE_NOT_FOUND);
                        System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateate, kuna request tüüp on vale: " + sõnumiTüüp + ".");
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.RESPONSE_CODE_NOT_FOUND)); // antud tüüp oli vale
                        in.readUTF(); jälgimiskes++;// päringu komplekti välja puhastamiseks
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println();
            System.out.println(mitmesKlient + 1 + ". klient lõpetab ühenduse.");
            System.out.println("-".repeat(50));
        }
    }
}
