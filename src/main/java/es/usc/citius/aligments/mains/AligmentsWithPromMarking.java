package es.usc.citius.aligments.mains;

import es.usc.citius.aligments.algoritmos.AlgorithmAStar;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;

public class AligmentsWithPromMarking {

    private double fitness;
    private double precision;
    private double generalization;

    public PNRepResultImpl calculate(String logfile, String netfile) {
        PNRepResultImpl results = AlgorithmAStar.problem(logfile, netfile);
        fitness = (double) results.getInfo().get("Trace Fitness");
        precision = AlgorithmAStar.getPrecision();
        generalization = AlgorithmAStar.getGeneralization();
        return results;
    }

    public double getPrecision() {
        return precision;
    }

    public double getFitness() {
        return fitness;
    }

    public double getGeneralization() {
        return generalization;
    }
}
