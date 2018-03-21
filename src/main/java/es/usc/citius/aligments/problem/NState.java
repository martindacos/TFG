package es.usc.citius.aligments.problem;

import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import domainLogic.workflow.algorithms.geneticMining.CMTask.SubsetsUtil;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMSubsetsMapped;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.FiringCombination;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class NState {

    private NState() {
    }

    //Posibles movimientos del alineamiento
    public enum StateMove {
        SINCRONO, MODELO, TRAZA, MODELO_FORZADO
    }

    public static final class State {

        //Posición actual de análisis de la traza
        private int pos;
        //Para identificar a tareas dumming nos skips
        private int tarea;
        //Movimiento ejecutado en este estado
        private StateMove mov;
        //Tokens del modelo
        private HashMap<Integer, HashMap<TIntHashSet, Integer>> tokens;
        //Tarea final del modelo
        private final int tareaFinalModelo;

        //Marcado
        private final int numOfTasks;
        private final int numOfPosibleTasks;
        private final TIntObjectMap<CMTask> tasks;
        private final CMSubsetsMapped subsetsMapped;
        private int startPlace;
        private int numOfTokens;
        private int endPlace;
        private final int startTask;
        private TIntHashSet possibleEnabledTasks;
        private FiringCombination bestCombination;

        //Matrix with tokens
        private int[][] matrix;
        private final HashMap<Integer, Integer> orderTasks;

        public State(CMIndividual ind) {
            pos = 0;
            tarea = -1;
            this.numOfTasks = ind.getNumOfTasks();
            this.numOfPosibleTasks = posibleTask(ind);
            this.matrix = new int[numOfPosibleTasks][numOfPosibleTasks];
            this.tokens = new HashMap<>(numOfPosibleTasks);
            this.orderTasks = initializateTokens(ind);
            TIntIterator it = ind.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                tareaFinalModelo = -1;
            }

            this.tasks = ind.getTasks();
            this.subsetsMapped = new CMSubsetsMapped(ind);
            this.startPlace = 1;
            this.numOfTokens = 1;
            this.endPlace = 0;
            TIntIterator it2 = ind.getStartTasks().iterator();
            if (it2.hasNext()) {
                startTask = it2.next();
                this.possibleEnabledTasks = new TIntHashSet(numOfPosibleTasks);
                this.possibleEnabledTasks.add(startTask);
            } else {
                startTask = 0;
                System.err.print("No tenemos tarea inicial del modelo");
                System.exit(5);
            }
        }

        //Copias para la exploración del A*
        public State(State a) {
            pos = a.getPos();
            //Los tokens se copian "bien" cuando se ejecute el movimiento del estado
            tokens = a.getTokens();
            tareaFinalModelo = a.getTareaFinalModelo();

            numOfTasks = a.getNumOfTasks();
            numOfPosibleTasks = a.getNumOfPosibleTasks();
            tasks = a.getTasks();
            subsetsMapped = a.getSubsetsMapped();
            startPlace = a.getStartPlace();
            numOfTokens = a.getNumOfTokens();
            endPlace = a.getEndPlace();
            startTask = a.getStartTask();
            //Se realiza la copia "bien" cuando se ejecute el movimiento del estado
            possibleEnabledTasks = a.getPossibleEnabledTasks();

            matrix = new int[numOfPosibleTasks][numOfPosibleTasks];
            for (int i = 0; i < a.getMatrix().length; i++) {
                for (int j = 0; j < a.getMatrix().length; j++) {
                    matrix[i][j] = a.getMatrix()[i][j];
                }
            }
            orderTasks = a.getOrderTasks();
        }

        public void restartState() {
            pos = 0;
            tarea = -1;
            mov = null;

            this.startPlace = 1;
            this.numOfTokens = 1;
            this.endPlace = 0;
            this.possibleEnabledTasks = new TIntHashSet(numOfPosibleTasks);
            this.possibleEnabledTasks.add(startTask);
            restartTokens();
        }

        //Reservamos espacio solo para las tareas posibles
        private int posibleTask(CMIndividual ind) {
            int localPosibleTasks = 0;

            for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
                CMSet taskOutputs = ind.getTask(indexTask).getOutputs();
                if (taskOutputs.size() > 0) {
                    localPosibleTasks++;
                }
            }

            return localPosibleTasks;
        }

        private HashMap<Integer, Integer> initializateTokens(CMIndividual ind) {
            HashMap<Integer, Integer> orderTasksLocal = new HashMap<>(numOfPosibleTasks);
            int matrixIndex=0;
            for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
                HashMap<TIntHashSet, Integer> outputs = new HashMap<>();
                CMSet taskOutputs = ind.getTask(indexTask).getOutputs();
                if (taskOutputs.size() > 0) {
                    for (TIntHashSet subset : taskOutputs) {
                        outputs.put(subset, 0);
                    }
                    this.tokens.put(indexTask,outputs);
                    orderTasksLocal.put(indexTask, matrixIndex);
                    matrixIndex++;
                }
            }

            return orderTasksLocal;
        }

        private void restartTokens() {
            for (Map.Entry<Integer, HashMap<TIntHashSet, Integer>> t : tokens.entrySet()) {
                for (TIntHashSet subset : t.getValue().keySet()) {
                    t.getValue().put(subset, 0);
                }
            }
        }

        public int getPos() {
            return pos;
        }

        public HashMap<Integer, HashMap<TIntHashSet, Integer>> getTokens() {
            return tokens;
        }

        public CMSubsetsMapped getSubsetsMapped() {
            return subsetsMapped;
        }

        public int getTareaFinalModelo() {
            return tareaFinalModelo;
        }

        public int getNumOfTasks() {
            return numOfTasks;
        }

        public int getStartTask() {
            return startTask;
        }

        public int getNumOfTokens() {
            return numOfTokens;
        }

        public TIntObjectMap<CMTask> getTasks() {
            return tasks;
        }

        public int getStartPlace() {
            return startPlace;
        }

        public int getNumOfPosibleTasks() {
            return numOfPosibleTasks;
        }

        public int getEndPlace() {
            return endPlace;
        }

        public int[][] getMatrix() {
            return matrix;
        }

        public HashMap<Integer, Integer> getOrderTasks() {
            return orderTasks;
        }

        public TIntHashSet getPossibleEnabledTasks() {
            return possibleEnabledTasks;
        }

        public StateMove getMov() {
            return mov;
        }

        public void setMov(StateMove mov) {
            this.mov = mov;
        }

        public int getTarea() {
            return tarea;
        }

        public void setTokens(HashMap<Integer, HashMap<TIntHashSet, Integer>> tokens) {
            this.tokens = tokens;
        }

        public void setPossibleEnabledTasks(TIntHashSet possibleEnabledTasks) {
            this.possibleEnabledTasks = possibleEnabledTasks;
        }

        public TIntHashSet getTareas() {
            return this.getEnabledElements();
        }

        public void setTarea(Integer tarea) {
            this.tarea = tarea;
        }

        public void avanzarTarea() {
            pos++;
        }

        //La tarea final se ha ejecutado y no quedan tareas activas
        public boolean finalModelo() {
            return endPlace > 0;
        }

        //Devuelve TRUE cuando quedan tokens en el modelo y FALSE cuando no quedan
        public boolean sinTokens() {
            return numOfTokens <= 1;
        }

        public boolean finalModelo(CMIndividual ind) {
            //Si el modelo NO tiene tarea final
            if (tareaFinalModelo == -1) {
                //System.out.println(marcado.toString());
                return sinTokens();
            } else {
                return false;
            }
        }

        //Operaciones del marcado
        public int executeMatrix(int currentTaskID) {
            final CMTask currentTask = this.tasks.get(currentTaskID);
            this.bestCombination = getBestCombination(currentTask, currentTaskID);
            consumeInputs(currentTask, currentTaskID, this.bestCombination.getTasks());
            enableOutputs(currentTask, currentTaskID);
            return this.bestCombination.getNumMissingTokens();
        }

        public void executeTokens() {
            final CMTask currentTask = this.tasks.get(tarea);
            consumeInputsTokens(currentTask, tarea, this.bestCombination.getTasks());
            enableOutputsTokens(currentTask, tarea);
            this.possibleEnabledTasks.addAll(currentTask.getOutputs().getUnionSubsets());
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
                while (iter.hasNext()) {
                    final int task = iter.next();
                    final CMSet subsets = this.subsetsMapped.getRelatedElemens(currentTaskID, task);
                    if (subsets != null) {
                        for (TIntHashSet subset : subsets) {
                            int currentTokens = this.tokens.get(task).get(subset);
                            if (currentTokens > 0) {
                                this.numOfTokens--;
                                //this.tokens.get(task).put(subset, --currentTokens);

                                TIntIterator iterator = subset.iterator();
                                while (iterator.hasNext()) {
                                    int next = iterator.next();
                                    Integer row = orderTasks.get(task);
                                    Integer col = orderTasks.get(next);
                                    if (row != null && col != null) {
                                        matrix[row][col]--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private void consumeInputsTokens(CMTask currentTask, int currentTaskID, TIntHashSet activities) {
            if (!currentTask.getInputs().isEmpty()) {
                TIntIterator iter = activities.iterator();
                while (iter.hasNext()) {
                    final int task = iter.next();
                    final CMSet subsets = this.subsetsMapped.getRelatedElemens(currentTaskID, task);
                    if (subsets != null) {
                        for (TIntHashSet subset : subsets) {
                            int currentTokens = this.tokens.get(task).get(subset);
                            if (currentTokens > 0) {
                                this.tokens.get(task).put(subset, --currentTokens);
                            }
                        }
                    }
                }
            }
        }

        private void enableOutputsTokens(CMTask currentTask, int currentTaskID) {
            final CMSet outputs = currentTask.getOutputs();
            final int outputsSize = outputs.size();
            if (outputsSize != 0) {
                for (TIntHashSet subset : outputs) {
                    increaseTokens2(currentTaskID, subset);
                }
            }
        }

        private int increaseTokens2(int currentTask, TIntHashSet subset) {
            int currentTokens = this.tokens.get(currentTask).get(subset);
            this.tokens.get(currentTask).put(subset, ++currentTokens);

            return currentTokens;
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
                            final int task = SubsetsUtil.getPos(subset, new Random().nextInt(subset.size()));
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

        private TIntHashSet getTasksWithEmptyOutputPlaces(int task) {
            TIntHashSet inputTasks = this.tasks.get(task).getInputs().getUnionSubsets();
            TIntHashSet tasksEmptyOutPlaces = new TIntHashSet();
            TIntIterator iter = inputTasks.iterator();
            while (iter.hasNext()) {
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
            FiringCombination bestLocalCombination = getBestCombination(this.tasks.get(elem), elem);
            return (bestLocalCombination.getNumMissingTokens() > 0) ? false : true;
        }

        public TIntHashSet getEnabledElements() {
            for (TIntIterator it = possibleEnabledTasks.iterator(); it.hasNext();) {
                int currentTaskID = it.next();
                if (!isEnabled(currentTaskID)) {
                    it.remove();
                }
            }
            return possibleEnabledTasks;
        }

        private void increaseTokens(int currentTask, TIntHashSet subset) {
            //int currentTokens = this.tokens.get(currentTask).get(subset);
            //this.tokens.get(currentTask).put(subset, ++currentTokens);

            TIntIterator iterator = subset.iterator();
            while (iterator.hasNext()) {
                int next = iterator.next();
                Integer row = orderTasks.get(currentTask);
                Integer col = orderTasks.get(next);
                if (row != null && col != null) {
                    matrix[row][col]++;
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state = (State) o;

            if (pos != state.pos) return false;
            if (tareaFinalModelo != state.tareaFinalModelo) return false;
            if (numOfTasks != state.numOfTasks) return false;
            if (numOfPosibleTasks != state.numOfPosibleTasks) return false;
            if (startPlace != state.startPlace) return false;
            if (numOfTokens != state.numOfTokens) return false;
            if (endPlace != state.endPlace) return false;
            if (startTask != state.startTask) return false;
            if (!tasks.equals(state.tasks)) return false;
            if (!subsetsMapped.equals(state.subsetsMapped)) return false;
            if (!Arrays.deepEquals(matrix, state.matrix)) return false;
            return orderTasks.equals(state.orderTasks);
        }

        @Override
        public int hashCode() {
            int result = pos;
            result = 31 * result + tareaFinalModelo;
            result = 31 * result + numOfTasks;
            result = 31 * result + numOfPosibleTasks;
            result = 31 * result + tasks.hashCode();
            result = 31 * result + subsetsMapped.hashCode();
            result = 31 * result + startPlace;
            result = 31 * result + numOfTokens;
            result = 31 * result + endPlace;
            result = 31 * result + startTask;
            result = 31 * result + Arrays.deepHashCode(matrix);
            result = 31 * result + orderTasks.hashCode();
            return result;
        }
    }
}
