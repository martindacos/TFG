package Problem;

import static Problem.NState.StateMove.OK;
import static Problem.NState.StateMove.SKIP;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author marti
 */
public class SalidaTerminalImpl implements InterfazSalida {

    @Override
    public void minimumSalidaVisual(WeightedNode nodo, Double coste) {
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
            if (node.action().equals(SKIP)) {
                System.out.println("\t>>\t" + s.getTarea());
            }
        }
        System.out.println();     
        System.out.println("Coste del camino más corto: " + coste);
    }

    @Override
    public void salidaVisual(ArrayList<WeightedNode> nodosSalida, Readers r) {
        for (int i = 0; i < nodosSalida.size(); i++) {
            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            System.out.println("***************************");
//            System.out.println(nodosSalida.get(i).path());
            System.out.println();
            System.out.println("---------SALIDA VISUAL----------");
            System.out.println("\tTRAZA\tMODELO");
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NState.State s = (NState.State) node.state();
                if (node.action().equals(OK)) {
                    System.out.println("\t" + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "\t" + s.getTarea());
                } else if (node.action().equals(SKIP)) {
                    System.out.println("\t>>\t" + s.getTarea());
                } else {
                    System.out.println("\t" + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "\t>>");
                }
            }
            System.out.println();
            System.out.println("Coste del alineamiento = " + r.getTrazaPos(i).getScore());          
            System.out.println("Tiempo de cálculo del alineamiento = " + r.getTrazaPos(i).getTiempoC() + " ms");
        }
    }

    @Override
    public void estadisticasModelo(CMIndividual ind, Double coste, long tiempo) {
        System.out.println();
        System.out.println("********** ESTADÍSTICAS DEL MODELO **************");
        System.out.println("Coste del modelo: " + coste);
        System.out.println("Fitness del modelo: " + ind.getFitness().getCompleteness());
        System.out.println("Precission del modelo: " + ind.getFitness().getPreciseness());
        System.out.println("Tiempo total de cálculo = " + tiempo + " ms");
    }

    @Override
    public void setVisible(boolean visible) {
        //TODO sin esto non se debería imprimir nada (ifs por ejemplo)
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void imprimirModelo(CMIndividual ind) {
        ind.print();
    }

    @Override
    public void ActualizarTrazas(InterfazTraza trace, WeightedNode nodo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
