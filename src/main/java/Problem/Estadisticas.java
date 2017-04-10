package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class Estadisticas implements InterfazEstadisticas{

    private double minimumIndividualCost;
    
    public Estadisticas(double cost) {
        minimumIndividualCost = cost;
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
    public void fitness(ArrayList<InterfazTraza> t) {
        Double costeIndividuo = costeIndividuo(t);
        System.out.println("Coste del individuo: " + costeIndividuo);
        Double moveL = 0d;
        for (int i=0; i<t.size(); i++) {
            //Double fitness = 1 - (costeIndividuo / (t.get(i).tamTrace() * t.get(i).getNumRepeticiones() + this.minimumIndividualCost * t.size()));
            moveL = moveL + (t.get(i).tamTrace() * t.get(i).getNumRepeticiones());
        }
        Double fitness = 1 - (costeIndividuo / (moveL + this.minimumIndividualCost * t.size()));
        System.out.println("Fitness del individuo: " + fitness);
    }
}
