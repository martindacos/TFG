package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.precision.ETConformanceBestAlign;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.NBestPrefixAlignmentsGraphGuessMarkingAlg;

public class AlignmentBasedPrecision {

    public static ETConformanceBestAlign calculate(String logfile, String netfile) {
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
        System.out.println("Tiempo total CoBeFra : " + timer.getReadableElapsedTime() + "\n ");

        System.out.println(algorithm.getResult());

        return algorithm;
    }
}
