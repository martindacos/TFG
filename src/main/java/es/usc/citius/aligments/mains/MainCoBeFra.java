package es.usc.citius.aligments.mains;

import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import org.processmining.cobefra.AligmentBasedFitness;

public class MainCoBeFra {
    public static void main(String[] args) {
        AryaFitness cobefra = AligmentBasedFitness.calculate(args[0], args[1]);
    }
}
