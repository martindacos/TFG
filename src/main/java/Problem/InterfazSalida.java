package Problem;

import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazSalida {
    /*Función que imprime el camino más corto para el modelo, almacenado 
    en un único nodo*/
    void minimumSalidaVisual(WeightedNode nodo);
    //Función que imprime la salida de los alineamientos 
    void salidaVisual(ArrayList<WeightedNode> nodosSalida, Readers r, 
            ArrayList<ArrayList<NState.State>> tareasActivasEstado);
}
