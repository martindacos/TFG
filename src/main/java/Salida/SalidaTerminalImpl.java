package Salida;

import Problem.InterfazTraza;
import Problem.NState;
import Salida.InterfazSalida;
import static Problem.NState.StateMove.*;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.impl.ADStarNodeImpl;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.Iterator;

/**
 *
 * @author marti
 */
public class SalidaTerminalImpl implements InterfazSalida {

    private boolean imprimir = true;

    @Override
    public void minimumSalidaVisual(WeightedNode nodo, Double coste) {
        if (imprimir) {
            Iterator it2 = nodo.path().iterator();
            //La primera iteración corresponde con el Estado Inicial
            it2.next();
//        System.out.println(nodo.path());
            System.out.println();
            System.out.println("---------SALIDA VISUAL----------");
            System.out.println("\tTRAZA\tMODELO");
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NState.State s = (NState.State) node.state();
                if (node.action().equals(MODELO)) {
                    System.out.println("\t>>\t" + s.getTarea());
                } else {
                    System.out.println("\t>>'\t" + s.getTarea());
                }
            }
            System.out.println();
            System.out.println("Coste del camino más corto: " + coste);
        }
    }

    @Override
    public void estadisticasModelo(CMIndividual ind, double coste, long tiempo, double memoria) {
        if (imprimir) {
            System.out.println();
            System.out.println("********** ESTADÍSTICAS DEL MODELO **************");
            int cI = (int) coste;
            System.out.println("Coste del modelo: " + cI);
            System.out.println("Tiempo total de cálculo = " + tiempo + " ms");
            System.out.println("Fitness del modelo: " + ind.getFitness().getCompleteness());
            System.out.println("Precission del modelo: " + ind.getFitness().getPreciseness());
            int mI = (int) memoria;
            System.out.println("Memoria total consumida = " + mI);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.imprimir = visible;
    }

    @Override
    public void imprimirModelo(CMIndividual ind) {
        ind.print();
    }

    @Override
    public void ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad) {
        if (imprimir) {
            Iterator it2 = nodo.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            System.out.println("***************************");
//            System.out.println(nodosSalida.get(i).path());
            System.out.println();
            System.out.println("---------SALIDA VISUAL----------");
            System.out.println("\tTRAZA\tMODELO");
            while (it2.hasNext()) {
                AbstractNode node;
                if (ad) {
                    node = (WeightedNode) it2.next();
                } else {
                    node = (ADStarNodeImpl) it2.next();
                }
                NState.State s = (NState.State) node.state();
                if (node.action().equals(SINCRONO)) {
                    System.out.println("\t" + trace.leerTarea(s.getPos() - 1) + "\t" + s.getTarea());
                } else if (node.action().equals(MODELO)) {
                    System.out.println("\t>>\t" + s.getTarea());
                } else if (node.action().equals(MODELO_FORZADO)) {
                    System.out.println("\t>>'\t" + s.getTarea());
                } else {
                    System.out.println("\t" + trace.leerTarea(s.getPos() - 1) + "\t>>");
                }
            }
            System.out.println();
            System.out.println("Coste del alineamiento = " + trace.getScore());
            System.out.println("Coste del alineamiento con repeticiones = " + trace.getScoreRepetido());
            System.out.println("Tiempo de cálculo del alineamiento = " + trace.getTiempoC() + " ms");
            System.out.println("Memoria consuminda por el alineamiento = " + trace.getMemoriaC());
        }
    }

    @Override
    public void setTotalTrazas(int size) {
        if (imprimir) {
            System.out.println("El número total de trazas es de " + size);
        }
    }

}
