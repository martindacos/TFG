package Problem;

import es.usc.citius.hipster.model.impl.WeightedNode;
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
    //Cálculo de la precisión del modelo
    Double precision(ArrayList<InterfazTraza> t, ArrayList<WeightedNode> nodosSalida);
    //Función que devuelve el coste de un individuo
    Double getCoste();
}
