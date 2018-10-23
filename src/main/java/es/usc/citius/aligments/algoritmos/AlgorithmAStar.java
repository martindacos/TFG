package es.usc.citius.aligments.algoritmos;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import es.usc.citius.aligments.problem.LogMove;
import es.usc.citius.aligments.problem.NStateLikeCoBeFra;
import es.usc.citius.aligments.problem.PossibleMovements;
import es.usc.citius.aligments.problem.SyncMove;
import es.usc.citius.hipster.algorithm.AStar;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.ActionFunction;
import es.usc.citius.hipster.model.function.ActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.ProblemBuilder;
import es.usc.citius.hipster.model.problem.SearchProblem;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import nl.tue.astar.*;
import nl.tue.astar.util.LinearTrace;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.impl.AbstractPDelegate;
import org.processmining.plugins.astar.petrinet.impl.PNaiveDelegate;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import javax.swing.*;
import java.util.*;

import static es.usc.citius.aligments.config.Parametros.*;
import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.*;
import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.StateMoveCoBeFra.*;
import static nl.tue.astar.AStarThread.NOMOVE;

public class AlgorithmAStar {

    private static es.usc.citius.aligments.utils.Timer timerAct = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerMovs = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerTotal = new es.usc.citius.aligments.utils.Timer();
    private static int stateCount;
    private static int queuedStateCount;
    private static int traversedArcCount;
    private static long aligmentTime;

    private static Trace trace = null;

    private static AbstractPDelegate<?> delegate;
    private static XLog log;
    private static Marking initMarking;
    private static PossibleMovements possibleMovements;

    //A* Functions
    private static ActionFunction<StateMoveCoBeFra, StateLikeCoBeFra> af;
    private static ActionStateTransitionFunction<StateMoveCoBeFra, StateLikeCoBeFra> atf;
    private static CostFunction<StateMoveCoBeFra, StateLikeCoBeFra, Double> cf;
    private static HeuristicFunction<StateLikeCoBeFra, Double> hf;

    public static PNRepResultImpl problem(String logFile, String netFile) {
        readFiles(logFile, netFile);

        af = state -> AlgorithmAStar.validMovementsFor(state);

        atf = (action, state) -> AlgorithmAStar.applyActionToState(action, state);

        cf = transition -> AlgorithmAStar.evaluateToState(transition);

        hf = state -> 0d;

        List<SyncReplayResult> solutions = new ArrayList<>();
        Map<Trace, SortedSet<Integer>> xTraces = new HashMap<>();

        timerTotal.start();
        //Detect duplicated traces
        for (int i = 0; i < log.size(); i++) {
            TIntList unUsedIndices = new TIntArrayList();
            TIntIntMap trace2orgTrace = new TIntIntHashMap(log.get(i).size(), 0.5f, -1, -1);
            Trace localTrace = getLinearTrace(log, i, unUsedIndices, trace2orgTrace);
            if (xTraces.containsKey(localTrace)) {
                SortedSet<Integer> repetitionsIndex = xTraces.get(localTrace);
                repetitionsIndex.add(i);
                xTraces.put(localTrace, repetitionsIndex);
            } else {
                SortedSet<Integer> repetitions = new TreeSet<>();
                repetitions.add(i);
                xTraces.put(localTrace, repetitions);
            }
        }

        WeightedNode n;
        int minCostModel = getMinCostModel();

        for (Map.Entry<Trace, SortedSet<Integer>> entry : xTraces.entrySet()) {
            SortedSet<Integer> repetitionsIndex = entry.getValue();
            XTrace xTrace = log.get(repetitionsIndex.first());
            final StateLikeCoBeFra initialState = new StateLikeCoBeFra(delegate, initMarking, xTrace);
            trace = entry.getKey();

            stateCount = 0;
            queuedStateCount = 0;
            aligmentTime = 0l;
            traversedArcCount = 0;
            n = getOptimalAligment(initialState);

            SyncReplayResult result = addReplayResult(n, repetitionsIndex, stateCount, true, aligmentTime,
                    queuedStateCount, traversedArcCount, minCostModel);
            solutions.add(result);
        }
        timerTotal.stop();

        //printTimes();

        PNRepResultImpl syncReplayResults = new PNRepResultImpl(solutions);
        PNRepResultImpl sol = new PNRepResultImpl(syncReplayResults);

        return sol;
    }

