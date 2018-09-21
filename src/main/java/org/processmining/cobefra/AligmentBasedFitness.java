package org.processmining.cobefra;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import org.processmining.plugins.astar.petrinet.PrefixBasedPetrinetReplayer;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class AligmentBasedFitness {

    public AligmentBasedFitness(String logfile, String netfile) {
        Mapping mapping = new Mapping(logfile, netfile);
        mapping.assignUnmappedToInvisible();

        AryaFitness algorithm = new AryaFitness();
        //algorithm.setChosenAlgorithm(new PetrinetReplayerWithoutILP());
        algorithm.setChosenAlgorithm(new PrefixBasedPetrinetReplayer());
        algorithm.setCreateInitialMarking(true);
        algorithm.setCreateFinalMarking(true);
        algorithm.load(mapping);
        algorithm.calculate();

        System.out.println(algorithm.getResult());
        //See Aligments result
        PNRepResult pnRepResult = algorithm.getPNRepResult();
    }

}
