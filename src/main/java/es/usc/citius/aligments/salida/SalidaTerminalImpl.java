package es.usc.citius.aligments.salida;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState;
import es.usc.citius.aligments.salida.InterfazSalida;
import static es.usc.citius.aligments.problem.NState.StateMove.*;
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
    private Integer countS = 0;
    private Integer countT = 0;
    private Integer countM = 0;
    private Integer countMF = 0;

    @Override
    public String estadisticasModelo(CMIndividual ind, double coste, long tiempo, double memoria) {
        String s = "";
        if (imprimir) {
            s = s + "\n\n********** ESTADÍSTICAS DEL MODELO **************";
            int cI = (int) coste;
            s = s + "\nCoste del modelo: " + cI;
            s = s + "\nTiempo total de cálculo = " + tiempo + " ms";
            s = s + "\nFitness del modelo: " + ind.getFitness().getCompleteness();
            s = s + "\nPrecission del modelo: " + ind.getFitness().getPreciseness();
            int mI = (int) memoria;
            s = s + "\nMemoria total consumida = " + mI;
        }

        return s;
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
    public String ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind) {
        String salida = "";
        if (imprimir) {
            Iterator it2 = nodo.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            salida = salida + "\n***************************";
//            System.out.println(nodosSalida.get(i).path());
            salida = salida + "\n\n---------SALIDA VISUAL----------";
            salida = salida + "\n\tTRAZA\tMODELO";
            while (it2.hasNext()) {
                AbstractNode node;
                if (ad) {
                    node = (WeightedNode) it2.next();
                } else {
                    node = (ADStarNodeImpl) it2.next();
                }
                NState.State s = (NState.State) node.state();
                if (node.action().equals(SINCRONO)) {
                    countS++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO)) {
                    countM++;
                    salida = salida + "\n\t>>\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO_FORZADO)) {
                    countMF++;
                    salida = salida + "\n\t>>'\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else {
                    countT++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t>>";
                }
            }
            salida = salida + "\n\nCoste del alineamiento = " + trace.getScore();
            salida = salida + "\nCoste del alineamiento con repeticiones = " + trace.getScoreRepetido();
            salida = salida + "\nTiempo de cálculo del alineamiento = " + trace.getTiempoC() + " ms";
            salida = salida + "\nMemoria consuminda por el alineamiento = " + trace.getMemoriaC();
        }

        return salida;
    }

    @Override
    public String getStatMovs() {
        String s = "";
        s = s + "\nSINCRONO :" + countS;
        s = s + "\nTRAZA :" + countT;
        s = s + "\nMODELO :" + countM;
        s = s + "\nMODELO FORZADO :" + countMF;
        return s;
    }

    @Override
    public void setTotalTrazas(int size) {
        if (imprimir) {
            System.out.println("El número total de trazas es de " + size);
        }
    }

}
