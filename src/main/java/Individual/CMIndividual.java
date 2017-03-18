package Individual;

import CMTask.CMTask;
import Individual.properties.IndividualCombUsage;
import Individual.properties.IndividualFitness;
import Task.Task;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Objects;
import org.ejml.simple.SimpleMatrix;

public class CMIndividual {

    private final int numOfTasks;

    private TIntObjectMap<CMTask> tasks;

    private SimpleMatrix arcsUsage;
    private IndividualCombUsage combUsage;
    private IndividualFitness fitness;

    private TIntHashSet startTasks;
    private boolean isOffspring = false;


    public CMIndividual(int numOfTasks) {
        this.numOfTasks = numOfTasks;
        this.tasks = new TIntObjectHashMap<>(numOfTasks);
    }

//    public CMIndividual(Log log) {
//        this(log.getNumOfTasks());
//        this.tasks = new TIntObjectHashMap<>(numOfTasks);
//        initTasks(log);
//    }

    public CMIndividual(CMIndividual ind) {
        this(ind.numOfTasks);
        this.isOffspring = ind.isOffspring;
        setFitness(ind.getFitness());
        setArcsUsage(ind.arcsUsage);
        setCombUsage(ind.combUsage);
        deepCopyCausalMatrix(ind);
    }

//    private void initTasks(Log log) {
//        for (Task task : log.getArrayTasks()) {
//            CMTask actualTask = new CMTask(task);
//            this.tasks.put(task.getMatrixID(), actualTask);
//        }
//    }

    private void deepCopyCausalMatrix(CMIndividual ind) {
        this.tasks = new TIntObjectHashMap<>(numOfTasks);
        for (int indexTask = 0; indexTask < this.numOfTasks; indexTask++) {
            this.tasks.put(indexTask, new CMTask(ind.tasks.get(indexTask)));
        }
    }

    public void initArcUsage() {
        this.arcsUsage = new SimpleMatrix(numOfTasks, numOfTasks);
        this.combUsage = new IndividualCombUsage(numOfTasks);
    }

    public int getNumOfTasks() {
        return numOfTasks;
    }

    public CMTask getTask(int task) {
        return tasks.get(task);
    }

    public void setTasks(TIntObjectHashMap<CMTask> tasks) {
        if (tasks.size() == numOfTasks) {
            this.tasks = tasks;
        }
    }

    public IndividualFitness getFitness() {
        return fitness;
    }

    public final boolean setFitness(IndividualFitness fitness) {
        if (fitness == null) {
            this.fitness = null;
        } else {
            this.fitness = new IndividualFitness(fitness);
            return true;
        }
        return false;
    }

    public boolean isOffspring() {
        return isOffspring;
    }

    public void setOffspring(boolean isOffspring) {
        this.isOffspring = isOffspring;
    }

    public final boolean setArcsUsage(SimpleMatrix arcsUsage) {
        if (arcsUsage != null) {
            this.arcsUsage = new SimpleMatrix(arcsUsage);
            return true;
        } else {
            this.arcsUsage = null;
        }
        return false;
    }

    public final boolean setCombUsage(IndividualCombUsage combUsage) {
        if (combUsage != null) {
            this.combUsage = new IndividualCombUsage(combUsage);
            return true;
        } else {
            this.combUsage = null;
        }
        return false;
    }

    public SimpleMatrix getArcsUsage() {
        return arcsUsage;
    }

    public IndividualCombUsage getCombUsage() {
        return combUsage;
    }

    public int getNumRelations() {
        int counter = 0;
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            CMTask auxTask = tasks.get(indexTask);
            counter += auxTask.getInputs().subsetsSize();
            counter += auxTask.getOutputs().subsetsSize();
        }
        return counter;
    }

    public TIntHashSet getStartTasks() {
        if (startTasks == null) {
            TIntHashSet tempStarTasks = new TIntHashSet();
            for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
                if (tasks.get(indexTask).getInputs().isEmpty()) {
                    tempStarTasks.add(indexTask);
                }
            }
            this.startTasks = tempStarTasks;
        }
        return startTasks;
    }

    public TIntHashSet getEndTasks() {
        TIntHashSet endTasks = new TIntHashSet();
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            if (tasks.get(indexTask).getOutputs().isEmpty()) {
                endTasks.add(indexTask);
            }
        }
        return endTasks;
    }

    public void increaseArcUsage(int task, TIntHashSet usedInputActivities, int amount) {
        TIntIterator iter = usedInputActivities.iterator();
        while (iter.hasNext()) {
            final int currentTaskID = iter.next();
            arcsUsage.set(currentTaskID, task, arcsUsage.get(currentTaskID, task) + amount);
        }
    }

    public void print() {
        tasks.forEachValue(new TObjectProcedure<CMTask>(
                           ) {
                               @Override
                               public boolean execute(CMTask task) {
                                   System.out.println("I(" + task.getTask().getId() + "_" + task.getTask().getMatrixID() + ")=" + task.getInputs());
                                   System.out.println("O(" + task.getTask().getId() + "_" + task.getTask().getMatrixID() + ")=" + task.getOutputs());
                                   return true;
                               }
                           }
        );
        if (fitness != null) {
            System.out.println("Fitness:" + fitness.getFitness() + " ( Completitud="
                    + +fitness.getCompleteness() + " | Precision=" + fitness.getPreciseness() + " )");
            System.out.println("num Relations:           " + fitness.getNumRelations());
            System.out.println("parsed activities:       " + fitness.getAllParsedActivities());
            System.out.println("enabled activities:      " + fitness.getEnabledActivities());
            System.out.println("Extra tokens behind:     " + fitness.getAllExtraTokensLeftBehind());
            System.out.println("missing tokens:          " + fitness.getAllMissingTokens());
            System.out.println("num traces extra behind: " + fitness.getNumTracesExtraTokensLeftBehind());
            System.out.println("num traces missing:      " + fitness.getNumTracesMissingTokens());
        }
        System.out.println("*************************");
    }

    public void printArcUsage() {
        System.out.println("******* ARC USAGE *******");
        for (int indexTaskA = 0; indexTaskA < numOfTasks; indexTaskA++) {
            for (int indexTaskB = 0; indexTaskB < numOfTasks; indexTaskB++) {
                double usage = this.arcsUsage.get(indexTaskA, indexTaskB);
                if (usage != 0) {
                    System.out.println(">       " + tasks.get(indexTaskA) + " => " + tasks.get(indexTaskB) + " = " + usage);
                }
            }
        }
        System.out.println("*************************");
    }

    @Override
    public boolean equals(Object arg0) {
        if (this == arg0) {
            return true;
        }
        if (arg0 == null) {
            return false;
        }
        if (getClass() != arg0.getClass()) {
            return false;
        } else {
            final CMIndividual otherInd = (CMIndividual) arg0;
            for (int indexTask = 0; indexTask < this.numOfTasks; indexTask++) {
                if (!this.tasks.get(indexTask).equals(otherInd.tasks.get(indexTask))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.tasks);
        hash = 89 * hash + this.numOfTasks;
        return hash;
    }
}

