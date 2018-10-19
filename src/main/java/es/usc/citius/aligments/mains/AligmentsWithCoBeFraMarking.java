package es.usc.citius.aligments.mains;

import es.usc.citius.aligments.algoritmos.AlgorithmAStar;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;

public class AligmentsWithCoBeFraMarking {

    public static PNRepResultImpl calculate(String logfile, String netfile) {
        PNRepResultImpl results = AlgorithmAStar.problem(logfile, netfile);
        return results;
    }

}
