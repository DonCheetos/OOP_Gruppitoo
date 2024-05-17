import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Grupp {
    //private String gruppiNimi;

    private Map<String,ArrayList<Kasutaja>> gruppiliikmed=new HashMap<>();
    public Grupp() {
    }
    public boolean saadaGruppile(String gruppiNimi, String sõnum){
        if (gruppiliikmed.containsKey(gruppiNimi)){
            for(Kasutaja saaja:gruppiliikmed.get(gruppiNimi)){
                saaja.lisaSõnum(sõnum);
            }
            return true;
        }else return false;
    }
    public boolean lisaGrupp(String gruppiNimi){
        if(!gruppiliikmed.containsKey(gruppiNimi)){
            gruppiliikmed.put(gruppiNimi,new ArrayList<>());
            return true;
        }
        return false;
    }
    public boolean lisaLiige(String gruppinimi,Kasutaja kasutaja){
        if (gruppiliikmed.containsKey(gruppinimi) & !gruppiliikmed.get(gruppinimi).contains(kasutaja)){
            gruppiliikmed.get(gruppinimi).add(kasutaja);
            kasutaja.lisaSõnum("Lisatud gruppi:"+gruppinimi);
            return true;
        }
        return false;
    }
    public boolean eemaldaLiige(String gruppinimi,Kasutaja kasutaja){
        if(gruppiliikmed.containsKey(gruppinimi)){
            if(gruppiliikmed.get(gruppinimi).contains(kasutaja)){
                kasutaja.lisaSõnum("Eemaldatud gruppist:"+gruppinimi);
                gruppiliikmed.get(gruppinimi).remove(kasutaja);
            }
        }
        return false;
    }
    public boolean eemaldaGrupp(String gruppinimi){
        if(gruppiliikmed.containsKey(gruppinimi)){
            for(Kasutaja kasutaja:gruppiliikmed.get(gruppinimi)){
                kasutaja.lisaSõnum("Eemaldatud gruppist {"+gruppinimi+"} kuna grupp kustutati");
            }
            gruppiliikmed.remove(gruppinimi);
            return true;
        }
        return false;
    }
}
