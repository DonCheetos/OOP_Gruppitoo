import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientOperations {//kliendi tegemised oma klassi
    public static void sendEcho(DataInputStream in, DataOutputStream out,String sõnumiSisu) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_ECHO));
        int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        System.out.println(ResponseCodes.SEND_ECHO + ": \"" + sõnumiSisu + "\"");
        out.writeUTF(sõnumiSisu);
        System.out.println("Serverilt saadud echo-sõnum: \"" + in.readUTF() + "\".");
    }
    public static void getFile(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_FILE));
        int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
    }
    public static void getMessage(DataInputStream in, DataOutputStream out,String kasutajaID) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.GET_MESSAGE_BACKLOG));
        int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
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
    }
    public static int sendMessage(DataInputStream in, DataOutputStream out,String saajaID) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_MESSAGE_TO_BACKLOG));
        int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
        System.out.println(ResponseCodes.SEND_MESSAGE_TO_BACKLOG + ": \n    Sõnumi saaja: \"" + saajaID + "\"");
        out.writeUTF(saajaID); // kasutaja määramine
        return in.readInt();
    }
    public static void sendFile(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.SEND_FILE_TO_SERVER));
        int tagastuskood=tagastuskoodiLugemine(in);//kontrolliks kas on korras
    }
    public static int tagastuskoodiLugemine(DataInputStream in) throws IOException {
        int tagastusKood = in.readInt(); // oleku kontrolliks
        if (tagastusKood < 0) { // ERROR
            System.out.println("Tagastuskood: " + ResponseCodes.getCode(tagastusKood) + ".");
            throw new RuntimeException("Tagastuskoodi viga: " + ResponseCodes.getCode(tagastusKood));
        }
        System.out.println("Tagastuskood: OK.");
        return tagastusKood;
    }
}
