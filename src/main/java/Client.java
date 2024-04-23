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
                        for (int i = 0; i < sõnumiteArv; i++) { // loeb kõik sõnumeid
                            String sõnum = in.readUTF();
                            System.out.println("Saadud sõnum: \"" + sõnum + "\".");
                            try { // Kirjutab faili ka tulemusi mille nimeks saab "'kasutajaID'_msg.txt", praegu kasutatakse seda, et saaks lihtsamini kuvada sõnumeid GUI-s
                                FileUtil.writeToFileSave(kasutajaID+"_msg.txt", sõnum);
                                System.out.println("Sõnum on lisatud faili.");
                            } catch (IOException e) {
                                System.err.println("Faili kirjutamisel tekkis viga: " + e.getMessage());
                            }
                        }

                        break;

                    case SEND_MESSAGE_TO_BACKLOG: // kirjutab mingi sõnumi kasutajale, käasureal järjekord 'requestTüüp kasutaja sõnum'
                        String saajaID = sõnumiSisu; // loeb käasureal kasutajanime
                        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG + ": \n    Sõnumi saaja: \"" + saajaID + "\"");
                        out.writeUTF(saajaID); // kasutaja määramine

                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
                        if (tagastusKood2 == ResponseCodes.USER_NOT_FOUND) { // kui sellist kasutajat ei leitud
                            System.out.println("Sellist kasutajat pole: " + saajaID + ".");
                            jälgimiseks++;//kood viskas vea kui kasutajat ei leidnud
                            break;
                        }

                        sõnumiSisu = args[jälgimiseks++];
                        System.out.println("    Sõnumi sisu: \"" + sõnumiSisu + "\"");
                        out.writeUTF(sõnumiSisu); // kirjutab sõnumi välja
                        break;
                    case SEND_FILE_TO_SERVER:
                        out.writeUTF(sõnumiSisu);//failinimi
                        try(FileInputStream fis = new FileInputStream(sõnumiSisu)){
                            byte[] fail=fis.readAllBytes();
                            out.writeInt(fail.length);//failisuurus
                            System.out.println("Saatsin faili "+sõnumiSisu+" suurusega:"+fail.length);
                            try{
                                out.write(fail);

                            }catch (SocketException e){
                                System.out.println("kirjutamise viga, faili:"+sõnumiSisu+" ei õnnestunud kirjutada!");
                                //throw e;
                            }

                        }

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

                        // Find the last index of '/' or '\\'
                        int lastIndex = sõnumiSisu.lastIndexOf('/');
                        if (lastIndex == -1) {
                            lastIndex = sõnumiSisu.lastIndexOf('\\');
                        }

                        // Extract the filename
                        String filename = sõnumiSisu.substring(lastIndex + 1);

                        try (OutputStream uusFail = new FileOutputStream("received/"+filename)) {
                            byte[] sisu = new byte[failiSuurus];
                            in.readFully(sisu);
                            uusFail.write(sisu); // kirjuta võrgust saadud andmed oma arvutisse uude faili
                            System.out.println("salvestatud.");
                        }
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
