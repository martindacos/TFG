package Problem;

import Problem.NState.State;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class Estadisticas implements InterfazEstadisticas{

    private double minimumIndividualCost;
    private double totalEventosLog;
    
    public Estadisticas(double cost) {
        this.minimumIndividualCost = cost;
        this.totalEventosLog = 0d;
    }
    
    @Override
    public Double costeTraza(ArrayList<Traza> t, int pos) {
        return t.get(pos).getScore();
    }
        
    @Override
    //Obtenemos el coste del individuo en base a las trazas del fichero
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
        Double costeIndividuo = costeIndividuo(t);
        System.out.println("Coste del individuo: " + costeIndividuo);
        for (int i=0; i<t.size(); i++) {
            this.totalEventosLog = this.totalEventosLog + (t.get(i).tamTrace() * t.get(i).getNumRepeticiones());
        }
        Double fitness = 1 - (costeIndividuo / (this.totalEventosLog + this.minimumIndividualCost * t.size()));
        return fitness;
    }
    
    public Double tareasPrefijo(ArrayList<InterfazTraza> t, ArrayList<Integer> prefijo) {
        //TO DO : revisar que a implementación é a correcta
        double tareasPrefijo = 0d;
        for (int i=0; i < t.size(); i++) {
            boolean iguales = true;
            for (int j=0; j < t.get(i).tamTrace() && j < prefijo.size(); j++) {
                if (t.get(i).leerTarea(j) != prefijo.get(j)) iguales = false;
            }
            if (iguales) tareasPrefijo = tareasPrefijo + prefijo.size();
        }
        System.out.println("Tareas prefijo: " + tareasPrefijo);
        return tareasPrefijo;
    }
        
    @Override
    public Double precission(ArrayList<InterfazTraza> t, ArrayList<ArrayList<State>> tareasActivasEstado) {
        Double subPrecission = 0d;
        for (int i=0; i < t.size(); i++) {
            ArrayList<Integer> prefijo = new ArrayList<Integer>();
            for (int j=0; j < t.get(i).tamTrace(); j++) {
                prefijo.add(t.get(i).leerTarea(j));
                double enL = this.tareasPrefijo(t, prefijo);
                subPrecission = subPrecission + enL / tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size();   
            }
        }
        Double precission = 1 / this.totalEventosLog * subPrecission;
        return precission;
    }
}
