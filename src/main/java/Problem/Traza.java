package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class Traza implements InterfazTraza{
    private ArrayList<Integer> tareas;
    private double score;

    public Traza() {
        this.tareas = new ArrayList<>();
    }
        
    public ArrayList<Integer> getTareas() {
        return tareas;
    }

    public void setTareas(ArrayList<Integer> tareas) {
        this.tareas = tareas;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public int tamTrace() {
        return tareas.size();
    }

    @Override
    public void anadirTarea(int t) {
        tareas.add(t);
    }

    @Override
    public Integer leerTarea(int p) {
        if (p < tareas.size()){
            return tareas.get(p);
        }else{
            return null;
        }
    }
    
    @Override
    public Double getHeuristica(int pos) {
        double r;
        if (pos >= tareas.size()) {
            r = 0;
        } else {
            r = tareas.size() - pos;
        }
        return r;
    }

    //Ya acabé la traza. La última posicion quedaría fuera. 
    @Override
    public boolean procesadoTraza(int pos) {
        return pos >= tareas.size();
    }
    
    public void print() {
        System.out.println();
        for (int i = 0; i < tareas.size(); i++) {
            System.out.print(tareas.get(i) + " ");
        }
        System.out.println();
    }
}
