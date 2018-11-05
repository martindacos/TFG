package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.AryaPrecision;
import be.kuleuven.econ.cbf.metrics.precision.ETConformanceBestAlign;
import be.kuleuven.econ.cbf.metrics.precision.NegativeEventPrecisionMetric;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.NBestPrefixAlignmentsGraphGuessMarkingAlg;

public class AlignmentBasedPrecision {

    public static ETConformanceBestAlign ETConformanceBestAlign(String logfile, String netfile) {
        es.usc.citius.aligments.utils.Timer timer = new es.usc.citius.aligments.utils.Timer();
        timer.start();
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();
        //mapping.assignUnmappedToVisible();

        ETConformanceBestAlign algorithm = new ETConformanceBestAlign();
        algorithm.setGamma(0.0);
        algorithm.setChosenAlgorithm(new NBestPrefixAlignmentsGraphGuessMarkingAlg());
        algorithm.setCreateInitialMarking(true);
        algorithm.setCreateFinalMarking(true);
        algorithm.load(mapping);
        algorithm.calculate();
        timer.stop();
        System.out.println("Tiempo total ETConformanceBestAlign : " + timer.getReadableElapsedTime() + "\n ");

        System.out.println(algorithm.getResult());

        return algorithm;
    }

    public static NegativeEventPrecisionMetric negativeEventPrecision(String logfile, String netfile) {
        es.usc.citius.aligments.utils.Timer timer = new es.usc.citius.aligments.utils.Timer();
        timer.start();
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();

        NegativeEventPrecisionMetric algorithm = new NegativeEventPrecisionMetric();
        algorithm.setUnmappedPrecision(true);
        algorithm.setUnmappedRecall(true);
        algorithm.setUseWeighted(true);
        algorithm.setMultiThreaded(true);
        algorithm.load(mapping);
        algorithm.calculate();
        timer.stop();
        System.out.println("Tiempo total NegativeEventPrecisionMetric : " + timer.getReadableElapsedTime() + "\n ");

        System.out.println(algorithm.getResult());

        return algorithm;
    }

    public static AryaPrecision aryaPrecision(String logfile, String netfile) {
        es.usc.citius.aligments.utils.Timer timer = new es.usc.citius.aligments.utils.Timer();
        timer.start();
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();

        AryaPrecision algorithm = new AryaPrecision();
        algorithm.setChosenAlgorithm(new PetrinetReplayerWithoutILP());
        algorithm.setCreateInitialMarking(true);
        algorithm.setCreateFinalMarking(true);
//        algorithm.setTraceGrouped(true);
        algorithm.load(mapping);
        algorithm.calculate();
        timer.stop();
        System.out.println("Tiempo total aryaPrecision : " + timer.getReadableElapsedTime());
        System.out.println("Result : " + algorithm.getResult());

        return algorithm;
    }
}
