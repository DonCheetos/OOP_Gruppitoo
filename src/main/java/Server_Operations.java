import java.io.*;
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
            System.out.println(mitmesKlient + 1 + ". kliendile saadetakse veateade, kuna faili (\"" + failiNimi + "\") ei leitud.");
            out.writeInt(ResponseCodes.getValue(ResponseCodes.FILE_NOT_FOUND));
        }
    }

    // server saadab kasutajale salvestaud sõnumi(d)
    public static void getMessageBacklog(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String,Kasutaja> kasutajatelist) throws IOException {
        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

        String kasutajaID = in.readUTF();
        System.out.println("Edastan kliendile (" + kasutajaID + ") sõnumi(d), mis teised on talle vahepeal saatnud.");
        if(kasutajatelist.containsKey(kasutajaID)){
            System.out.println("ei leidnud kasutajat");
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
    public static void sendMessageToBacklog(DataInputStream in, DataOutputStream out, int mitmesKlient, Map<String,Kasutaja> kasutajatelist) throws IOException {
        System.out.println(ResponseCodes.GET_MESSAGE_BACKLOG);
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras

        String sihtKasutaja = in.readUTF();
        System.out.println("Sihkasutaja: " + sihtKasutaja + ".");


        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras, kasutaja olemas
        String sõnum = in.readUTF();

        //kasutajatelist.get(kasutajatelist.indexOf(new Kasutaja(sihtKasutaja,new ArrayList<>()))).lisaSõnum(sõnum);
        if(!kasutajatelist.containsKey(sihtKasutaja)){
            kasutajatelist.put(sihtKasutaja,new Kasutaja(sihtKasutaja,new ArrayList<>()));
        }
        kasutajatelist.get(sihtKasutaja).lisaSõnum(sõnum);
        System.out.println("Salvestasin tekstisisu.");
        System.out.println(sihtKasutaja + ", sõnum: \"" + sõnum + "\".");
    }

    // server salvestab faili "received/" kasusta
    public static void sendFileToServer(DataInputStream in, DataOutputStream out, int mitmesKlient) throws IOException {
        out.writeInt(ResponseCodes.getValue(ResponseCodes.OK)); // kõik korras
        String sisenevaFailiNimi = in.readUTF();
        System.out.println("sain kasutaja (" + mitmesKlient + ") faili: '"+sisenevaFailiNimi+"' kirjutan kausta");
        int lastIndex = sisenevaFailiNimi.lastIndexOf('/');
        if (lastIndex == -1) {
            lastIndex = sisenevaFailiNimi.lastIndexOf('\\');
        }
        String filename = sisenevaFailiNimi.substring(lastIndex + 1);

        int failisuurus=in.readInt();
        try(FileOutputStream fos = new FileOutputStream("received/"+filename)){
            fos.write(in.readNBytes(failisuurus));
            out.writeInt(0);
        }catch (IOException e){
            System.out.println("ei kirjutanud edukalt");
            System.out.println(e.getMessage());
            out.writeInt(-5);
        }
    }

}