    private static int getMinCostModel() {
        WeightedNode n;
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XTrace emptyTrace = factory.createTrace();
        trace = new LinearTrace("Empty Trace", new TIntArrayList());

        final StateLikeCoBeFra initialStateEmpty = new StateLikeCoBeFra(delegate, initMarking, emptyTrace);
        int minCostModel = 0;

        n = getOptimalAligment(initialStateEmpty);

        if (n != null) {
            Double finalCost = (double) n.getCost();
            Double delta = delegate.getDelta();
            Iterator iteratorEmptyTrace = n.path().iterator();
            iteratorEmptyTrace.next();
            int backTraceSize = 0;
            while (iteratorEmptyTrace.hasNext()) {
                AbstractNode node = (WeightedNode) iteratorEmptyTrace.next();
                if (node.action().equals(MODEl) || node.action().equals(INVISIBLE)) backTraceSize += delegate.getEpsilon();
            }
            assert (finalCost - backTraceSize) % delta.intValue() == 0;
            minCostModel = (finalCost.intValue() - backTraceSize) / delta.intValue();
        }

        //System.out.println("Time to calculate aligment of empty trace : " + aligmentTime);
        return minCostModel;
    }

    private static WeightedNode getOptimalAligment(StateLikeCoBeFra initialState) {
        SearchProblem<StateMoveCoBeFra, StateLikeCoBeFra, WeightedNode<StateMoveCoBeFra, StateLikeCoBeFra, Double>> p
                = ProblemBuilder.create()
                .initialState(initialState)
                .defineProblemWithExplicitActions()
                .useActionFunction(af)
                .useTransitionFunction(atf)
                .useCostFunction(cf)
                .useHeuristicFunction(hf)
                .build();

        WeightedNode n = null;
        double bestScore = 0d;
        boolean stop = false;

        long time_start = System.currentTimeMillis();

        AStar<StateMoveCoBeFra, StateLikeCoBeFra, Double, WeightedNode<StateMoveCoBeFra, StateLikeCoBeFra, Double>> astar = Hipster.createAStar(p);
        AStar.Iterator it = astar.iterator();

        while (it.hasNext()) {
            WeightedNode n1 = (WeightedNode) it.next();
            StateLikeCoBeFra state = (StateLikeCoBeFra) n1.state();

            double cost = (double) n1.getCost();

            if (stop) {
                if (cost > bestScore) {
                    queuedStateCount = it.getOpen().size();
                    break;
                }
            }

            if (state.isFinal(delegate)) {
                stop = true;
                if (bestScore == 0) {
                    bestScore = (double) n1.getCost();
                    n = n1;
                } else {
                    double aux = (double) n1.getCost();
                    if (aux < bestScore) {
                        bestScore = aux;
                        n = n1;
                    }
                }
                //TODO Maybe is not correct. CoBeFra do this.
                queuedStateCount = it.getOpen().size() + stateCount;
                break;
            }
        }

        if (stop == false) {
            System.err.print("Error. No se encontró ningún aligment para la traza " + trace.toString());
            System.exit(2);
        }

        long time_end = System.currentTimeMillis();
        aligmentTime = time_end - time_start;

        return n;
    }

    private static void printTimes() {
        System.out.println("Tiempo cálculo movimientos : " + timerMovs.getReadableElapsedTime());
        System.out.println("Tiempo aplicar movimientos : " + timerAct.getReadableElapsedTime());
        System.out.println("Tiempo cálculo total : " + timerTotal.getReadableElapsedTime());
    }

    private static Iterable<StateMoveCoBeFra> validMovementsFor(StateLikeCoBeFra state) {
        timerMovs.resume();
        stateCount++;
        List<StateMoveCoBeFra> possibleMovs = new ArrayList<>();

        TIntList modelMoves;
        TIntList invisibleMoves;

        possibleMovements = new PossibleMovements();

        TIntCollection nextEvents = trace.getNextEvents(state.getExecuted());
        TIntIterator evtIt = nextEvents.iterator();

        while (evtIt.hasNext()) {
            int nextEvent = evtIt.next();

            TIntList ml;

            // move both log and model synchronously;
            int activity = trace.get(nextEvent);
            ml = state.getSynchronousMoves(delegate, activity);
            TIntIterator it = ml.iterator();
            while (it.hasNext()) {
                possibleMovs.add(SYNC);
                int trans = it.next();
                SyncMove mov = new SyncMove(trans, nextEvent, activity);
                possibleMovements.addSyncMovement(mov);
            }

            //Log Move
            possibleMovs.add(LOG);
            LogMove mov = new LogMove(nextEvent, activity);
            possibleMovements.addLogMovement(mov);
        }

        if (isValidMoveOnModel(state)) {
            //Save Enabled moves on state
            modelMoves = state.getModelMoves(delegate);

            //Model move
            for (int i = 0; i < modelMoves.size(); i++) {
                possibleMovs.add(MODEl);
            }

            //Invisible move
            invisibleMoves = state.getInvisible();
            for (int i = 0; i < invisibleMoves.size(); i++) {
                possibleMovs.add(INVISIBLE);
            }

            possibleMovements.setModel(modelMoves);
            possibleMovements.setInvisible(invisibleMoves);
        }

        timerMovs.pause();

        return possibleMovs;
    }

