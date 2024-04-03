import java.io.*;
import java.net.Socket;

public class Client2 {
    public static void main(String[] args) throws IOException {
        int portNumber = 1337;
        int sõnumSuurus = args.length;
        String username = "Kasutaja1";

        if (sõnumSuurus % 2 == 0) {
            try (Socket socket = new Socket("localhost", portNumber)) {
                System.out.println("Ühendatud serveriga pordil: " + portNumber);

                try (DataInputStream in = new DataInputStream(socket.getInputStream());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    int jälgimiseks = 0;
                    out.writeInt(sõnumSuurus); // serverile sõnumite koguse andmine

                    while (sõnumSuurus != jälgimiseks) {
                        String infoTüüp = args[jälgimiseks++]; // tüübi info

                        // 1 ja 2 määratud request tüübiks
                        if (infoTüüp.equals("file")) out.writeInt(1);
                        else if (infoTüüp.equals("echo")) out.writeInt(2);
                        else if (infoTüüp.equals("getsonum")) out.writeInt(3);
                        else if (infoTüüp.equals("writesonum")) out.writeInt(4);
                        else out.writeInt(-1); // ei ole olemas sellist tüüpi

                        int vastus = in.readInt(); // oleku kontrolliks

                        if (vastus == 0) {
                            System.out.println("ok");

                            String tahetud = args[jälgimiseks++]; // sõnum või failinimi

                            if (infoTüüp.equals("echo")) {
                                out.writeUTF(tahetud);
                                System.out.println("Sõnum saadud " + in.readUTF());
                            }

                            if (infoTüüp.equals("getsonum")) { // kasutaja küsib sõnumeid serverilt
                                out.writeUTF("Kasutaja2");

                                int sõnumiteArv = in.readInt(); // sõnumite kogus
                                System.out.println("Saadud sõnumite arv: " + sõnumiteArv);

                                for (int i = 0; i < sõnumiteArv; i++) { // loeb kõik sõnumeid
                                    System.out.println("Saadud sõnum: " +  "'" + in.readUTF() + "'");
                                }
                            }

                            if (infoTüüp.equals("writesonum")) { // kirjutab mingi sõnumi kasutajale

                                out.writeUTF("Kasutaja1"); // kasutaja määramine

                                if (in.readInt() == 0){ // kui kõik korra
                                    out.writeUTF(tahetud); // kirjutab sõnumi välja
                                }else System.out.println("Sellist kasutajat pole");
                            }

                            if (infoTüüp.equals("file")) {
                                out.writeUTF(tahetud);

                                String failinimi = tahetud;
                                int suurus = in.readInt();

                                if (suurus == -1) System.out.println("Faili ei leitud");
                                else {
                                    System.out.println("Fail saadud");
                                    try (OutputStream failiKirjutada = new FileOutputStream(new File(failinimi))) {
                                        byte[] sisu = new byte[suurus];
                                        in.readFully(sisu);
                                        failiKirjutada.write(sisu);
                                        System.out.println("Fail salvestatud");
                                    }
                                }
                            }
                        } else {
                            out.writeUTF(args[jälgimiseks++]); // paari välja puhastamiseks
                            System.out.println("error");
                        }
                    }
                }
            } finally {
                System.out.println("Serverist lahti ühendatud");
            }
        } else System.out.println("Käsureal ei ole formaadis request paari formaadis");
    }
}
