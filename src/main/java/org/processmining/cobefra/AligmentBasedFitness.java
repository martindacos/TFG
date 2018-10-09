package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.astar.petrinet.*;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.swapping.PetrinetSwapReplayer;

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
}
