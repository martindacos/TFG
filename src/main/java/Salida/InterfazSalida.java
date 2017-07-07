package Salida;

import Problem.InterfazTraza;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.model.AbstractNode;
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

    //Función que imprime las estadísticas del modelo (fitness, precission, coste, tiempoCálculo)
    void estadisticasModelo(CMIndividual ind, double coste, long tiempo, double memoria);

    //Función que puede mostrar/ocultar la salida
    void setVisible(boolean visible);

    //Función que imprime el modelo
    void imprimirModelo(CMIndividual ind);

    //Función que imprime las trazas y su nodo solución (alineamiento)
    void ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad);

    //Función que imprime el número total de trazas a alinear
    public void setTotalTrazas(int size);
}
