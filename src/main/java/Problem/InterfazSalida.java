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
    //Función que puede mostrar/ocultar la salida
    void setVisible(boolean visible);
    //Función que imprime el modelo
    void imprimirModelo(CMIndividual ind);
    //Función que almacena las trazas y su nodo solución para su impresión
    void ActualizarTrazas(InterfazTraza trace, WeightedNode nodo);
}
