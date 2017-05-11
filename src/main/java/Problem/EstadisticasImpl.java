package Problem;

import Problem.NState.State;
import es.usc.citius.hipster.model.impl.WeightedNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author marti
 */
public class EstadisticasImpl implements InterfazEstadisticas{

    private final double minimumIndividualCost;
    private double totalEventosLog;  
    private Double costeIndividuo;
    
    public EstadisticasImpl(double cost) {
        this.minimumIndividualCost = cost;
        this.totalEventosLog = 0d;
    }
    
    @Override
    public Double costeTraza(ArrayList<Traza> t, int pos) {
        return t.get(pos).getScore();
    }
        
    @Override
    public Double costeIndividuo(ArrayList<InterfazTraza> t){
        Double aux = 0d;
        for (int i=0; i<t.size(); i++) {
            //Obtenemos el coste de la traza y su número de repeticiones y lo anadimos
            aux = aux + t.get(i).getScore() * t.get(i).getNumRepeticiones();
        }
        return aux;
    }
    
    @Override
    public Double fitness(ArrayList<InterfazTraza> t) {
        //Obtenemos el coste 
        costeIndividuo = costeIndividuo(t);
        //Realizamos el sumatorio para todas la trazas del log
        for (int i=0; i<t.size(); i++) {
            //System.out.println("TotalEventosLog: " + totalEventosLog + " + " + t.get(i).tamTrace() + " *" + t.get(i).getNumRepeticiones());
            this.totalEventosLog = this.totalEventosLog + (t.get(i).tamTrace() * t.get(i).getNumRepeticiones());
        }
        //Obtenemos el fitness
        Double fitness = 1 - (costeIndividuo / (this.totalEventosLog + this.minimumIndividualCost * t.size()));
        return fitness;
    }
    
    //Calculamos el número de tareas que tienen el mismo contexto que el prefijo
    public Integer tareasPrefijo(ArrayList<InterfazTraza> t, ArrayList<Integer> prefijo) {
        HashSet<Integer> actividades = new HashSet<Integer>();

        //Para todas la trazas del log
        for (int i = 0; i < t.size(); i++) {
            //En un principio ambos contextos son iguales
            boolean iguales = true;
            int j = 0;
            //Mientras no acabemos las tareas de la traza o del prefijo
            while (j < t.get(i).tamTrace() && j < prefijo.size()) {
                //Comparamos si ambas tareas son distintas
                if (t.get(i).leerTarea(j).intValue() != prefijo.get(j).intValue()) {
                    //Si son distintas no tienen el mismo contexto
                    iguales = false;
                }
                j++;
            }
            //Si son iguales y existe la siguientes tarea de la traza
            if (iguales && j < t.get(i).tamTrace()) {
                //Añadimos la tarea sólo en caso de que no se encuentre añadida
                actividades.add(t.get(i).leerTarea(j));
            }
        }
        //System.out.println("ActividadesMismoContexto: " +actividades);
        return actividades.size();
    }
        
    @Override
    public Double precision(ArrayList<InterfazTraza> t, ArrayList<WeightedNode> nodosSalida) {
        ArrayList<ArrayList<State>> tareasActivasEstado = tareasActivasEstado(nodosSalida);
        Double subPrecission = 0d;
        //Para todas las trazas del log
        for (int i=0; i < t.size(); i++) {
            //Array para guardar las tareas del prefijo
            ArrayList<Integer> prefijo = new ArrayList<Integer>();
            //Para todas las tareas de la traza
            for (int j=0; j < t.get(i).tamTrace(); j++) {
                //Añadimos al prefijo la tarea anterior a la procesada
                if (j>0) prefijo.add(t.get(i).leerTarea(j-1));
                //Calculamos el contexto del prefijo
                double enL = this.tareasPrefijo(t, prefijo);
                //System.out.println("Subprecision = "+ subPrecission + " + " + t.get(i).getNumRepeticiones() +" * ("+ enL + " / " + tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size() +" )");
                //Realizamos el sumatorio
                subPrecission = subPrecission + t.get(i).getNumRepeticiones() * (enL / tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size());  
            }
            //System.out.println();
        }
        //Obtenemos la precisión
        Double precission = 1 / this.totalEventosLog * subPrecission;
        //System.out.println("Precision = 1 / " + totalEventosLog + " * " + subPrecission);
        return precission;
    }

    //Cálculo de las tareas activas en cada estado   
    public ArrayList<ArrayList<State>> tareasActivasEstado(ArrayList<WeightedNode> nodosSalida) {       
        ArrayList<ArrayList<State>> tareasActivasEstado = new ArrayList<ArrayList<State>>();
        for (int i = 0; i < nodosSalida.size(); i++) {
            ArrayList<NState.State> tareasEstadoTraza = new ArrayList<NState.State>();
            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            WeightedNode node2 = (WeightedNode) it2.next();
            NState.State s2 = (NState.State) node2.state();
            tareasEstadoTraza.add(s2);
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NState.State s = (NState.State) node.state();
                tareasEstadoTraza.add(s);
            }
            tareasActivasEstado.add(tareasEstadoTraza);
        }
        return tareasActivasEstado;
    }

    @Override
    public Double getCoste() {
        return this.costeIndividuo;
    }
}