    private static boolean isValidMoveOnModel(StateLikeCoBeFra state) {
        return state.getPreviousMove() == null || state.getModelMove() != NOMOVE;
    }

    private static StateLikeCoBeFra applyActionToState(StateMoveCoBeFra action, StateLikeCoBeFra state) {
        timerAct.resume();
        StateLikeCoBeFra successor = null;

        switch (action) {
            case SYNC:
                SyncMove syncMove = possibleMovements.getAndDeleteSyncMovement();
                successor = processMove(state, syncMove.getModelMove(), syncMove.getMovedEvent(), syncMove.getActivity(), SYNC);
                break;
            case LOG:
                LogMove logMove = possibleMovements.getAndDeleteLogMovement();
                successor = processMove(state, NOMOVE, logMove.getMovedEvent(), logMove.getActivity(), LOG);
                break;
            case MODEl:
                successor = processMove(state, possibleMovements.getAndDeleteModelMovement(), NOMOVE, NOMOVE, MODEl);
                break;
            case INVISIBLE:
                successor = processMove(state, possibleMovements.getAndDeleteInvisibleMovement(), NOMOVE, NOMOVE, INVISIBLE);
                break;
        }

        timerAct.pause();
        return successor;
    }

    private static Double evaluateToState(Transition<StateMoveCoBeFra, StateLikeCoBeFra> transition) {
        StateMoveCoBeFra action = transition.getAction();
        Double cost = null;
        switch (action) {
            case LOG:
                cost = delegate.getEpsilon() + delegate.getDelta() * LOG_COST;
                break;
            case SYNC:
                cost = delegate.getEpsilon() + delegate.getDelta() * SYNC_COST;
                break;
            case MODEl:
                cost = delegate.getEpsilon() + delegate.getDelta() * MODEL_COST;
                break;
            case INVISIBLE:
                cost = delegate.getEpsilon() + delegate.getDelta() * INVISIBLE_COST;
                break;
        }
        return cost;
    }

    protected static LinearTrace getLinearTrace(XLog log, int trace, TIntList unUsedIndices, TIntIntMap trace2orgTrace) {
        int s = log.get(trace).size();
        String name = XConceptExtension.instance().extractName(log.get(trace));
        if (name == null || name.isEmpty()) {
            name = "Trace " + trace;
        }
        TIntList activities = new TIntArrayList(s);
        for (int i = 0; i < s; i++) {
            int act = delegate.getActivityOf(trace, i);
            if (act != NOMOVE) {
                trace2orgTrace.put(activities.size(), i);
                activities.add(act);
            } else {
                unUsedIndices.add(i);
            }
        }

        LinearTrace result = new LinearTrace(name, activities);

        return result;
    }

    protected static StateLikeCoBeFra processMove(StateLikeCoBeFra state, int modelMove, int movedEvent, int activity, StateMoveCoBeFra move) {

        // First, construct the next state from the old state
        final StateLikeCoBeFra newState = state.getNextHead(delegate, modelMove, movedEvent, activity, move);
        traversedArcCount++;

        return newState;
    }

    private static void readFiles(String logFile, String netFile) {
        Mapping mapping = new Mapping(logFile, netFile);
        mapping.assignUnmappedToInvisible();
        Object[] petrinetWithMarking = mapping.getPetrinetWithMarking();

        Petrinet petrinet = (Petrinet) petrinetWithMarking[0];
        //Marking marking = (Marking) petrinetWithMarking[1];

        log = mapping.getLog();
        MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
        TransEvClassMapping transEvClassMapping = MappingUtils.getTransEvClassMapping(mapping, petrinet, log);
        XEventClassifier classifier = transEvClassMapping.getEventClassifier();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
        XEventClasses classes = summary.getEventClasses();

        //Initial and final marking
        initMarking = PetrinetUtils.getInitialMarking(petrinet);
        Marking finaMarking = PetrinetUtils.getFinalMarking(petrinet);

        IPNReplayParamProvider provider = new PetrinetReplayerWithoutILP().constructParamProvider(new FakePluginContext(),
                petrinet, log, transEvClassMapping);
        JComponent paramUI = provider.constructUI();

        CostBasedCompleteParam parameters = (CostBasedCompleteParam) provider.constructReplayParameter(paramUI);
        Map<org.processmining.models.graphbased.directed.petrinet.elements.Transition, Integer> mapTrans2Cost = parameters.getMapTrans2Cost();
        Map<XEventClass, Integer> mapEvClass2Cost = parameters.getMapEvClass2Cost();

        //Difference with cost SYNC
        int delta = 1000;
        delegate = new PNaiveDelegate(petrinet, log, classes, transEvClassMapping, mapTrans2Cost,
                mapEvClass2Cost, delta, false, finaMarking);
    }

