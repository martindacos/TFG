package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import es.usc.citius.aligments.algoritmos.AlgorithmAStar;
import nl.tue.astar.*;
import org.processmining.plugins.astar.petrinet.*;

public class AligmentBasedFitness {

    public static AryaFitness calculate2(String logfile, String netfile) {
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

    public static AryaFitness calculate(String logfile, String netfile) throws AStarException {
        AlgorithmAStar.problem(logfile, netfile);
        return null;
    }
}
