package es.usc.citius.aligments.estadisticas;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState;
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

    Double fitnessNuevo(ArrayList<InterfazTraza> t);

    //Cálculo de la precisión del modelo
    Double precission(ArrayList<InterfazTraza> t, ArrayList<AbstractNode> nodosSalida);

    Double precission(ArrayList<InterfazTraza> t);

    //Función que devuelve el coste de un individuo
    Double getCoste();

    void setTiempoCalculo(Long tiempo);

    Long getTiempoCalculo();

    void setMemoriaConsumida(double memoria);

    void setDiferentStates(int v);

    int getDiferentStates();

    double getMemoriaConsumida();

    Double getFitness();

    Double getPrecission();

    void countTypeMovs(NState.StateMove action);

    String getStatMovs();

    void resetMovs();

    String getAllStatMovs();

    void menorCamino(int n);
}
