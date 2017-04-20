package Problem;

import Problem.NState.State;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazEstadisticas {
    
    Double costeIndividuo(ArrayList<InterfazTraza> t);
    Double costeTraza(ArrayList<Traza> t, int pos);
    Double fitness(ArrayList<InterfazTraza> t);
    Double precission(ArrayList<InterfazTraza> t, ArrayList<ArrayList<State>> tareasActivasEstado); 
}
