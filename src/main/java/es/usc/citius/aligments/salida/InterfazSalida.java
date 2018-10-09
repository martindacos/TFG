package es.usc.citius.aligments.salida;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 *
 * @author marti
 */
public interface InterfazSalida {

    //Función que imprime las estadísticas del modelo (fitness, precission, coste, tiempoCálculo)
    String estadisticasModelo(CMIndividual ind, double coste, long tiempo, double memoria);

    //Función que puede mostrar/ocultar la salida
    void setVisible(boolean visible);

    //Función que imprime el modelo
    void imprimirModelo(CMIndividual ind);

    //Función que imprime las trazas y su nodo solución (alineamiento)
    String ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind);

    //Función que imprime las trazas y su nodo solución (alineamiento)
    String ActualizarTrazasOld(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind);

    //Función que imprime las trazas y su nodo solución (alineamiento)
    void ActualizarTrazasReduced(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind);

    //Función que imprime el número total de trazas a alinear
    void setTotalTrazas(int size);

    String getStatMovs();

    void printCobefra(SyncReplayResult result);

    void printCobefra(PNRepResult pnRepResult);

    void printTrace(InterfazTraza trace);

    String compareResults(PNRepResult pnRepResult, Readers miReader);
}
