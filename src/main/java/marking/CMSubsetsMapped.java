/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marking;

import CMTask.CMSet;
import Individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author qnoxo
 */
public class CMSubsetsMapped {

    private final CMSet[][] map;

    public CMSubsetsMapped(CMIndividual ind) {
        final int numOfTasks=ind.getNumOfTasks();
        this.map = new CMSet[numOfTasks][numOfTasks];
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            final CMSet currenTaskOutputs = ind.getTask(indexTask).getOutputs();
            for (TIntHashSet subset : currenTaskOutputs) {
                for (TIntIterator it = subset.iterator(); it.hasNext(); ) {
                    int indexOtherTask = it.next();
                    CMSet set = this.map[indexOtherTask][indexTask];
                    if (set == null) {
                        set = new CMSet();
                    }
                    set.add(subset);
                    this.map[indexOtherTask][indexTask] = set;
                }
            }
        }
    }

    public CMSet getRelatedElemens(int indesOtherTask, int currentTask) {
        return this.map[indesOtherTask][currentTask];
    }
}

