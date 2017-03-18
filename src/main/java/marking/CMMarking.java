/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marking;

import CMTask.CMSet;
import CMTask.CMTask;
import CMTask.SubsetsUtil;
import Individual.CMIndividual;
import Task.Task;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CMMarking {

    private final Random rnd;
    private final CMIndividual ind;
    private final TIntHashSet startTasks;
    private final CMSubsetsMapped subsetsMapped;
    private ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    private int startPlace = 1;
    private int numOfTokens = 1;
    private int endPlace = 0;
    private TIntHashSet possibleEnabledTasks;
    private FiringCombination bestCombination;

    public CMMarking(CMIndividual ind, Random rnd) {
        this.ind = ind;
//        this.rnd = new Random(Integer.MAX_VALUE); // CHEaTeR
        this.rnd = rnd;
        this.subsetsMapped = new CMSubsetsMapped(ind);
        this.startTasks = ind.getStartTasks();
        this.possibleEnabledTasks = new TIntHashSet(startTasks);

        final int numOfTasks = ind.getNumOfTasks();
        this.tokens = new ArrayList<>(numOfTasks);
        initializateTokens(numOfTasks);
    }

    private void initializateTokens(int numOfTasks) {
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            HashMap<TIntHashSet, Integer> outputs = new HashMap<>(numOfTasks);
            CMSet taskOutputs = ind.getTask(indexTask).getOutputs();
            for (TIntHashSet subset : taskOutputs) {
                outputs.put(subset, 0);
            }
            this.tokens.add(outputs);
        }
    }

    public void restartMarking() {
        this.numOfTokens = 1;
        this.startPlace = 1;
        this.endPlace = 0;
        this.possibleEnabledTasks = new TIntHashSet(startTasks);
        restartTokens();
    }

    private void restartTokens() {
        for (HashMap<TIntHashSet, Integer> outputs : tokens) {
            for (TIntHashSet subset : outputs.keySet()) {
                outputs.put(subset, 0);
            }
        }
    }

    public ArrayList<HashMap<TIntHashSet, Integer>> getTokens() {
        return tokens;
    }

    /*remove numcase*/
    public int execute(int currentTaskID) {
        final CMTask currentTask = ind.getTask(currentTaskID);
        this.bestCombination = getBestCombination(currentTask, currentTaskID);
        consumeInputs(currentTask, currentTaskID, this.bestCombination.getTasks());
        enableOutputs(currentTask, currentTaskID);
        this.possibleEnabledTasks.addAll(currentTask.getOutputs().getUnionSubsets());
        return this.bestCombination.getNumMissingTokens();
    }

    private void enableOutputs(CMTask currentTask, int currentTaskID) {
        final CMSet outputs = currentTask.getOutputs();
        final int outputsSize = outputs.size();
        // is an end element?
        if (outputsSize == 0) {
            this.numOfTokens++;
            this.endPlace++;
            // other element
        } else {
            this.numOfTokens += outputsSize;
            for (TIntHashSet subset : outputs) {
                increaseTokens(currentTaskID, subset);
            }
        }
    }

    private void consumeInputs(CMTask currentTask, int currentTaskID, TIntHashSet activities) {
        // is start element?
        if (currentTask.getInputs().isEmpty()) {
            if (this.startPlace > 0) {
                this.startPlace--;
                this.numOfTokens--;
            }
            // other element
        } else {
            TIntIterator iter = activities.iterator();
            while(iter.hasNext()){
                final int task = iter.next();
                final CMSet subsets = this.subsetsMapped.getRelatedElemens(currentTaskID, task);
                if (subsets != null) {
                    for (TIntHashSet subset : subsets) {
                        int currentTokens = this.tokens.get(task).get(subset);
                        if (currentTokens > 0) {
                            this.numOfTokens--;
                            this.tokens.get(task).put(subset, --currentTokens);
                        }
                    }
                }
            }
        }
    }

    private int getPlacesWithTokens(CMSet newInputSet, CMSet currenTaskInputs, int currentTaskID) {
        int missingTokens = 0;
       TIntHashSet noTokensFromTasks = getTasksWithEmptyOutputPlaces(currentTaskID);
        for (TIntHashSet subset : currenTaskInputs) {
            subset.removeAll(noTokensFromTasks);
            if (subset.isEmpty()) {
                missingTokens++;
            } else {
                newInputSet.add(subset);
            }
        }
        return missingTokens;
    }

    private FiringCombination getBestCombination(CMTask currentTask, int currentTaskID) {
        FiringCombination bestLocalCombination = new FiringCombination();
        final CMSet currenTaskInputs = new CMSet(currentTask.getInputs());
        int missingTokens = 0;
        if (currenTaskInputs.isEmpty()) {
            if (startPlace <= 0) {
                missingTokens++;
            }
        } else {
            CMSet newInputSet = new CMSet();
            missingTokens = getPlacesWithTokens(newInputSet, currenTaskInputs, currentTaskID);
            if (!newInputSet.isEmpty()) {
                bestLocalCombination = getBestCombination(bestLocalCombination, newInputSet,
                        Task.INVALID_TASK, new FiringCombination(), new TIntHashSet());
            }
        }
        bestLocalCombination.setFiringTask(currentTaskID);
        bestLocalCombination.addTokens(missingTokens);
        return bestLocalCombination;
    }

    private FiringCombination getBestCombination(FiringCombination bCombination, CMSet inputSet,
                                                 int currentTask, FiringCombination combination, TIntHashSet treatedTasks) {
        if ((bCombination.getTasks().isEmpty())
                || (bCombination.getNumMissingTokens() > combination.getNumMissingTokens())) {
            TIntHashSet noTokensFromTasks = null;
            if (currentTask != Task.INVALID_TASK) {
                CMSet alreadyMarkedPlaces = getAlreadyMarkedPlaces(inputSet, currentTask);
                noTokensFromTasks = alreadyMarkedPlaces.getUnionSubsets();
                inputSet.removeAll(alreadyMarkedPlaces);
                combination.addTask(currentTask);
            }

            if (inputSet.isEmpty()) {
                bCombination = new FiringCombination(combination);
            } else {
                if (currentTask != Task.INVALID_TASK) {
                    CMSet inputSetAux = new CMSet();
                    for (TIntHashSet subset : inputSet) {
                        subset.removeAll(noTokensFromTasks);
                        subset.removeAll(treatedTasks);
                        if (subset.isEmpty()) {
                            combination.addTokens(1);
                        } else {
                            inputSetAux.add(subset);
                        }
                    }
                    inputSet = inputSetAux;
                }
                for (TIntHashSet subset : inputSet) {
                    while (!subset.isEmpty()) {
                        final int task = SubsetsUtil.getPos(subset, rnd.nextInt(subset.size()));
                        bCombination = getBestCombination(bCombination, new CMSet(inputSet), task, new FiringCombination(combination),
                                new TIntHashSet(treatedTasks));
                        treatedTasks.add(task);
                        subset.remove(task);
                    }
                }
            }
        }
        return bCombination;
    }

    private CMSet getAlreadyMarkedPlaces(CMSet set, int task) {
        CMSet markedPlaces = new CMSet();
        for (TIntHashSet subset : set) {
            if (subset.contains(task)) {
                markedPlaces.add(subset);
            }
        }
        return markedPlaces;
    }

    private TIntHashSet getTasksWithEmptyOutputPlaces(int task) {
        TIntHashSet inputTasks = this.ind.getTask(task).getInputs().getUnionSubsets();
        TIntHashSet tasksEmptyOutPlaces = new TIntHashSet();
        TIntIterator iter = inputTasks.iterator();
        while(iter.hasNext()){
            final int inputTask = iter.next();
            CMSet outputSubsets = this.subsetsMapped.getRelatedElemens(task, inputTask);
            if (outputSubsets != null) {
                if (!allSubsetsAreMarked(inputTask, outputSubsets)) {
                    tasksEmptyOutPlaces.add(inputTask);
                }
            } else {
                tasksEmptyOutPlaces.add(inputTask);
            }
        }
        return tasksEmptyOutPlaces;
    }

    private boolean allSubsetsAreMarked(int inputTask, CMSet outputSet) {
        for (TIntHashSet subset : outputSet) {
            if (tokens.get(inputTask).get(subset) <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isEnabled(int elem) {
        FiringCombination bestLocalCombination = getBestCombination(ind.getTask(elem), elem);
        return (bestLocalCombination.getNumMissingTokens() > 0) ? false : true;
    }

    public TIntHashSet getEnabledElements() {
        for (TIntIterator it = possibleEnabledTasks.iterator(); it.hasNext(); ) {
            int currentTaskID = it.next();
            if (!isEnabled(currentTaskID)) {
                it.remove();
            }
        }
        return possibleEnabledTasks;
    }

    public FiringCombination getBestCombination() {
        return bestCombination;
    }

    public void clearPossibleEnabledTasksSet() {
        this.possibleEnabledTasks.clear();
    }

    public int getNumberTokens() {
        return numOfTokens;
    }

    public boolean isEndPlaceEnabled() {
        return (endPlace > 0);
    }

    public int getStartPlace() {
        return startPlace;
    }

    public int getEndPlace() {
        return endPlace;
    }

    public void setTokens(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
        this.tokens = tokens;
    }

    public void setStartPlace(int startPlace) {
        this.startPlace = startPlace;
    }

    public void setNumOfTokens(int numOfTokens) {
        this.numOfTokens = numOfTokens;
    }

    public void setEndPlace(int endPlace) {
        this.endPlace = endPlace;
    }

    public void setPossibleEnabledTasks(TIntHashSet possibleEnabledTasks) {
        this.possibleEnabledTasks = possibleEnabledTasks;
    }


    private int increaseTokens(int currentTask, TIntHashSet subset) {
        int currentTokens = this.tokens.get(currentTask).get(subset);
        this.tokens.get(currentTask).put(subset, ++currentTokens);
        return currentTokens;
    }

    @Override
    public String toString() {
        StringBuilder writer = new StringBuilder();
        writer.append("\n Start place = ").append(startPlace);
        writer.append("\n End place = ").append(endPlace);
        final int numOfTasks = ind.getNumOfTasks();
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            writer.append("\n>>>>>>>Task = ").append(indexTask).append("[")
                    .append(ind.getTask(indexTask).getTask().getId())
                    .append("]");
            writer.append("\n\t").append(tokens.get(indexTask).toString());
        }
        return writer.toString();
    }
}
