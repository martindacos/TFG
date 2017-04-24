package Problem;

import Problem.NState.State;
import java.util.ArrayList;
import java.util.HashSet;

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
            //System.out.println("TotalEventosLog: " + totalEventosLog + " + " + t.get(i).tamTrace() + " *" + t.get(i).getNumRepeticiones());
            this.totalEventosLog = this.totalEventosLog + (t.get(i).tamTrace() * t.get(i).getNumRepeticiones());
        }
        Double fitness = 1 - (costeIndividuo / (this.totalEventosLog + this.minimumIndividualCost * t.size()));
        return fitness;
    }
    
    public Integer tareasPrefijo(ArrayList<InterfazTraza> t, ArrayList<Integer> prefijo) {
        HashSet<Integer> actividades = new HashSet<Integer>();

        for (int i = 0; i < t.size(); i++) {
            boolean iguales = true;
            int j = 0;
            while (j < t.get(i).tamTrace() && j < prefijo.size()) {
                if (t.get(i).leerTarea(j).intValue() != prefijo.get(j).intValue()) {
                    iguales = false;
                }
                j++;
            }
            if (iguales && j < t.get(i).tamTrace()) {
                actividades.add(t.get(i).leerTarea(j));
            }
        }
        System.out.println("ActividadesMismoContexto: " +actividades);
        return actividades.size();
    }
        
    @Override
    public Double precision(ArrayList<InterfazTraza> t, ArrayList<ArrayList<State>> tareasActivasEstado) {
        Double subPrecission = 0d;
        for (int i=0; i < t.size(); i++) {
            ArrayList<Integer> prefijo = new ArrayList<Integer>();
            for (int j=0; j < t.get(i).tamTrace(); j++) {
                if (j>0) prefijo.add(t.get(i).leerTarea(j-1));
                double enL = this.tareasPrefijo(t, prefijo);
                System.out.println("Subprecision = "+ subPrecission + " + " + t.get(i).getNumRepeticiones() +" * ("+ enL + " / " + tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size() +" )");
                subPrecission = subPrecission + t.get(i).getNumRepeticiones() * (enL / tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size());  
            }
            System.out.println();
        }
        Double precission = 1 / this.totalEventosLog * subPrecission;
        System.out.println("Precision = 1 / " + totalEventosLog + " * " + subPrecission);
        return precission;
    }
}
