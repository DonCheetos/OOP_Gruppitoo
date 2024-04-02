import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        int portNumber = 1337;
        int sõnumSuurus = args.length;

        if (sõnumSuurus % 2 == 0) { // eelne kontroll kas annab ikka paarid serverile
            try (Socket socket = new Socket("localhost", portNumber)) { // ühendab serveriga
                System.out.println("Ühendatud serveriga pordil: " + portNumber);

                try (DataInputStream in = new DataInputStream(socket.getInputStream());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {


                    int jälgimiseks = 0;
                    out.writeInt(sõnumSuurus); //serverile sõnumite koguse andmine

                    File kaust = new File("received");
                    kaust.mkdir(); // teeb kausta received, kui olemas kaust juba ei tee midagi

                    while (sõnumSuurus != jälgimiseks) {
                        String infoTüüp = args[jälgimiseks++]; // tüübi info

                        System.out.print("Avaldasin serverile soovi tüübi: "+ '"'+infoTüüp + '"'+ ", ootan serveri vastus: ");

                        // 1 ja 2 määratud request tüübiks
                        if (infoTüüp.equals("file")) out.writeInt(1);
                        else if (infoTüüp.equals("echo")) out.writeInt(2); // kasutame echo, et saata tekste
                        else out.writeInt(-1); // ei ole olemas sellist tüüpi

                        int vastus = in.readInt(); // oleku kontrolliks

                        if (vastus == 0) { // 0 on ok, -1 on error
                            System.out.println("ok");

                            String tahetud = args[jälgimiseks++]; // sõnum või failinimi
                            out.writeUTF(tahetud);

                            if (infoTüüp.equals("echo")) System.out.println("Sõnum saadud " + in.readUTF());

                            if (infoTüüp.equals("file")) {
                                String failinimi = tahetud;
                                int suurus = in.readInt();

                                System.out.print("Soovitud fail " + '"' + failinimi+ '"' + ", Serveri vastus: ");
                                if (suurus == -1) System.out.println("error");
                                else {
                                    System.out.println("ok");
                                    try (OutputStream failiKirjutada = new FileOutputStream(new File(kaust, failinimi))) {
                                        byte[] sisu = new byte[suurus];
                                        in.readFully(sisu);

                                        failiKirjutada.write(sisu);

                                        System.out.println("Salvestud fail nimega " + '"' + failinimi + '"' + " kausta " + '"' + kaust + '"');
                                    }
                                }
                            }
                        }else {
                            out.writeUTF(args[jälgimiseks++]); // paari välja puhastamiseks
                            System.out.println("error");
                        }
                    }
                }
            }finally {
                System.out.println();
                System.out.println("Serverist lahti ühendatud");
            }
        } else System.out.println("Käsureal ei ole formaadis request paari formaadis");
    }
}

