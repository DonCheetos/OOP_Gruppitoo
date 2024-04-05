import java.io.*;
import java.net.Socket;
import java.util.Arrays;

// käsurida: 0 echo Tere writesonum 0 "Sõnum, see on sõnum" getsonum file "test.txt"
public class Client {
    public static void main(String[] args) throws IOException {
        int pordiNumber = 1337;
        int sõnumiSuurus = args.length;

        if (sõnumiSuurus < 1) {
            System.out.println("Käsureal peab olema vähemalt üks argument: kasutaja ID.\nSee peab olema alati kõige esimene argument!");
            throw new RuntimeException("Käsurea viga.");
        }
        int kasutajaID;
        try {
            kasutajaID = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Esimene argument peab olema kasutaja id! Selle asemel oli: \"" + args[0] + "\".");
            throw e;
        }

        try (Socket socket = new Socket("localhost", pordiNumber);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Ühendatud serveriga pordil: " + pordiNumber + ".");

            out.writeInt(kasutajaID); // serverile kasutaja ID andmine
            out.writeInt(sõnumiSuurus); // serverile sõnumite koguse andmine

            int jälgimiseks = 1;
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
                        jälgimiseks--; // kuna sellel käsul pole ühtegi argumenti
                        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
                        int sõnumiteArv = in.readInt(); // sõnumite arv
                        System.out.println("Saadud sõnumite arv: " + sõnumiteArv + ".");
                        for (int i = 0; i < sõnumiteArv; i++) // loeb kõik sõnumeid
                            System.out.println("Saadud sõnum: \"" + in.readUTF() + "\".");
                        break;

                    case SEND_MESSAGE_TO_BACKLOG: // kirjutab mingi sõnumi kasutajale, käasureal järjekord 'requestTüüp kasutaja sõnum'
                        int saajaID = -1;
                        try {
                            saajaID = Integer.parseInt(sõnumiSisu); // loeb käasureal kasutajanime
                        } catch (NumberFormatException e) {
                            System.out.println("Saaja ID peab olema arv. Selle asemel oli: \"" + sõnumiSisu + "\".");
                        }
                        if (saajaID < 0){
                            System.out.println("Saaja ID viga: ID=" + saajaID + ".");
                            throw new RuntimeException("ID viga: ID=" + saajaID);
                        }
                        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG + ": \n    Sõnumi saaja: ID=" + saajaID + ".");
                        out.writeInt(saajaID); // saaja ID serverile saatmine

//                        tagastusKood2 = ResponseCodes.getCode(in.readInt());
//                        if (tagastusKood2 == ResponseCodes.USER_NOT_FOUND) { // kui sellist kasutajat ei leitud
//                            System.out.println("Sellist kasutajat pole: ID=" + saajaID + ".");
//                            break;
////                        }

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
                        if (new File("src/main/resources/klient_" + kasutajaID).mkdirs())
                            System.out.println("Lõin kasutaja jaoks uue kausta: \"src/main/resources/klient_" + kasutajaID + "\".");
                        File võrguFail = new File("src/main/resources/klient_" + kasutajaID + "/võrk_" + failiNimi);
                        if (võrguFail.createNewFile())
                            System.out.println("Lõin kasutaja jaoks uue faili: \"src/main/resources/klient_" + kasutajaID + "/võrk_" + failiNimi + "\".");
                        try (OutputStream uusFail = new FileOutputStream(võrguFail, false)) {
                            byte[] sisu = new byte[failiSuurus];
                            in.readFully(sisu);
                            uusFail.write(sisu); // kirjuta võrgust saadud andmed oma arvutisse uude faili
                            System.out.println("salvestatud.");
                        } catch (IOException e) {
                            System.out.println("faili ei õnnestunud luua.");
                        }
                }
            }
        } finally {
            System.out.println("\nServerist lahti ühendatud.");
        }
    }
}
