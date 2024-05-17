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

        ArrayList<Kasutaja> kasutajatelist=new ArrayList<>();
        Grupp grupp=new Grupp();
        grupp.lisaGrupp("kõik");


        ExecutorService threads = Executors.newCachedThreadPool();
        int kliendiID = 0; // mingi ID viis kuidas aru saada, kellega tegemist

        try (ServerSocket ss = new ServerSocket(pordiNumber)) { // hõivab porti, kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + pordiNumber + ".");
            System.out.println("-".repeat(50));
            while (true) {
                Socket socket = ss.accept();
                System.out.println(kliendiID + 1 + ". klient on serveriga ühendatud.");

                threads.execute(new ParalleelTöötlemiseks(socket, kliendiID++, kasutajatelist, grupp));
            }
        }
    }
}

class ParalleelTöötlemiseks implements Runnable {
    private final Socket socket;
    private ArrayList<Kasutaja> kasutajatelist;
    private Grupp grupp;//dodo gruppile sõnumite saatmine

    private int mitmesKlient;

    public ParalleelTöötlemiseks(Socket socket, int mitmesKlient, ArrayList<Kasutaja> kasutajatelist,Grupp grupp) {
        this.socket = socket;
        this.mitmesKlient = mitmesKlient;
        this.kasutajatelist = kasutajatelist;
        this.grupp=grupp;
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
                        Server_Operations.getMessageBacklog(in, out, mitmesKlient, kasutajatelist);
                        jälgimiskes++;
                        break;

                    case SEND_MESSAGE_TO_BACKLOG:
                        Server_Operations.sendMessageToBacklog(in, out, mitmesKlient, kasutajatelist);
                        jälgimiskes+=2;
                        break;

                    case SEND_FILE_TO_SERVER:
                        Server_Operations.sendFileToServer(in, out, mitmesKlient);
                        jälgimiskes++;
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
