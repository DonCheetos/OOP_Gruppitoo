import java.io.*;
import java.net.Socket;
import java.net.SocketException;

// käsurida: echo Tere writesonum "Sõnum, see on sõnum"
public class Client {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        int sõnumiSuurus = args.length;
        if(args.length==0){//ütlen võimalikud käsud kui midagi ei anta ette
            System.out.println("Võimalikud sisendid on{");
            ResponseCodes.koodid();//väljastab sisendid
            System.out.println("}");
            System.exit(1);//lõpetab koodi kuna midagi edasi ei tehta
        }
        //String kasutajaID = "Kasutaja1";

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
                    case SEND_FILE_TO_SERVER:
                        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_FILE_TO_SERVER));
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
                switch (infoTüüp) {
                    case SEND_ECHO: // kasutaja saadab echo-sõnumi
                        ClientOperations.sendEcho(in,out,sõnumiSisu);
                        break;

                    case GET_MESSAGE_BACKLOG: // kasutaja küsib sõnumeid serverilt
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        String kasutajaID = sõnumiSisu; // loeb käasureal kasutajanime
                        ClientOperations.getMessage(in,out,kasutajaID);

                        break;

                    case SEND_MESSAGE_TO_BACKLOG: // kirjutab mingi sõnumi kasutajale, käasureal järjekord 'requestTüüp kasutaja sõnum'
                        String saajaID = sõnumiSisu; // loeb käasureal kasutajanime
                        sõnumiSisu = args[jälgimiseks++];
                        ClientOperations.sendMessage(in,out,saajaID,sõnumiSisu);

                        break;
                    case SEND_FILE_TO_SERVER:
                        ClientOperations.sendFile(in,out,sõnumiSisu);

                        break;

                    case GET_FILE: // küsib serverilt faili
                        ClientOperations.getFile(in,out,sõnumiSisu);

                        break;
                }
            }
        } finally {
            System.out.println("\nServerist lahti ühendatud.");
        }
    }
    public static void writeToFileSaved(String filename, String message) throws IOException { // kirjutab sõnumeid mida saadi
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(message);
            writer.newLine(); // Lisa uus rida .e. taane
        }
    }
}
