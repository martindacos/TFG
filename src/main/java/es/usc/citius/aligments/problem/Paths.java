package es.usc.citius.aligments.problem;

import es.usc.citius.aligments.config.Parametros;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

public class Paths {

    /**
     * Obtain approximate paths of model
     */

    //Actividades obligatorias
    TIntHashSet requiredTasks;
    //Actividades que es obligatorio ejecutar alguna de el grupo
    List<TIntHashSet> requiredOptionalTasks;
    //Actividades bucle
    TIntHashSet loopTasks;

    public Paths() {
        requiredTasks = new TIntHashSet();
        requiredOptionalTasks = new ArrayList<>();
        loopTasks = new TIntHashSet();
    }

    void addRequiredTask(int t) {
        requiredTasks.add(t);
    }

    void removeRequiredTask(int t) {
        requiredTasks.remove(t);
    }

    void addRequiredOptionalTask(TIntHashSet t) {
        requiredOptionalTasks.add(t);
    }

    void addLoopsTask(int t) {
        loopTasks.add(t);
    }

    //Cálculo de la heurística para cada traza
    double checkTrace(ArrayList<Integer> tareas, int pos){
        //Copiamos las posibles tareas e inicializamos las variables
        int movsSincronos = 0;
        int otherMovs = 0;
        boolean addMovs;
        double adjustHeuristic = pos * Parametros.COSTE_TRAZA;
        TIntHashSet requiredTasksLocal = new TIntHashSet(requiredTasks);
        List<TIntHashSet> requiredOptionalTasksLocal = new ArrayList<>();
        for (TIntHashSet set : requiredOptionalTasks) {
            requiredOptionalTasksLocal.add(set);
        }
        for (; pos<tareas.size(); pos++) {
            Integer task = tareas.get(pos);
            //Actividad obligatoria
            if (requiredTasksLocal.contains(task)) {
                movsSincronos++;
                requiredTasksLocal.remove(task);
            //Bucle
            } else if (loopTasks.contains(task)) {
                movsSincronos++;
            } else {
                addMovs = true;
                //OR en modelo
                for (TIntHashSet set : requiredOptionalTasksLocal) {
                    if (set.contains(task)) {
                        movsSincronos++;
                        addMovs = false;
                        //TODO Check this remove
                        requiredOptionalTasksLocal.remove(set);
                        break;
                    }
                }
                if (addMovs) otherMovs++;
            }
        }
        //Añadimos los movimientos que no se encuentran en la traza
        otherMovs += requiredTasksLocal.size();
        otherMovs += requiredOptionalTasksLocal.size();

        //Ajustamos la traza cuando ya se encuentra procesada una parte
        double h = movsSincronos * Parametros.COSTE_SINCRONO + otherMovs * Parametros.COSTE_TRAZA;
        if (h > adjustHeuristic) {
            h -= adjustHeuristic;
        } else {
            h = 0;
        }
        return h;
    }
}
