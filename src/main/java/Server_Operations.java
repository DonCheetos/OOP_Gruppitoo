import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Server_Operations { // serveri operatsioonide jaoks class

    // server echo
    public static void sendEcho(DataInputStream in, DataOutputStream out, int mitmesKlient) throws IOException {
        System.out.println(ResponseCodes.SEND_ECHO);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));

        String echoSõnum = in.readUTF();
        System.out.println(mitmesKlient + 1 + ". klient saatis sõnumi: \"" + echoSõnum + "\".");

        out.writeUTF(echoSõnum);
    }

    // server saadab faili kasutajale
    public static void getFile(DataInputStream in, DataOutputStream out, int mitmesKlient) throws IOException {
        System.out.println(ResponseCodes.GET_FILE);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));

        String failiNimi = in.readUTF();
        System.out.println(mitmesKlient + 1 + ". klient tahab saada faili: \"" + failiNimi + "\".");

        try (InputStream failStream = new FileInputStream(failiNimi)) {
            out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));
            byte[] fail = failStream.readAllBytes();
            out.writeInt(fail.length);
            out.write(fail);
        } catch (FileNotFoundException e) {
            System.err.println(mitmesKlient + 1 + ". kliendile saadetakse veateade, kuna faili (\"" + failiNimi + "\") ei leitud.");
            out.writeInt(ResponseCodes.getValue(ResponseCodes.FILE_NOT_FOUND));
        }
    }

    // server saadab kasutajale salvestaud sõnumi(d)
    public static void getMessageBacklog(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String, Kasutaja> kasutajatelist) throws IOException {
        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

        String kasutajaID = in.readUTF();
        System.out.println("Edastan kliendile (" + kasutajaID + ") sõnumi(d), mis teised on talle vahepeal saatnud.");
        if (!kasutajatelist.containsKey(kasutajaID)) {
            System.err.println("Ei leidnud kasutajat!");
            out.writeInt(0);
            return;
        }
        List<String> kasutajaleSaadetudSõnumid = kasutajatelist.get(kasutajaID).getSõnumid();
        if (kasutajaleSaadetudSõnumid == null) {
            out.writeInt(0);
            return;
        }

        int sõnumiteKogus = kasutajaleSaadetudSõnumid.size();
        out.writeInt(sõnumiteKogus);
        System.out.println(mitmesKlient + ". kliendile on saadetud " + sõnumiteKogus + " sõnum(it).");

        for (String sõnum : kasutajaleSaadetudSõnumid)
            out.writeUTF(sõnum);

        kasutajaleSaadetudSõnumid.clear(); // puhastab listi, kuna kõik sõnumid on loetud
    }

    // server salvestab sihtkasutajale sõnumeid
    public static void sendMessageToBacklog(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String, Kasutaja> kasutajatelist) throws IOException {
        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

        String sihtKasutaja = in.readUTF();
        System.out.println("Sihkasutaja: " + sihtKasutaja + ".");


        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras, kasutaja olemas
        String sõnum = in.readUTF();

        //kasutajatelist.get(kasutajatelist.indexOf(new Kasutaja(sihtKasutaja,new ArrayList<>()))).lisaSõnum(sõnum);
        if (!kasutajatelist.containsKey(sihtKasutaja)) {
            kasutajatelist.put(sihtKasutaja, new Kasutaja(sihtKasutaja, new ArrayList<>()));
        }
        System.out.println(kasutajatelist);

        kasutajatelist.get(sihtKasutaja).lisaSõnum(sõnum);
        System.out.println("Salvestasin sõnumi.");
        System.out.println(sihtKasutaja + ", sõnum: \"" + sõnum + "\".");
    }

    // server salvestab faili "received/" kasusta
    public static void sendFileToServer(DataInputStream in, DataOutputStream out, int mitmesKlient) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras
        String sisenevaFailiNimi = in.readUTF();
        System.out.println("sain kasutaja (" + mitmesKlient + ") faili: '" + sisenevaFailiNimi + "' kirjutan kausta");
        int lastIndex = sisenevaFailiNimi.lastIndexOf('/');
        if (lastIndex == -1) {
            lastIndex = sisenevaFailiNimi.lastIndexOf('\\');
        }
        String filename = sisenevaFailiNimi.substring(lastIndex + 1);

        int failisuurus = in.readInt();
        try (FileOutputStream fos = new FileOutputStream("received/" + filename)) {
            fos.write(in.readNBytes(failisuurus));
            out.writeInt(0);
        } catch (IOException e) {
            System.err.println("Ei kirjutanud edukalt!");
            System.err.println(e.getMessage());
            out.writeInt(-5);
        }
    }

    public static void createUser(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String, String> kasutajaInfo, Map<String, Kasutaja> kasutajatelist) throws IOException {
        String kasutaja = in.readUTF();
        if (kasutajaInfo.containsKey(kasutaja)) {
            out.writeInt(ResponseCodes.getValue(ResponseCodes.USER_TAKEN));
            System.err.println(mitmesKlient + ". kasutajanimi oli võetud, ei registeeritud");
            return;
        }
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));
        String parool = in.readUTF();
        String paroolRäsi = hashPassword(parool);
        kasutajaInfo.put(kasutaja, paroolRäsi); // kasutaja salvestamine
        if (!kasutajatelist.containsKey(kasutaja))
            kasutajatelist.put(kasutaja, new Kasutaja(kasutaja, new ArrayList<>()));
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));
        System.out.println(mitmesKlient + ". registeeriti kasutaja nimega: \"" + kasutaja + "\"");
    }

    public static void checkUser(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String, String> kasutajaInfo) throws IOException {
        String kasutaja = in.readUTF();
        if (!kasutajaInfo.containsKey(kasutaja)) {
            out.writeInt(ResponseCodes.getValue(ResponseCodes.USER_NOT_FOUND));
            System.err.println(mitmesKlient + ". kasutaja \"" + kasutaja + "\" ei eksisteeri, annb veateate");
            return;
        }
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));
        String parool = in.readUTF();
        String paroolRäsi = hashPassword(parool);
        if (kasutajaInfo.get(kasutaja).equals(paroolRäsi)) {
            out.writeInt(ResponseCodes.getValue(ResponseCodes.OK));
        } else {
            out.writeInt(ResponseCodes.getValue(ResponseCodes.FALSE_PASSWORD)); // vale parool
        }
    }

    public static String hashPassword(String password) { // parooli räsimine
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
