package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazEstadisticas {
    
    Double costeIndividuo(ArrayList<InterfazTraza> t);
    Double costeTraza(ArrayList<Traza> t, int pos);
}
