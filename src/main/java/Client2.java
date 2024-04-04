import java.io.*;
import java.net.Socket;

//public class Client2 {
//    public static void main(String[] args) throws IOException {
//        int portNumber = 1337;
//        int sõnumSuurus = args.length;
//        String username = "Kasutaja1";
//
//        if (sõnumSuurus % 2 == 0) {
//            try (Socket socket = new Socket("localhost", portNumber)) {
//                System.out.println("Ühendatud serveriga pordil: " + portNumber);
//
//                try (DataInputStream in = new DataInputStream(socket.getInputStream());
//                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
//
//                    int jälgimiseks = 0;
//                    out.writeInt(sõnumSuurus); // serverile sõnumite koguse andmine
//
//                    while (sõnumSuurus != jälgimiseks) {
//                        String infoTüüp = args[jälgimiseks++]; // tüübi info
//
//                        // 1 ja 2 määratud request tüübiks
//                        if (infoTüüp.equals("file")) out.writeInt(1);
//                        else if (infoTüüp.equals("echo")) out.writeInt(2);
//                        else if (infoTüüp.equals("getsonum")) out.writeInt(3);
//                        else if (infoTüüp.equals("writesonum")) out.writeInt(4);
//                        else out.writeInt(-1); // ei ole olemas sellist tüüpi
//
//                        int vastus = in.readInt(); // oleku kontrolliks
//
//                        if (vastus == 0) {
//                            System.out.println("ok");
//
//                            String tahetud = args[jälgimiseks++]; // sõnum või failinimi
//
//                            if (infoTüüp.equals("echo")) {
//                                out.writeUTF(tahetud);
//                                System.out.println("Sõnum saadud " + in.readUTF());
//                            }
//
//                            if (infoTüüp.equals("getsonum")) { // kasutaja küsib sõnumeid serverilt
//                                out.writeUTF("Kasutaja2");
//
//                                int sõnumiteArv = in.readInt(); // sõnumite kogus
//                                System.out.println("Saadud sõnumite arv: " + sõnumiteArv);
//
//                                for (int i = 0; i < sõnumiteArv; i++) { // loeb kõik sõnumeid
//                                    System.out.println("Saadud sõnum: " +  "'" + in.readUTF() + "'");
//                                }
//                            }
//
//                            if (infoTüüp.equals("writesonum")) { // kirjutab mingi sõnumi kasutajale
//
//                                out.writeUTF("Kasutaja1"); // kasutaja määramine
//
//                                if (in.readInt() == 0){ // kui kõik korra
//                                    out.writeUTF(tahetud); // kirjutab sõnumi välja
//                                }else System.out.println("Sellist kasutajat pole");
//                            }
//
//                            if (infoTüüp.equals("file")) {
//                                out.writeUTF(tahetud);
//
//                                int suurus = in.readInt();
//
//                                if (suurus == -1) System.out.println("Faili ei leitud");
//                                else {
//                                    System.out.println("Fail saadud");
//                                    try (OutputStream failiKirjutada = new FileOutputStream(tahetud)) {
//                                        byte[] sisu = new byte[suurus];
//                                        in.readFully(sisu);
//                                        failiKirjutada.write(sisu);
//                                        System.out.println("Fail salvestatud");
//                                    }
//                                }
//                            }
//                        } else {
//                            out.writeUTF(args[jälgimiseks++]); // paari välja puhastamiseks
//                            System.out.println("error");
//                        }
//                    }
//                }
//            } finally {
//                System.out.println("Serverist lahti ühendatud");
//            }
//        } else System.out.println("Käsureal ei ole formaadis request paari formaadis");
//    }
//}

// käsurida: writesonum "Tere, Klient1" getsonum 0
public class Client2 {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        int sõnumiSuurus = args.length;
        String kasutajaID = "Kasutaja2";

        if (sõnumiSuurus % 2 == 1) {
            System.out.println("Käsurea sisend peab olema formaadis: käsk sisu käsk sisu ...\nEhk käsk ja sisu käivad koos.\nEhk tühikuga eraldatud sõnesid peab alati olema PAARISARV!");
            throw new RuntimeException("Käsurea viga.");
        }

