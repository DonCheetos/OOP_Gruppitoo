import java.util.ArrayList;

public class Kasutaja {
    private String nimi;
    private ArrayList<String> sõnumid;
    private boolean kasOnUusi=true;

    public Kasutaja(String nimi, ArrayList<String> sõnumid) {
        this.nimi = nimi;
        this.sõnumid = sõnumid;
    }

    public String getNimi() {
        return nimi;
    }

    public ArrayList<String> getSõnumid() {
        this.kasOnUusi=false;
        return sõnumid;
    }
    public void lisaSõnum(String sõnum){
        this.kasOnUusi=true;
        this.sõnumid.add(sõnum);
    }
}
