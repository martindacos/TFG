package Problem;

import static Problem.NState.StateMove.OK;
import static Problem.NState.StateMove.SKIP;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author marti
 */
public class SalidaTerminalImpl implements InterfazSalida {

    @Override
    public void minimumSalidaVisual(WeightedNode nodo) {
        Iterator it2 = nodo.path().iterator();
        //La primera iteración corresponde con el Estado Inicial
        it2.next();
        System.out.println("***************************");
//        System.out.println(nodo.path());
//        System.out.println();
        System.out.println("------SALIDA VISUAL-------");
        System.out.println("    TRAZA     MODELO");
        while (it2.hasNext()) {
            WeightedNode node = (WeightedNode) it2.next();
            NState.State s = (NState.State) node.state();
            if (node.action().equals(SKIP)) {
                System.out.println("    >>         " + s.getTarea());
            }
        }
        System.out.println();
    }

    @Override
    public void salidaVisual(ArrayList<WeightedNode> nodosSalida, Readers r, ArrayList<ArrayList<NState.State>> tareasActivasEstado) {
        for (int i = 0; i < nodosSalida.size(); i++) {
            ArrayList<NState.State> tareasEstadoTraza = new ArrayList<NState.State>();
            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            WeightedNode node2 = (WeightedNode) it2.next();
            NState.State s2 = (NState.State) node2.state();
            tareasEstadoTraza.add(s2);
            System.out.println("***************************");
//            System.out.println(nodosSalida.get(i).path());
//            System.out.println();
            System.out.println("------SALIDA VISUAL-------");
            System.out.println("    TRAZA     MODELO");
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NState.State s = (NState.State) node.state();
                tareasEstadoTraza.add(s);
                if (node.action().equals(OK)) {
                    System.out.println("    " + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "          " + s.getTarea());
                } else if (node.action().equals(SKIP)) {
                    System.out.println("    >>         " + s.getTarea());
                } else {
                    System.out.println("    " + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "          >>");
                }
            }
            System.out.println();
            System.out.println("Coste del Alineamiento " + r.getTrazaPos(i).getScore());
            tareasActivasEstado.add(tareasEstadoTraza);
        }
    }

}
