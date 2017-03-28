package Problem;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class Traza implements InterfazTraza{
    private ArrayList<Integer> trace;
    private double score;

    public Traza() {
        this.trace = new ArrayList<>();
    }
        
    public ArrayList<Integer> getTrace() {
        return trace;
    }

    public void setTrace(ArrayList<Integer> trace) {
        this.trace = trace;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public int tamTrace() {
        return trace.size();
    }

    @Override
    public void anadirTarea(int t) {
        trace.add(t);
    }

    @Override
    public Integer leerTarea(int p) {
        if (p < trace.size()){
            return trace.get(p);
        }else{
            return null;
        }
    }
    
    @Override
    public Double getHeuristica(int pos) {
        double r;
        if (pos >= trace.size()) {
            r = 0;
        } else {
            r = trace.size() - pos;
        }
        return r;
    }

    //Ya acabé la traza. La última posicion quedaría fuera. 
    @Override
    public boolean procesadoTraza(int pos) {
        return pos >= trace.size();
    }
    
    public void print() {
        System.out.println();
        for (int i = 0; i < trace.size(); i++) {
            System.out.print(trace.get(i) + " ");
        }
        System.out.println();
    }
}
