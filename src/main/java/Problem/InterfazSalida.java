package Problem;

import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazSalida {
    /*Función que imprime el camino más corto para el modelo, almacenado 
    en un único nodo*/
    void minimumSalidaVisual(WeightedNode nodo, Double coste);
    //Función que imprime la salida de los alineamientos 
    void salidaVisual(ArrayList<WeightedNode> nodosSalida, Readers r);
    //Función que imprime las estadísticas del modelo (fitness, precission, coste, tiempoCálculo)
    void estadisticasModelo(CMIndividual ind, Double coste, long tiempo);
}
