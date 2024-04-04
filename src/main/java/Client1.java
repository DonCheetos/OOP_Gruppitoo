import java.io.*;
import java.net.Socket;

// käsurida: echo Tere writesonum "Sõnum, see on sõnum"
public class Client1 {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        int sõnumiSuurus = args.length;
        String kasutajaID = "Kasutaja1";

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
                ResponseCodes infoTüüp = ResponseCodes.stringToCode(args[jälgimiseks++]); // tüübi info

                // 1 ja 2 määratud request tüübiks
                switch (infoTüüp) {
                    case SEND_ECHO:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_ECHO));
                        break;
                    case GET_FILE:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_FILE));
                        break;
                    case GET_MESSAGE_BACKLOG:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_MESSAGE_BACKLOG));
                        break;
                    case SEND_MESSAGE_TO_BACKLOG:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_MESSAGE_TO_BACKLOG));
                        break;
                    default:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.RESPONSE_CODE_NOT_FOUND)); // ei ole olemas sellist tüüpi
                }

                int tagastusKood = in.readInt(); // oleku kontrolliks
                if (tagastusKood < 0) { // ERROR
                    System.out.println("Tagastuskood: " + ResponseCodes.getCode(tagastusKood) + ".");
                    throw new RuntimeException("Tagastuskoodi viga: " + ResponseCodes.getCode(tagastusKood));
                }
                System.out.println("Tagastuskood: OK.");

                String sõnumiSisu = args[jälgimiseks++]; // sõnum või failinimi
                System.out.println(infoTüüp + ", " + sõnumiSisu);
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
                        String saajaID = "Kasutaja2";
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
