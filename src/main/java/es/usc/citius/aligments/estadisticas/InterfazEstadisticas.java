package es.usc.citius.aligments.estadisticas;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.Traza;
import es.usc.citius.hipster.model.AbstractNode;
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

    Double fitnessNuevo(ArrayList<InterfazTraza> t, ArrayList<AbstractNode> nodosSalida);

    //Cálculo de la precisión del modelo
    Double precission(ArrayList<InterfazTraza> t, ArrayList<AbstractNode> nodosSalida);

    //Función que devuelve el coste de un individuo
    Double getCoste();

    void setTiempoCalculo(Long tiempo);

    Long getTiempoCalculo();

    void setMemoriaConsumida(double memoria);

    double getMemoriaConsumida();

    Double getFitness();

    Double getPrecission();
}
