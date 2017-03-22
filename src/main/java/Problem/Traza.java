/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Problem;

import domainLogic.workflow.Task.Task;
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
        //Sumamos 1 porque la posición va de 0..X y el size de 1..X
        double r;
        if (pos >= trace.size()) {
            r = 0;
        } else {
            r = trace.size() - pos;
        }
        //System.out.println("Size: " + trace.size() + " Pos: " + pos);
        return r;
    }

    //Estoy en la última tarea de la traza 
//    public boolean finalTraza(int pos) {
//        return pos == (trace.size() - 1);
//    }

    //Ya acabé la traza. La última posicion quedaría fuera. 
    @Override
    public boolean procesadoTraza(int pos) {
        return pos >= trace.size();
    }
}
