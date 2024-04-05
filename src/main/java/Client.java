import java.io.*;
import java.net.Socket;

// käsurida: echo Tere writesonum "Sõnum, see on sõnum"
public class Client {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        int sõnumiSuurus = args.length;
//        String kasutajaID = "Kasutaja1";

        /*
        if (sõnumiSuurus % 2 == 1) {
            System.out.println("Käsurea sisend peab olema formaadis: käsk sisu käsk sisu ...\nEhk käsk ja sisu käivad koos.\nEhk tühikuga eraldatud sõnesid peab alati olema PAARISARV!");
            throw new RuntimeException("Käsurea viga.");
        } */

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
                        System.out.println(ResponseCodes.SEND_ECHO + ": \"" + sõnumiSisu + "\"");
                        out.writeUTF(sõnumiSisu);
                        System.out.println("Serverilt saadud echo-sõnum: \"" + in.readUTF() + "\".");
                        break;

                    case GET_MESSAGE_BACKLOG: // kasutaja küsib sõnumeid serverilt
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        String kasutajaID = sõnumiSisu; // loeb käasureal kasutajanime
                        out.writeUTF(kasutajaID);
                        int sõnumiteArv = in.readInt(); // sõnumite arv
                        System.out.println("Saadud sõnumite arv: " + sõnumiteArv + ".");
                        for (int i = 0; i < sõnumiteArv; i++) // loeb kõik sõnumeid
                            System.out.println("Saadud sõnum: \"" + in.readUTF() + "\".");
                        break;

                    case SEND_MESSAGE_TO_BACKLOG: // kirjutab mingi sõnumi kasutajale, käasureal järjekord 'requestTüüp kasutaja sõnum'
                        String saajaID = sõnumiSisu; // loeb käasureal kasutajanime
                        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG + ": \n    Sõnumi saaja: \"" + saajaID + "\"");
                        out.writeUTF(saajaID); // kasutaja määramine

                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
                        if (tagastusKood2 == ResponseCodes.USER_NOT_FOUND) { // kui sellist kasutajat ei leitud
                            System.out.println("Sellist kasutajat pole: " + saajaID + ".");
                            break;
                        }

                        sõnumiSisu = args[jälgimiseks++];
                        System.out.println("    Sõnumi sisu: \"" + sõnumiSisu + "\"");
                        out.writeUTF(sõnumiSisu); // kirjutab sõnumi välja
                        break;

                    case GET_FILE: // küsib serverilt faili
                        String failiNimi = sõnumiSisu;
                        System.out.println(ResponseCodes.GET_FILE + ": \"" + failiNimi + "\"");
                        out.writeUTF(failiNimi); // saadab faili nime

                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
                        if (tagastusKood2 == ResponseCodes.FILE_NOT_FOUND) {
                            System.out.println("Faili (\"" + failiNimi + "\") ei leitud.");
                            break;
                        }

                        System.out.print("Fail leitud... ");
                        int failiSuurus = in.readInt();
                        try (OutputStream uusFail = new FileOutputStream(failiNimi)) {
                            byte[] sisu = new byte[failiSuurus];
                            in.readFully(sisu);
                            uusFail.write(sisu); // kirjuta võrjust saadud andmed oma arvutisse uude faili
                            System.out.println("salvestatud.");
                        }
                }
            }
        } finally {
            System.out.println("\nServerist lahti ühendatud.");
        }
    }
}
