package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazEstadisticas {
    
    public Double costeIndividuo(ArrayList<InterfazTraza> t);
    public Double costeTraza(ArrayList<Traza> t, int pos);
}
