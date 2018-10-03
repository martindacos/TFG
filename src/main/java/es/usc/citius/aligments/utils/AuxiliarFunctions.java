package es.usc.citius.aligments.utils;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AuxiliarFunctions {

    /**
     * Functions to simplify model and reduce OR options. Need test the correct functioning.
     */

    public static CMIndividual simplifyModel(CMIndividual model, InterfazTraza trace) {
        List<Integer> tareas = trace.getTareas();
        TIntObjectIterator<CMTask> it = model.getTasks().iterator();
        //Check what tasks of model not are in the trace
        List<Integer> tasks = new ArrayList<>();
        while (it.hasNext()) {
            it.advance();
            int key = it.value().getMatrixID();
            if (!tareas.contains(key)) tasks.add(key);
        }

        //If we have any task on model and not on trace
        if (tasks.size() != 0) {
            for (int t : tasks) {
                CMTask task = model.getTask(t);
                //If we have more than one task is not and OR
                if (task.getInputs().size() > 1) break;
                int closeORTask = checkInputs(model, task);
                if (checkOutputs(model, tasks, task, closeORTask)) {
                    model = removeTask(model, task, closeORTask);
                }
            }
            return model;
        } else {
            return model;
        }
    }

    //Check the task who close the OR (if it's possible)
    public static int checkInputs(CMIndividual model, CMTask task) {
        TIntHashSet maybeCloseTasks = new TIntHashSet();
        //Check the tasks that can close the OR from actual task
        Iterator<TIntHashSet> iterator1 = task.getOutputs().iterator();
        while (iterator1.hasNext()) {
            TIntIterator iterator = iterator1.next().iterator();
            while (iterator.hasNext()) {
                maybeCloseTasks.add(iterator.next());
            }
        }
        CMSet inputs = task.getInputs();
        Iterator<TIntHashSet> iterator = inputs.iterator();
        while (iterator.hasNext()) {
            TIntIterator it = iterator.next().iterator();
            //Tasks to explore
            TIntHashSet otherTasks = new TIntHashSet();
            //Tasks yet explored
            TIntHashSet exploredTasks = new TIntHashSet();
            while (it.hasNext()) {
                int next = it.next();
                if (!exploredTasks.contains(next)) otherTasks.add(next);
                exploredTasks.add(next);
                //Explore Until Close
                if (maybeCloseTasks.contains(next)) return next;
            }
            if (otherTasks.size() > 0) {
                int next = otherTasks.iterator().next();
                otherTasks.remove(next);
                CMTask task1 = model.getTask(next);
                iterator = task1.getOutputs().iterator();
            }
        }
        return 0;
    }

    //Check if we have an OR that we can delete
    public static boolean checkOutputs(CMIndividual model, List<Integer> tasks, CMTask task, int closeORTask) {
        CMSet outputs = task.getOutputs();
        if (outputs.size() == 1) {
            //Outputs of task
            Iterator<TIntHashSet> iterator = outputs.iterator();
            //Tasks to explore
            TIntHashSet otherTasks = new TIntHashSet();
            //Tasks yet explored
            TIntHashSet exploredTasks = new TIntHashSet();
            while (iterator != null) {
                while (iterator.hasNext()) {
                    TIntIterator it = iterator.next().iterator();
                    while (it.hasNext()) {
                        int next = it.next();
                        if (!exploredTasks.contains(next)) otherTasks.add(next);
                        exploredTasks.add(next);
                        //If trace contains de task we can't delete it
                        if (closeORTask != next && tasks.contains(next)) return false;
                    }
                }
                if (otherTasks.size() > 0) {
                    int next = otherTasks.iterator().next();
                    otherTasks.remove(next);
                    CMTask task1 = model.getTask(next);
                    outputs = task1.getOutputs();
                    iterator = outputs.iterator();
                    //Not possible OR
                    if (outputs.size() != 1) {
                        if (outputs.size() == 0) {
                            iterator = null;
                        } else {
                            return false;
                        }
                    }

                }
            }
            return true;
        } else {
            return false;
        }
    }

    //Remove a task on the model
    public static CMIndividual removeTask(CMIndividual model, CMTask task, int closeORTask) {
        //Change tasks on a copy
        CMIndividual copyModel = new CMIndividual(model);
        //Remove inputs of original task
        int matrixID1 = task.getMatrixID();
        Iterator<TIntHashSet> iterator2 = copyModel.getTask(matrixID1).getInputs().iterator();
        while (iterator2.hasNext()) {
            TIntIterator it = iterator2.next().iterator();
            while (it.hasNext()) {
                int next = it.next();
                //Remove actual task of outputs
                CMSet outputs = copyModel.getTask(next).getOutputs();
                for (int i=0; i<outputs.size(); i++) {
                    TIntHashSet hashSet = outputs.get(i);
                    hashSet.remove(matrixID1);
                }
            }
        }

        int count = copyModel.getNumOfTasks() - 1;
        Iterator<TIntHashSet> iterator = task.getOutputs().iterator();
        while (iterator.hasNext()) {
            //Tasks to explore
            TIntHashSet otherTasks = new TIntHashSet();
            //Tasks yet explored
            TIntHashSet exploredTasks = new TIntHashSet();
            //Outputs of task
            TIntIterator iterator1 = iterator.next().iterator();
            while (iterator1.hasNext()) {
                int next = iterator1.next();
                CMTask newTask = copyModel.getTask(next);
                CMSet inputs = newTask.getInputs();
                for (int i=0; i<inputs.size(); i++) {
                    inputs.get(i).remove(matrixID1);
                }
                int matrixID = newTask.getMatrixID();
                if (matrixID != closeORTask && !exploredTasks.contains(matrixID)) {
                    exploredTasks.add(matrixID);
                    otherTasks.add(matrixID);
                }
            }
            if (otherTasks.size() > 0) {
                count--;
                int next = otherTasks.iterator().next();
                otherTasks.remove(next);
                //Remove the new task
                copyModel.getTask(next).getOutputs().iterator();
            }
        }

        //Create a new model with the correct number of tasks
        CMIndividual model2 = new CMIndividual(count);
        TIntObjectHashMap<CMTask> cmTaskTIntObjectHashMap = (TIntObjectHashMap<CMTask>) copyModel.getTasks();
        TIntObjectIterator<CMTask> it = cmTaskTIntObjectHashMap.iterator();
        int key = 0;
        while (it.hasNext()) {
            it.advance();
            CMTask value = it.value();
            if (value.getMatrixID() == matrixID1) {
               key = it.key();
            }
        }
        cmTaskTIntObjectHashMap.remove(key);
        model2.setTasks(cmTaskTIntObjectHashMap);

        return model2;
    }
}
