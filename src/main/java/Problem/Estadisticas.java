package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class Estadisticas implements InterfazEstadisticas{

    public Estadisticas() {
    }
    
    @Override
    public Double costeTraza(ArrayList<Traza> t, int pos) {
        return t.get(pos).getScore();
    }
        
    @Override
    public Double costeIndividuo(ArrayList<InterfazTraza> t){
        Double aux = 0d;
        for (int i=0; i<t.size(); i++) {
            aux = aux + t.get(i).getScore();
        }
        return aux;
    }
}
