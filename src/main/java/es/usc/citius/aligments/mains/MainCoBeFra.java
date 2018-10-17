package es.usc.citius.aligments.mains;

import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import nl.tue.astar.AStarException;
import org.processmining.cobefra.AligmentBasedFitness;

public class MainCoBeFra {
    public static void main(String[] args) throws AStarException {
        AryaFitness cobefra = AligmentBasedFitness.calculate(args[0], args[1]);
    }
}
