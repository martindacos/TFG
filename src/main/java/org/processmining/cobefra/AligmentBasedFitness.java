package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import be.kuleuven.econ.cbf.utils.MappingUtils;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm;
import nl.tue.astar.util.ilp.LPMatrixException;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.*;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import java.util.ArrayList;
import java.util.List;

import static es.usc.citius.aligments.config.Parametros.LOG_COST;
import static es.usc.citius.aligments.config.Parametros.MODEL_COST;
import static es.usc.citius.aligments.config.Parametros.SYNC_COST;

public class AligmentBasedFitness {

    public static AryaFitness calculate(String logfile, String netfile) {
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();
        //mapping.assignUnmappedToVisible();

        AryaFitness algorithm = new AryaFitness();
        //algorithm.setChosenAlgorithm(new PetrinetReplayerWithILP());
        //algorithm.setChosenAlgorithm(new PetrinetReplayerILPRestrictedMoveModel());
        //algorithm.setChosenAlgorithm(new PetrinetReplayerNoILPRestrictedMoveModel());
        //algorithm.setChosenAlgorithm(new PetrinetReplayerSSD());
        //algorithm.setChosenAlgorithm(new PetrinetSwapReplayer());
        //Probablemente funciona bien
        algorithm.setChosenAlgorithm(new PetrinetReplayerWithoutILP());
        //algorithm.setChosenAlgorithm(new PrefixBasedPetrinetReplayer());
        algorithm.setCreateInitialMarking(true);
        algorithm.setCreateFinalMarking(true);
        algorithm.load(mapping);
        algorithm.calculate();

        return algorithm;
    }

    public static PNRepResultImpl calculatePromSplit(String logfile, String netfile) {
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();

        Object[] petrinetWithMarking = mapping.getPetrinetWithMarking();

        Petrinet net = (Petrinet) petrinetWithMarking[0];
        //Marking marking = (Marking) petrinetWithMarking[1];

        XLog log = mapping.getLog();
        MappingUtils.setInvisiblesInPetrinet(mapping, net);
        TransEvClassMapping transEvClassMapping = MappingUtils.getTransEvClassMapping(mapping, net, log);
        XEventClassifier classifier = transEvClassMapping.getEventClassifier();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
        XEventClasses classes = summary.getEventClasses();

        //Initial and final marking
        Marking initMarking = PetrinetUtils.getInitialMarking(net);
        Marking finaMarking = PetrinetUtils.getFinalMarking(net);

        PNRepResultImpl results = doReplay(log, net, initMarking, finaMarking, classes, transEvClassMapping);
        return results;
    }

    //Call PROM replayer with split technique
    private static PNRepResultImpl doReplay(XLog log, Petrinet net, Marking initialMarking, Marking finalMarking, XEventClasses classes,
                                TransEvClassMapping mapping) {

        // Setup default parameters with 2 threads (Incremental A*)
        ReplayerParameters parameters = new ReplayerParameters.Default(1, ReplayAlgorithm.Debug.NONE);
        //ReplayerParameters parameters = new ReplayerParameters.Dijkstra();
        // Setup the replayer
        Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classes,
                mapping, false);
        // Set a timeout per trace in milliseconds
        int toms = 10 * 1000;
        // preprocessing time to be added to the statistics if necessary
        long preProcessTimeNanoseconds = 0;
        List<SyncReplayResult> solutions = new ArrayList<>();

        int minCostModel = 0;
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XTrace emptyTrace = factory.createTrace();
        TraceReplayTask emptyTask = new TraceReplayTask(replayer, parameters, emptyTrace, -1, toms, parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
        try {
            TraceReplayTask call = emptyTask.call();
            SyncReplayResult minCostResult = call.getSuccesfulResult();
            minCostModel = getCost(minCostResult).intValue();
        } catch (LPMatrixException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < log.size(); i++) {
            // Setup the trace replay task
            TraceReplayTask task = new TraceReplayTask(replayer, parameters, log.get(i), i, toms,
                    parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
            try {
                TraceReplayTask result = task.call();
                switch (result.getResult()) {
                    case DUPLICATE:
                        assert false; // cannot happen in this setting
                        //throw new Exception("Result cannot be a duplicate in per-trace computations.");
                    case FAILED:
                        // internal error in the construction of synchronous product or other error.
                        throw new RuntimeException("Error in alignment computations");
                    case SUCCESS:
                        // process succcesful execution of the replayer
                        SyncReplayResult replayResult = result.getSuccesfulResult();
                        // obtain the exit code of the replay algorithm
                        int ec = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();
                        if ((ec & Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
                            // Optimal alignment found.
                            // Handle further processing here.
                            // set info fitness
                            setFitnessInfo(replayResult, minCostModel);
                            solutions.add(replayResult);
                        } else if ((ec & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
                            // failure in the alignment. Error code shows more details.
                            // Handle further processing here.
                        }
                        // Additional exitcode information for failed alignments:
                        if ((ec & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
                        }
                        // in some marking, there were too many tokens in a place.
                        if ((ec & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
                        }
                        // in some marking, the cost function went through the upper limit of 2^24
                        if ((ec & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
                        }
                        // in some marking, the heuristic function went through the upper limit of 2^24
                        if ((ec & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED) {
                        }
                        // alignment failed with a timeout
                        if ((ec & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
                        }
                        // alignment failed due to reacing too many states.}
                        if ((ec & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
                        }
                        // no optimal alignment found with cost less or equal to the given limit.
                        if ((ec & Utils.CANCELED) == Utils.CANCELED) {
                        }
                        // user-cancelled.
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PNRepResultImpl syncReplayResults = new PNRepResultImpl(solutions);
        PNRepResultImpl sol = new PNRepResultImpl(syncReplayResults);

        return sol;
    }

    private static Double getCost(SyncReplayResult replayResult) {
        List<StepTypes> nodeInstance = replayResult.getStepTypes();
        double cost = 0;

        for (StepTypes step : nodeInstance) {
            switch (step) {
                case LMGOOD:
                    cost += LOG_COST;
                    break;
                case MREAL:
                    cost += MODEL_COST;
                    break;
            }
        }

        return cost;
    }

    private static void setFitnessInfo(SyncReplayResult replayResult, int minCostMoveModel) {
        List<StepTypes> nodeInstance = replayResult.getStepTypes();
        double mmCost = 0; // total cost of move on model
        double mlCost = 0; // total cost of move on log
        double mSyncCost = 0; // total cost of synchronous move

        double mlUpper = 0; // total cost if all events are move on log
        for (StepTypes step : nodeInstance) {
            switch (step) {
                case LMGOOD:
                    mSyncCost += SYNC_COST;
                    mlUpper += LOG_COST;
                    break;
                case MREAL:
                    mmCost += MODEL_COST;
                    break;
                case L:
                    mlCost += LOG_COST;
                    mlUpper += LOG_COST;
                    break;
            }
        }

        if (mmCost > 0 || mlCost > 0 || mSyncCost > 0) {
            replayResult.addInfo(PNRepResult.TRACEFITNESS, 1 - ((mmCost + mlCost + mSyncCost) / (mlUpper + minCostMoveModel)));
        } else {
            replayResult.addInfo(PNRepResult.TRACEFITNESS, 1.0);
        }
    }
}