    protected static SyncReplayResult addReplayResult(WeightedNode node, SortedSet<Integer> tracesIndex, int stateCount, boolean isReliable, long milliseconds,
                                                      int queuedStates, int traversedArcs, int minCostMoveModel) {
        double mmCost = 0; // total cost of move on model
        double mlCost = 0; // total cost of move on log
        double mSyncCost = 0; // total cost of synchronous move

        double mmUpper = 0; // total cost if all movements are move on model (including the synchronous one)
        double mlUpper = 0; // total cost if all events are move on log

        int eventInTrace = 0;
        List<StepTypes> stepTypes = new ArrayList<>(node.pathSize());
        List<Object> nodeInstance = new ArrayList<>(node.pathSize());

        Iterator iterator = node.path().iterator();
        //First element is null
        iterator.next();
        while (iterator.hasNext()) {
            AbstractNode actualNode = (WeightedNode) iterator.next();
            StateLikeCoBeFra state = (StateLikeCoBeFra) actualNode.state();

            if (actualNode.action().equals(SYNC)) {
                eventInTrace++;
                stepTypes.add(StepTypes.LMGOOD);
                nodeInstance.add(delegate.getTransition((short) state.getModelMove()));
                mSyncCost += SYNC_COST;
                mmUpper += MODEL_COST;
                mlUpper += LOG_COST;
            } else if (actualNode.action().equals(MODEl)) {
                stepTypes.add(StepTypes.MREAL);
                nodeInstance.add(delegate.getTransition((short) state.getModelMove()));
                mmCost += MODEL_COST;
                mmUpper += MODEL_COST;
            } else if (actualNode.action().equals(LOG)) {
                eventInTrace++;
                stepTypes.add(StepTypes.L);
                short a = (short) state.getActivity();
                nodeInstance.add(delegate.getEventClass(a));
                mlCost += LOG_COST;
                mlUpper += LOG_COST;
            } else if (actualNode.action().equals(INVISIBLE)) {
                stepTypes.add(StepTypes.MINVI);
                nodeInstance.add(delegate.getTransition((short) state.getModelMove()));
            }
        }

        SyncReplayResult res = new SyncReplayResult(nodeInstance, stepTypes, tracesIndex.first());
        res.setTraceIndex(tracesIndex);

        res.setReliable(isReliable);
        Map<String, Double> info = new HashMap<>();
        info.put(PNRepResult.RAWFITNESSCOST, (mmCost + mlCost + mSyncCost));
        info.put(PNRepResult.MAXFITNESSCOST, (mlUpper + minCostMoveModel));
        info.put(PNRepResult.MAXMOVELOGCOST, (mlUpper));

        if (mlCost > 0) {
            info.put(PNRepResult.MOVELOGFITNESS, 1 - (mlCost / mlUpper));
        } else {
            info.put(PNRepResult.MOVELOGFITNESS, 1.0);
        }

        if (mmCost > 0) {
            info.put(PNRepResult.MOVEMODELFITNESS, 1 - (mmCost / mmUpper));
        } else {
            info.put(PNRepResult.MOVEMODELFITNESS, 1.0);
        }
        info.put(PNRepResult.NUMSTATEGENERATED, (double) stateCount);
        info.put(PNRepResult.QUEUEDSTATE, (double) queuedStates);
        info.put(PNRepResult.TRAVERSEDARCS, (double) traversedArcs);

        // set info fitness
        if (mmCost > 0 || mlCost > 0 || mSyncCost > 0) {
            info.put(PNRepResult.TRACEFITNESS, 1 - ((mmCost + mlCost + mSyncCost) / (mlUpper + minCostMoveModel)));
        } else {
            info.put(PNRepResult.TRACEFITNESS, 1.0);
        }
        info.put(PNRepResult.TIME, (double) milliseconds);
        info.put(PNRepResult.ORIGTRACELENGTH, (double) eventInTrace);
        res.setInfo(info);
        return res;
    }
}
