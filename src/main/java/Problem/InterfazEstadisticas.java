package Problem;

import Problem.NState.State;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazEstadisticas {
    
    //Cálculo del coste total de las trazas del log
    Double costeIndividuo(ArrayList<InterfazTraza> t);
    //Coste de una traza concreta del log
    Double costeTraza(ArrayList<Traza> t, int pos);
    //Cálculo de la completitud del modelo
    Double fitness(ArrayList<InterfazTraza> t);
    //Cáluclo de la precisión del modelo
    Double precision(ArrayList<InterfazTraza> t, ArrayList<ArrayList<State>> tareasActivasEstado); 
}
