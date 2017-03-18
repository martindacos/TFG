/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Individual.properties;

import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author QnOx
 */
public class IndividualFitness {

    private double completeness;
    private double preciseness;
    private int numRelations;

    private double fitness;

    private int enabledActivities;
    private int allParsedActivities;
    private int allMissingTokens;
    private int allExtraTokensLeftBehind;
    private int numTracesMissingTokens;
    private int numTracesExtraTokensLeftBehind;
    private TIntHashSet unfiredTasks = new TIntHashSet();
    private TIntHashSet unfiredTasksOutputs = new TIntHashSet();
    private TIntHashSet unfiredTasksInputs = new TIntHashSet();

    public IndividualFitness() {}
    
    public IndividualFitness(IndividualFitness fitness) {
        this.completeness = fitness.completeness;
        this.preciseness = fitness.preciseness;
        this.fitness = fitness.fitness;
        this.enabledActivities = fitness.enabledActivities;
        this.allParsedActivities = fitness.allParsedActivities;
        this.allMissingTokens = fitness.allMissingTokens;
        this.allExtraTokensLeftBehind = fitness.allExtraTokensLeftBehind;
        this.numTracesMissingTokens = fitness.numTracesMissingTokens;
        this.numTracesExtraTokensLeftBehind = fitness.numTracesExtraTokensLeftBehind;
        this.numRelations = fitness.numRelations;
        this.unfiredTasks = new TIntHashSet(fitness.unfiredTasks);
        this.unfiredTasksOutputs = new TIntHashSet(fitness.unfiredTasksOutputs);
        this.unfiredTasksInputs = new TIntHashSet(fitness.unfiredTasksOutputs);
    }

    public int getNumRelations() {
        return numRelations;
    }

    public TIntHashSet getUnfiredTasks() {
        return unfiredTasks;
    }
    public void setUnfiredTasks(TIntHashSet unfired) {
        this.unfiredTasks = unfired;
    }
    public void addUnfiredTasks(int unfiredTask) {
        this.unfiredTasks.add(unfiredTask);
    }
    public void addUnfiredTasksAll(TIntHashSet unfiredTask) {
        this.unfiredTasks.addAll(unfiredTask);
    }
    public void addUnfiredTasksOutputsAll(TIntHashSet unfiredTask) {
        this.unfiredTasksOutputs.addAll(unfiredTask);
    }
    public void addUnfiredTasksInputsAll(TIntHashSet unfiredTask) {
        this.unfiredTasksInputs.addAll(unfiredTask);
    }
    public void setNumRelations(int numRelations) {
        this.numRelations = numRelations;
    }

    public TIntHashSet getUnfiredTasksOutputs() {
        return unfiredTasksOutputs;
    }
    public TIntHashSet getUnfiredTasksInputs() {
        return unfiredTasksInputs;
    }

    public void addUnfiredTasksOutputs(int unfiredTask) {
        this.unfiredTasksOutputs.add(unfiredTask);
    }

    public void addNumTracesMissingTokens(int n) {
        this.numTracesMissingTokens += n;
    }

    public void addNumTracesExtraTokensLeftBehind(int n) {
        this.numTracesExtraTokensLeftBehind += n;
    }

    public void addParsedActivies(int n) {
        this.allParsedActivities += n;
    }

    public void addMissingTokens(int n) {
        this.allMissingTokens += n;
    }

    public void addExtraTokensLeftBehind(int n) {
        this.allExtraTokensLeftBehind += n;
    }

    public void addEnabledActivities(double n) {
        this.enabledActivities += n;
    }

    public int getAllExtraTokensLeftBehind() {
        return allExtraTokensLeftBehind;
    }

    public int getAllMissingTokens() {
        return allMissingTokens;
    }

    public int getAllParsedActivities() {
        return allParsedActivities;
    }

    public int getEnabledActivities() {
        return enabledActivities;
    }

    public int getNumTracesExtraTokensLeftBehind() {
        return numTracesExtraTokensLeftBehind;
    }

    public int getNumTracesMissingTokens() {
        return numTracesMissingTokens;
    }

    public void setAllExtraTokensLeftBehind(int allExtraTokensLeftBehind) {
        this.allExtraTokensLeftBehind = allExtraTokensLeftBehind;
    }

    public void setAllMissingTokens(int allMissingTokens) {
        this.allMissingTokens = allMissingTokens;
    }

    public void setAllParsedActivities(int allParsedActivities) {
        this.allParsedActivities = allParsedActivities;
    }

    public void setEnabledActivities(int enabledActivities) {
        this.enabledActivities = enabledActivities;
    }

    public void setNumTracesExtraTokensLeftBehind(int numTracesExtraTokensLeftBehind) {
        this.numTracesExtraTokensLeftBehind = numTracesExtraTokensLeftBehind;
    }

    public void setNumTracesMissingTokens(int numTracesMissingTokens) {
        this.numTracesMissingTokens = numTracesMissingTokens;
    }

    public double getCompleteness() {
        return completeness;
    }

    public double getFitness() {
        return fitness;
    }

    public double getPreciseness() {
        return preciseness;
    }

    public void setCompleteness(double Completeness) {
        this.completeness = Completeness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void setPreciseness(double preciseness) {
        this.preciseness = preciseness;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.completeness) ^ (Double.doubleToLongBits(this.completeness) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.preciseness) ^ (Double.doubleToLongBits(this.preciseness) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.fitness) ^ (Double.doubleToLongBits(this.fitness) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.enabledActivities) ^ (Double.doubleToLongBits(this.enabledActivities) >>> 32));
        hash = 11 * hash + this.allParsedActivities;
        hash = 11 * hash + this.allMissingTokens;
        hash = 11 * hash + this.allExtraTokensLeftBehind;
        hash = 11 * hash + this.numTracesMissingTokens;
        hash = 11 * hash + this.numTracesExtraTokensLeftBehind;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndividualFitness other = (IndividualFitness) obj;
        if (Double.doubleToLongBits(this.completeness) != Double.doubleToLongBits(other.completeness)) {
            return false;
        }
        if (Double.doubleToLongBits(this.preciseness) != Double.doubleToLongBits(other.preciseness)) {
            return false;
        }
        if (Double.doubleToLongBits(this.fitness) != Double.doubleToLongBits(other.fitness)) {
            return false;
        }
        if (Double.doubleToLongBits(this.enabledActivities) != Double.doubleToLongBits(other.enabledActivities)) {
            return false;
        }
        if (this.allParsedActivities != other.allParsedActivities) {
            return false;
        }
        if (this.allMissingTokens != other.allMissingTokens) {
            return false;
        }
        if (this.allExtraTokensLeftBehind != other.allExtraTokensLeftBehind) {
            return false;
        }
        if (this.numTracesMissingTokens != other.numTracesMissingTokens) {
            return false;
        }
        if (this.numTracesExtraTokensLeftBehind != other.numTracesExtraTokensLeftBehind) {
            return false;
        }
        return true;
    }
   
}