        try (Socket socket = new Socket("localhost", pordiNumber);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Ühendatud serveriga pordil: " + pordiNumber + ".");

            out.writeInt(sõnumiSuurus); // serverile sõnumite koguse andmine

            int jälgimiseks = 0;
            while (sõnumiSuurus != jälgimiseks) {
                System.out.println();
                ResponseCodes infoTüüp = ResponseCodes.stringToCode(args[jälgimiseks++]); // tüübi info

                // 1 ja 2 määratud request tüübiks
                switch (infoTüüp) {
                    case SEND_ECHO: // saada echo-sõnum
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_ECHO));
                        break;
                    case GET_FILE: // küsi faili
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_FILE));
                        break;
                    case GET_MESSAGE_BACKLOG: // saa vahepeal saadetud sõnumid
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_MESSAGE_BACKLOG));
                        break;
                    case SEND_MESSAGE_TO_BACKLOG: // saada sõnumid serverile, et hiljem edasi saata
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_MESSAGE_TO_BACKLOG));
                        break;
                    default: // tundmatu tüüp
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.RESPONSE_CODE_NOT_FOUND));
                }

                int tagastusKood = in.readInt(); // oleku kontrolliks
                if (tagastusKood < 0) { // ERROR
                    System.out.println("Tagastuskood: " + ResponseCodes.getCode(tagastusKood) + ".");
                    throw new RuntimeException("Tagastuskoodi viga: " + ResponseCodes.getCode(tagastusKood));
                }
                System.out.println("Tagastuskood: OK.");

                String sõnumiSisu = args[jälgimiseks++]; // sõnum või failinimi
                ResponseCodes tagastusKood2;
                switch (infoTüüp) {
                    case SEND_ECHO: // kasutaja saadab echo-sõnumi
                        out.writeUTF(sõnumiSisu);
                        System.out.println("Echo: \"" + in.readUTF() + "\".");
                        break;

                    case GET_MESSAGE_BACKLOG: // kasutaja küsib sõnumeid serverilt
                        out.writeUTF(kasutajaID);
                        int sõnumiteArv = in.readInt(); // sõnumite arv
                        System.out.println("Saadud sõnumite arv: " + sõnumiteArv + ".");
                        for (int i = 0; i < sõnumiteArv; i++) // loeb kõik sõnumeid
                            System.out.println("Saadud sõnum: \"" + in.readUTF() + "\".");
                        break;

                    case SEND_MESSAGE_TO_BACKLOG: // kirjutab mingi sõnumi kasutajale
                        String saajaID = "Kasutaja1";
                        out.writeUTF(saajaID); // kasutaja määramine

                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
                        if (tagastusKood2 == ResponseCodes.USER_NOT_FOUND) { // kui sellist kasutajat ei leitud
                            System.out.println("Sellist kasutajat pole: " + saajaID + ".");
                            break;
                        }
                        out.writeUTF(sõnumiSisu); // kirjutab sõnumi välja
                        break;

                    case GET_FILE: // küsib serverilt faili
                        out.writeUTF(sõnumiSisu); // saadab faili nime

                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
                        if (tagastusKood2 == ResponseCodes.FILE_NOT_FOUND) {
                            System.out.println("Faili (\"" + sõnumiSisu + "\") ei leitud.");
                            break;
                        }

                        System.out.print("Fail leitud... ");
                        int failiSuurus = in.readInt();
                        try (OutputStream uusFail = new FileOutputStream(sõnumiSisu)) {
                            byte[] sisu = new byte[failiSuurus];
                            in.readFully(sisu);
                            uusFail.write(sisu); // kirjuta võrjust saadud andmed oma arvutisse uude faili
                            System.out.println("salvestatud.");
                        }
                }
            }
        } finally {
            System.out.println("Serverist lahti ühendatud.");
        }
    }
}
