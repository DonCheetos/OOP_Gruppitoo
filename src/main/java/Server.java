
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 1337;
        /* paindlik lõime loomise tüüp, mis loob vastavalt vajadusele ja taaskasutab võimalusel lõimesi, kui ei eksi
        see peaks ise ka mingi ajajooksul thread hävitama, kui seda ei kasutata enam
        */
        ExecutorService threads = Executors.newCachedThreadPool();
        int mitmesKlient = 0; // mingi ID viis kuidas arusaada kellega tegemist

        try(ServerSocket ss = new ServerSocket(portNumber)) { // hõivab porti kus hakkab toimetama
            System.out.println("Server ootab ühendusi pordil: " + portNumber);
            while(true) {
                Socket socket = ss.accept();
                System.out.println(mitmesKlient +".Klient on ühenduses serveriga.");

                threads.execute(new ParalleelTöötlemiseks(socket, mitmesKlient++));
            }
        }
    }
}
class ParalleelTöötlemiseks implements Runnable{
    private Socket socket;
    private int mitmesKlient;

    public ParalleelTöötlemiseks(Socket socket, int mitmesKlient) {
        this.socket = socket;
        this.mitmesKlient = mitmesKlient;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            int sõnumeid = in.readInt();
            System.out.println(mitmesKlient + ".Klient, Oodatud sõnumi kogus: " + sõnumeid);

            for (int i = 0; i < sõnumeid/2; i++) { // sõnumeid/2 kuna töötlen info paar korraga
                int sõnumTüüp = in.readInt();

                if (sõnumTüüp == 2){
                    out.writeInt(0); // kinnitab kõik on korras, võib jätkata

                    String echoSõnum = in.readUTF();

                    System.out.println(mitmesKlient + ".Klient, Saadab tagasi saadud sõnum: " + echoSõnum);

                    out.writeUTF(echoSõnum);
                }
                else if(sõnumTüüp == 1){
                    out.writeInt(0);

                    String filenimi = in.readUTF();
                    System.out.println(mitmesKlient + ".Klient, Tahab saada faili: " + filenimi);

                    try( InputStream failStream = new FileInputStream(filenimi)){
                        // Otsustasin toimetada baitidega, kuna lihtsam erinevate andmete puhul
                        byte[] fileSisu = failStream.readAllBytes();
                        out.writeInt(fileSisu.length);
                        out.write(fileSisu);
                    } catch (FileNotFoundException e) {
                        System.out.println(mitmesKlient + ".Klient, Saab veateate, kuna faili ei leitud");
                        out.writeInt(-1); // see on sõnum kliendile, et faili ei leitud
                    }
                }else {
                    System.out.println(mitmesKlient + ".Klient, Saab veateate, kuna request tüüp vale");
                    out.writeInt(-1); // antud tüüp oli vale
                    in.readUTF(); // paari välja puhastamiseks
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally { // lõpetab ühenduse kliendiga
            System.out.println(mitmesKlient + ".Klient, Lahti ühendamine");
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
