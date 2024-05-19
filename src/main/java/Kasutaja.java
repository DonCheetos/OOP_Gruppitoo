import java.util.ArrayList;

public class Kasutaja {
    private String nimi;
    private ArrayList<String> sõnumid;

    public Kasutaja(String nimi, ArrayList<String> sõnumid) {
        this.nimi = nimi;
        this.sõnumid = sõnumid;
    }

    public String getNimi() {
        return nimi;
    }

    public ArrayList<String> getSõnumid() {
        return sõnumid;
    }
    public void lisaSõnum(String sõnum){
        this.sõnumid.add(sõnum);
    }
}
