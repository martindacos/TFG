package es.usc.citius.aligments.algoritmos;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import es.usc.citius.aligments.problem.LogMove;
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

import javax.swing.*;
import java.util.*;

import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.*;
import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.StateMoveCoBeFra.LOG;
import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.StateMoveCoBeFra.MODEl;
import static es.usc.citius.aligments.problem.NStateLikeCoBeFra.StateMoveCoBeFra.SYNC;
import static nl.tue.astar.AStarThread.NOMOVE;

public class AlgorithmAStar {

    private static es.usc.citius.aligments.utils.Timer timerAct = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerMovs = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerTotal = new es.usc.citius.aligments.utils.Timer();
    private static int contadorInstanciasMarcado = 0;

    private static Trace trace = null;

    private static AbstractPDelegate<?> delegate;
    private static XLog log;
    private static Marking initMarking;

    public static void problem(String logLile, String netFile) {

        readFiles(logLile, netFile);

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMoveCoBeFra, StateLikeCoBeFra> af = new ActionFunction<StateMoveCoBeFra, StateLikeCoBeFra>() {
            @Override
            public Iterable<StateMoveCoBeFra> actionsFor(StateLikeCoBeFra state) {
                return AlgorithmAStar.validMovementsFor(state, delegate, trace);
            }
        };

        ActionStateTransitionFunction<StateMoveCoBeFra, StateLikeCoBeFra> atf;
        atf = new ActionStateTransitionFunction<StateMoveCoBeFra, StateLikeCoBeFra>() {
            @Override
            public StateLikeCoBeFra apply(StateMoveCoBeFra action, StateLikeCoBeFra state) {
                return AlgorithmAStar.applyActionToState(action, state, delegate);
            }
        };

        CostFunction<StateMoveCoBeFra, StateLikeCoBeFra, Double> cf = new CostFunction<StateMoveCoBeFra, StateLikeCoBeFra, Double>() {
            @Override
            public Double evaluate(Transition<StateMoveCoBeFra, StateLikeCoBeFra> transition) {
                return AlgorithmAStar.evaluateToState(transition);
            }
        };

        HeuristicFunction<StateLikeCoBeFra, Double> hf = new HeuristicFunction<StateLikeCoBeFra, Double>() {
            @Override
            public Double estimate(StateLikeCoBeFra state) {
                return 0d;
            }
        };

        List<WeightedNode> solutions = new ArrayList<>();
        Map<Trace, Integer> tracesRepetitions = new HashMap<>();
        Map<Trace, XTrace> xTraces = new HashMap<>();

        timerTotal.start();
        //Detect duplicated traces
        for (int i = 0; i < log.size(); i++) {
            TIntList unUsedIndices = new TIntArrayList();
            TIntIntMap trace2orgTrace = new TIntIntHashMap(log.get(i).size(), 0.5f, -1, -1);
            Trace localTrace = getLinearTrace(log, i, delegate, unUsedIndices, trace2orgTrace);
            if (tracesRepetitions.containsKey(localTrace)) {
                tracesRepetitions.put(localTrace, tracesRepetitions.get(localTrace) + 1);
            } else {
                tracesRepetitions.put(localTrace, 1);
                xTraces.put(localTrace, log.get(i));
            }
        }

        for (Map.Entry<Trace, Integer> entry : tracesRepetitions.entrySet()) {
            //TODO Do something with repetitions
            Integer repetitions = entry.getValue();
            XTrace xTrace = xTraces.get(entry.getKey());
            final StateLikeCoBeFra initialState = new StateLikeCoBeFra(delegate, initMarking, xTrace);
            trace = entry.getKey();

            //Definimos el problema de búsqueda
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

            long time_start, time_end;
            time_start = System.currentTimeMillis();

            AStar<StateMoveCoBeFra, StateLikeCoBeFra, Double, WeightedNode<StateMoveCoBeFra, StateLikeCoBeFra, Double>> astar = Hipster.createAStar(p);
            AStar.Iterator it = astar.iterator();

            while (it.hasNext()) {
                WeightedNode n1 = (WeightedNode) it.next();
                StateLikeCoBeFra state = (StateLikeCoBeFra) n1.state();

                double score = (double) n1.getScore();

                if (stop) {
                    if (score > bestScore) {
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
                }
            }

            if (stop == false) {
                System.err.print("Error. No se encontró ningún aligment para la traza " + trace.toString());
                System.exit(2);
            }

            time_end = System.currentTimeMillis();
            // (time_end - time_start); Traces calculation time

            solutions.add(n);
        }
        timerTotal.stop();

        printTimes();
        //printSolutions(solutions, delegate);
    }

    private static void printTimes() {
        System.out.println("Tiempo cálculo movimientos : " + timerMovs.getReadableElapsedTime());
        System.out.println("Tiempo aplicar movimientos : " + timerAct.getReadableElapsedTime());
        System.out.println("Tiempo cálculo total : " + timerTotal.getReadableElapsedTime());
        System.out.println("Nº Instancias marcado : " + contadorInstanciasMarcado);
    }

    private static Iterable<StateMoveCoBeFra> validMovementsFor(StateLikeCoBeFra state,
                                                                                  Delegate<? extends Head, ? extends Tail> delegate,
                                                                                  Trace trace) {
        timerMovs.resume();
        List<StateMoveCoBeFra> possibleMovs = new ArrayList<>();

        TIntList enabled = state.getModelMoves(delegate);

        TIntCollection nextEvents = trace.getNextEvents(state.getExecuted());
        TIntIterator evtIt = nextEvents.iterator();

        while (evtIt.hasNext()) {
            int nextEvent = evtIt.next();

            TIntList ml;

            // move both log and model synchronously;
            int activity = trace.get(nextEvent);
            ml = state.getSynchronousMoves(delegate, enabled, activity);
            TIntIterator it = ml.iterator();
            while (it.hasNext()) {
                possibleMovs.add(SYNC);
                int trans = it.next();
                SyncMove mov = new SyncMove(trans, nextEvent, activity);
                state.addSyncMovement(mov);
            }

            //Log Move
            possibleMovs.add(LOG);
            LogMove mov = new LogMove(nextEvent, activity);
            state.addLogMovement(mov);
        }

        //Model move
        TIntIterator it = enabled.iterator();
        while (it.hasNext()) {
            // move model
            it.next();
            possibleMovs.add(MODEl);
        }

        timerMovs.pause();
        return possibleMovs;
    }

    private static StateLikeCoBeFra applyActionToState(StateMoveCoBeFra action,
                                                                         StateLikeCoBeFra state,
                                                                         Delegate<? extends Head, ? extends Tail> delegate) {
        timerAct.resume();
        contadorInstanciasMarcado++;
        final AbstractPDelegate<?> d = (AbstractPDelegate<?>) delegate;
        StateLikeCoBeFra successor = null;

        switch (action) {
            case SYNC:
                SyncMove syncMove = state.getAndDeleteSyncMovement();
                successor = processMove(state, syncMove.getModelMove(), syncMove.getMovedEvent(), syncMove.getActivity(), d);
                break;
            case MODEl:
                successor = processMove(state, state.getAndDeleteModelMovement(), NOMOVE, NOMOVE, d);
                break;
            case LOG:
                LogMove logMove = state.getAndDeleteLogMovement();
                successor = processMove(state, NOMOVE, logMove.getMovedEvent(), logMove.getActivity(), d);
                break;
        }

        timerAct.pause();
        return successor;
    }

    private static Double evaluateToState(Transition<StateMoveCoBeFra, StateLikeCoBeFra> transition) {
        StateMoveCoBeFra action = transition.getAction();
        Double cost = null;
        switch (action) {
            case MODEl:
                cost = 1001d;
                break;
            case LOG:
                cost = 1001d;
                break;
            case SYNC:
                cost = 1d;
                break;
        }
        return cost;
    }

    /**
     * get list of event class. Record the indexes of non-mapped event classes.
     *
     * @param trace
     * @param unUsedIndices
     * @param trace2orgTrace
     * @return
     */
    protected static LinearTrace getLinearTrace(XLog log, int trace, AbstractPDelegate<?> delegate, TIntList unUsedIndices,
                                                TIntIntMap trace2orgTrace) {
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

    protected static StateLikeCoBeFra processMove(StateLikeCoBeFra state, int modelMove, int movedEvent, int activity,
                                                                    AbstractPDelegate<?> delegate) {

        // First, construct the next state from the old state
        final StateLikeCoBeFra newState = state.getNextHead(delegate, modelMove, movedEvent, activity);
        return newState;
    }

    //TODO Revise print
    public static void printSolutions(List<WeightedNode> solutions, AbstractPDelegate<?> delegate) {
        String salida = "";
        for (WeightedNode finalNode : solutions) {
            Iterator it2 = finalNode.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            salida = salida + "\n***************************";
            salida = salida + "\n\n---------SALIDA VISUAL----------";
            salida = salida + "\n\tTRAZA\tMODELO";
            while (it2.hasNext()) {
                AbstractNode node = (WeightedNode) it2.next();

                StateLikeCoBeFra s = (StateLikeCoBeFra) node.state();

                if (node.action().equals(SYNC)) {
                    salida = salida + "\n\t" + delegate.getTransition((short) s.getLogMove())+
                            "\t" + delegate.getTransition((short) s.getLogMove());
                } else if (node.action().equals(MODEl)) {
                    salida = salida + "\n\t>>\t" + delegate.getTransition((short) s.getModelMove());
                } else if (node.action().equals(LOG)) {
                    salida = salida + "\n\t" + delegate.getTransition((short) s.getLogMove()) + "\t>>";
                } else {
                    salida = salida + "\n\n??????????????????????";
                }
            }
            salida = salida + "\n\nCoste del alineamiento = " + finalNode.getScore();
        }
        System.out.println(salida);
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

        delegate = new PNaiveDelegate(petrinet, log, classes, transEvClassMapping, mapTrans2Cost,
                mapEvClass2Cost, 1000, false, finaMarking);
    }
}
