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
    private static Trace trace = null;

    public static void problem(String logfile, String netfile) {

        //Read Files
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();
        Object[] petrinetWithMarking = mapping.getPetrinetWithMarking();

        Petrinet petrinet = (Petrinet) petrinetWithMarking[0];
        Marking marking = (Marking) petrinetWithMarking[1];

        XLog log = mapping.getLog();
        MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
        TransEvClassMapping transEvClassMapping = MappingUtils.getTransEvClassMapping(mapping, petrinet, log);
        XEventClassifier classifier = transEvClassMapping.getEventClassifier();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
        XEventClasses classes = summary.getEventClasses();

        //Initial and final marking
        Marking initMarking = PetrinetUtils.getInitialMarking(petrinet);
        Marking finaMarking = PetrinetUtils.getFinalMarking(petrinet);

        IPNReplayParamProvider provider = new PetrinetReplayerWithoutILP().constructParamProvider(new FakePluginContext(),
                petrinet, log, transEvClassMapping);
        JComponent paramUI = provider.constructUI();

        CostBasedCompleteParam parameters = (CostBasedCompleteParam) provider.constructReplayParameter(paramUI);
        Map<org.processmining.models.graphbased.directed.petrinet.elements.Transition, Integer> mapTrans2Cost = parameters.getMapTrans2Cost();
        Map<XEventClass, Integer> mapEvClass2Cost = parameters.getMapEvClass2Cost();

        AbstractPDelegate<?> delegate = new PNaiveDelegate(petrinet, log, classes, transEvClassMapping, mapTrans2Cost,
                mapEvClass2Cost, 1000, false, finaMarking);


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

        long total_time = 0;
        List<WeightedNode> solutions = new ArrayList<>();

        //For each trace
        for (int i = 0; i < log.size(); i++) {
            final StateLikeCoBeFra initialState = new StateLikeCoBeFra(delegate, initMarking, log.get(i));
            TIntList unUsedIndices = new TIntArrayList();
            TIntIntMap trace2orgTrace = new TIntIntHashMap(log.get(i).size(), 0.5f, -1, -1);
            trace = getLinearTrace(log, i, delegate, unUsedIndices, trace2orgTrace);

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
            double mejorScore = 0d;
            boolean parar = false;

            long time_start, time_end;
            time_start = System.currentTimeMillis();

            AStar<StateMoveCoBeFra, StateLikeCoBeFra, Double, WeightedNode<StateMoveCoBeFra, StateLikeCoBeFra, Double>> astar = Hipster.createAStar(p);
            AStar.Iterator it = astar.iterator();

            while (it.hasNext()) {
                WeightedNode n1 = (WeightedNode) it.next();
                StateLikeCoBeFra state = (StateLikeCoBeFra) n1.state();

                double score = (double) n1.getScore();

                if (parar) {
                    if (score > mejorScore) {
                        break;
                    }
                }

                if (state.isFinal(delegate)) {
                    parar = true;
                    if (mejorScore == 0) {
                        mejorScore = (double) n1.getCost();
                        n = n1;
                    } else {
                        double aux = (double) n1.getCost();
                        if (aux < mejorScore) {
                            mejorScore = aux;
                            n = n1;
                        }
                    }
                }
            }

            if (parar == false) {
                System.err.print("Error. No se encontró ningún aligment para la traza nº " + i);
                System.exit(2);
            }

            time_end = System.currentTimeMillis();
            total_time = total_time + (time_end - time_start);

            solutions.add(n);
        }

        printSolutions(solutions, delegate);
    }

    private static Iterable<StateMoveCoBeFra> validMovementsFor(StateLikeCoBeFra state,
                                                                                  Delegate<? extends Head, ? extends Tail> delegate,
                                                                                  Trace trace) {
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

        return possibleMovs;
    }

    private static StateLikeCoBeFra applyActionToState(StateMoveCoBeFra action,
                                                                         StateLikeCoBeFra state,
                                                                         Delegate<? extends Head, ? extends Tail> delegate) {
        timerAct.resume();
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
                    salida = salida + "\n\t>>\t" + s.getModelMove();
                } else if (node.action().equals(LOG)) {
                    salida = salida + "\n\t" + s.getLogMove() + "\t>>";
                } else {
                    salida = salida + "\n\n??????????????????????";
                }
            }
            salida = salida + "\n\nCoste del alineamiento = " + finalNode.getScore();
        }
        System.out.println(salida);
    }
}
