package es.usc.citius.aligments.problem;

import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author marti
 */
public class Traza implements InterfazTraza {

    private String id;
    private ArrayList<Integer> tareas;
    private double score;
    private int numRepeticiones;
    private double tiempoC;
    private double memoriaC;

    private int tareaFinalModelo;

    //Mapa con las tareas y el nº de tareas necesarias para alcanzar el final
    private Map<Integer, Integer> toFin;

    public Traza() {
        this.tareas = new ArrayList<>();
        this.numRepeticiones = 1;
        memoriaC = 0;
        tareaFinalModelo = -1;
        toFin = new HashMap();
    }

    public ArrayList<Integer> getTareas() {
        return tareas;
    }

    public void setTareas(ArrayList<Integer> tareas) {
        this.tareas = tareas;
    }

    @Override
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int getNumRepeticiones() {
        return numRepeticiones;
    }

    @Override
    public void setNumRepeticiones(int numRepeticiones) {
        this.numRepeticiones = numRepeticiones;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int tamTrace() {
        return tareas.size();
    }

    @Override
    public void anadirTarea(int t) {
        tareas.add(t);
    }

    @Override
    public Integer leerTarea(int p) {
        if (p < tareas.size()) {
            return tareas.get(p);
        } else {
            return null;
        }
    }

    @Override
    public Double getHeuristica(int pos, CMIndividual m, Integer lastEjecuted) {
        double tareasFin, r, resultado;

        Integer tarea = leerTarea(pos);
        if (tarea == null) {
            if (lastEjecuted == null) {
                r = tareas.size() - pos;
                return r;
            } else {
                tarea = lastEjecuted;
            }
        }

        //TODO Eliminar. Para checkear que se calcula ben a distancia
//        if (toFin.containsKey(tarea)) {
//            r = toFin.get(tarea);
//            return r;
//        }
//        tareasFin = tareasToFin(tarea, m);
        //TODO Esto deberíase comprobar no CMIndividual
        //Comprobamos que no hemos calculado ya la distancia a la tarea final
        if (toFin.containsKey(tarea)) {
            //Recuperamos la distancia calculada
            tareasFin = toFin.get(tarea);
        } else {
            //Calculamos la nueva distancia
            tareasFin = tareasToFin(tarea, m);
        }

        r = tareas.size() - pos;
        //System.out.println("Tarea " + tarea + " Maximo entre (tareas restantes modelo) " + tareasFin + " y (tareas restantes traza) " + r);
        resultado = Math.max(tareasFin, r);
        return resultado;
    }

    //Ya acabé la traza. La última posicion quedaría fuera. 
    @Override
    public boolean procesadoTraza(int pos) {
        return pos >= tareas.size();
    }

    @Override
    public void print() {
        System.out.println();
        for (int i = 0; i < tareas.size(); i++) {
            System.out.print(tareas.get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public double getTiempoC() {
        return tiempoC;
    }

    @Override
    public void setTiempoC(double tiempoC) {
        this.tiempoC = tiempoC;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public double getScoreRepetido() {
        return this.score * this.numRepeticiones;
    }

    @Override
    public double getMemoriaC() {
        return memoriaC;
    }

    @Override
    public void addMemoriaC(double c) {
        memoriaC = memoriaC + c;
    }

    @Override
    public void clear() {
        score = 0;
        tiempoC = 0;
        memoriaC = 0;
    }

    /*Función que dada una tarea nos indica el número mínimo de tareas necesarias
    para llegar a la tarea final
    TODO esto podémolo poñer dentro do individuo en ProDiGen, de maneira que se
    o calculamos para unha traza xa o podemos reutilizar para todas
     */
    private double tareasToFin(Integer task, CMIndividual m) {

        //Obtenemos la tarea final del modelo si no la tenemos almacenada ya
        if (this.tareaFinalModelo == -1) {
            TIntIterator it = m.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                //En caso de que no podamos devolver la tarea final devolvemos un error
                return -1;
            }
            //Si la tarea ya es la final devolvemos 0
        } else if (task == tareaFinalModelo) {
            toFin.put(task, 0);
            return 0;
        }

        //Creamos una lista con listas de todas las salidas de una tarea hasta
        //llegar al final
        List<List<Integer>> tareasRestantes = new ArrayList();
        //Lista con las tareas ya añadidas
        TIntHashSet anadidas = new TIntHashSet();
        //Lista con la tarea inicial de la que queremos saber el camino hasta el final
        List<Integer> primera = new ArrayList();
        primera.add(task);
        tareasRestantes.add(primera);

        int lista = 0;
        label1:
        while (lista < tareasRestantes.size()) {
            ArrayList<Integer> nuevasTareas = new ArrayList();
            for (int i = 0; i < tareasRestantes.get(lista).size(); i++) {
                Iterator<TIntHashSet> set = m.getTask(tareasRestantes.get(lista).get(i)).getOutputs().iterator();
                while (set.hasNext()) {
                    TIntIterator it = set.next().iterator();
                    while (it.hasNext()) {
                        int tarea = it.next();
                        if (!anadidas.contains(tarea)) {
                            if (tarea == tareaFinalModelo) {
                                tareasRestantes.add(nuevasTareas);
                                break label1;
                            } else {
                                anadidas.add(tarea);
                                nuevasTareas.add(tarea);
                            }
                        }
                    }
                }
            }
            if (nuevasTareas.size() > 0) {
                tareasRestantes.add(nuevasTareas);
            } else {
                break;
            }
            lista++;
        }

        /*Guardamos en el mapa la tarea y el nº de tareas necesarias para alcanzar
        la tarea final */
        Integer tareasToFin = tareasRestantes.size() - 1;
        toFin.put(task, tareasToFin);
        // Le tenemos que restar la lista que contiene la tarea inicial 
        return tareasToFin;
    }
}
