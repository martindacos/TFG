/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marking;

import Task.Task;
import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author qnoxo
 */
public class FiringCombination {

    private TIntHashSet tasks;
    private int missingTokens;
    private int task = Task.INVALID_TASK;

    public FiringCombination() {
        this.tasks = new TIntHashSet();
    }

    public FiringCombination(FiringCombination newCombination) {
        this.tasks = new TIntHashSet(newCombination.tasks);
        this.missingTokens = newCombination.missingTokens;
    }

    public void setFiringTask(int element) {
        this.task = element;
    }

    public int getFiringTask() {
        return this.task;
    }

    public TIntHashSet getTasks() {
        return this.tasks;
    }

    public void addTask(int elem){
        this.tasks.add(elem);
    }
    
    public int getNumMissingTokens() {
        return this.missingTokens;
    }

    public void setTasks(TIntHashSet newSetOftasks) {
        this.tasks = newSetOftasks;
    }

    public void addTokens(int n){
        missingTokens += n;
    }

    @Override
    public String toString() {
        return tasks.toString() + " : " + this.missingTokens;
    }
}

