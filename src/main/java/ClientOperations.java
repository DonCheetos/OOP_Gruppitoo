import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientOperations {//kliendi tegemised oma klassi

    public static void sendEcho(DataInputStream in, DataOutputStream out,String sõnumiSisu) throws IOException {
        //out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_ECHO));
        //int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        System.out.println(ResponseCodes.SEND_ECHO + ": \"" + sõnumiSisu + "\"");
        out.writeUTF(sõnumiSisu);
        System.out.println("Serverilt saadud echo-sõnum: \"" + in.readUTF() + "\".");
    }
    public static void getFile(DataInputStream in, DataOutputStream out,String sõnumiSisu) throws IOException {
        //out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_FILE));
        //int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        System.out.println(ResponseCodes.GET_FILE + ": \"" + sõnumiSisu + "\"");
        out.writeUTF(sõnumiSisu); // saadab faili nime

        ResponseCodes tagastusKood2 = ResponseCodes.getCode(in.readInt());
        if (tagastusKood2 == ResponseCodes.FILE_NOT_FOUND) {
            System.err.println("Faili (\"" + sõnumiSisu + "\") ei leitud.");
            return;
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
    public static void getMessage(DataInputStream in, DataOutputStream out,String kasutajaID) throws IOException {
        //out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_MESSAGE_BACKLOG));
        //int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        out.writeUTF(kasutajaID);
        int sõnumiteArv = in.readInt(); // sõnumite arv
        System.out.println("Saadud sõnumite arv: " + sõnumiteArv + ".");
        for (int i = 0; i < sõnumiteArv; i++) { // loeb kõik sõnumeid
            String sõnum = in.readUTF();
            System.out.println("Saadud sõnum: \"" + sõnum + "\".");
            try { // Kirjutab sõnumid faili, mille nimeks saab "'kasutajaID'_msg.txt", praegu kasutatakse seda, et saaks lihtsamini kuvada sõnumeid GUI-s
                FileUtil.writeToFileSave(kasutajaID+"_msg.txt", sõnum);
                System.out.println("Sõnum on lisatud faili.");
            } catch (IOException e) {
                System.err.println("Faili kirjutamisel tekkis viga: " + e.getMessage());
            }
        }
    }
    public static void sendMessage(DataInputStream in, DataOutputStream out,String saajaID,String sõnumiSisu) throws IOException {
        //out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_MESSAGE_TO_BACKLOG));
        //int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras//todo ülemised kaks rida kasutada valimise asemel read 57 ja 48 sama asi ka teistega
        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG + ": \n    Sõnumi saaja: \"" + saajaID + "\"");
        out.writeUTF(saajaID); // kasutaja määramine
        in.readInt();
        System.out.println("    Sõnumi sisu: \"" + sõnumiSisu + "\"");
        out.writeUTF(sõnumiSisu); // kirjutab sõnumi välja
    }
    public static void sendFile(DataInputStream in, DataOutputStream out,String failinimi) throws IOException {
        //out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_FILE_TO_SERVER));
        //int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        out.writeUTF(failinimi);//failinimi
        try(FileInputStream fis = new FileInputStream(failinimi)){
            byte[] fail=fis.readAllBytes();
            out.writeInt(fail.length);//failisuurus
            System.out.println("Saatsin faili "+failinimi+" suurusega:"+fail.length);
            try{
                out.write(fail);

            }catch (SocketException e){
                System.err.println("kirjutamise viga, faili:"+failinimi+" ei õnnestunud kirjutada!");
                //throw e;
            }

        }
    }
    public static int tagastuskoodiLugemine(DataInputStream in) throws IOException {
        int tagastusKood = in.readInt(); // oleku kontrolliks
        if (tagastusKood < 0) { // ERROR
            System.err.println("Tagastuskood: " + ResponseCodes.getCode(tagastusKood) + ".");
            throw new RuntimeException("Tagastuskoodi viga: " + ResponseCodes.getCode(tagastusKood));
        }
        System.out.println("Tagastuskood: OK.");
        return tagastusKood;
    }

    // TODO: Lahendused peab parandama, kuna sisendi tagaside(vale parool, kasutaja võetud jne) saamiseks ei saa päringut teha läbi client.java
    public static int createUser(String kasutajanimi, String parool) throws IOException {
        try (Socket socket = new Socket("localhost", 1337);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            out.writeInt(1);
            out.writeInt(ResponseCodes.getValue(ResponseCodes.CREATE_USER));

            out.writeUTF(kasutajanimi);
            int tagastusKood =  in.readInt();
            if (tagastusKood == ResponseCodes.getValue(ResponseCodes.USER_TAKEN)) return ResponseCodes.getValue(ResponseCodes.USER_TAKEN);
            out.writeUTF(parool);

            tagastusKood = in.readInt();
            return tagastusKood;
        }
    }

    public static int checkUser(String kasutajanimi, String parool) throws IOException {
        try (Socket socket = new Socket("localhost", 1337);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            out.writeInt(1);
            out.writeInt(ResponseCodes.getValue(ResponseCodes.CHECK_USER));

            out.writeUTF(kasutajanimi);
            int tagastusKood =  in.readInt();
            if (tagastusKood == ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND)) return ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND);
            out.writeUTF(parool);

            tagastusKood = in.readInt();
            return tagastusKood;
        }
    }
}
