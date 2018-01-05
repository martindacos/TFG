package es.usc.citius.aligments.mains;

import domainLogic.workflow.algorithms.geneticMining.fitness.parser.CMParserImpl;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.ParserInterface;
import domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
import domainLogic.workflow.algorithms.geneticMining.population.initial.dependencies.GeneticDependenciesBuilder;
import es.usc.citius.aligments.algoritmos.AlgoritmoA;
import es.usc.citius.aligments.problem.Readers;
import domainLogic.exceptions.*;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;

import java.io.IOException;
import java.util.Random;

public class MainA {

    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        args = new String[2];
        args[0] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.xes";
        args[1] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.hn";
        //args[0] = "/home/martin/Descargas/Citius/LogsAligments/g2/grouped_g2pi300.xes";
        //args[1] = "/home/martin/Descargas/Citius/LogsAligments/g2/BadIndividual21.hn";
        //args[0] = "/home/martin/Descargas/PLG_Logs/28 Actividades/5000.xes";
        //args[1] = "/home/martin/Descargas/PLG_Logs/28 Actividades/BestIndividual.hn";

        Readers miReader;
        switch (args.length) {
            case 2:
                //Cargamos el Modelo y el Log
                miReader = Readers.getReader(args[0], args[1]);
                miReader.getInd().print();
                //miReader.setTracesETM();
                //miReader.setTracesG3();
                break;
            default:
                //Cargamos un
                miReader = Readers.getReader();
                miReader.getInd().print();
        }

        AlgoritmoA.problem(miReader, false);
        //fitnessProdigen(miReader);
    }

    public static void fitnessProdigen(Readers miReader) {
        ParserInterface parser;
        GeneticDependenciesBuilder dependencies = new GeneticDependenciesBuilder(miReader.getLog());
        parser = new CMParserImpl(dependencies.getDependencies(), new Random(), miReader.getLog().getNumOfCases(), miReader.getLog().getNumOfActivities());

        long time_start, time_end, total_time;
        //Empezamos a tomar la medida del tiempo
        time_start = System.currentTimeMillis();

        IndividualFitness fitness = parser.parse(miReader.getInd(), miReader.getLog().getCaseInstances());
        miReader.getInd().setFitness(fitness);

        time_end = System.currentTimeMillis();
        total_time = time_end - time_start;

        InterfazSalida salida = new SalidaTerminalImpl();
        String s = salida.estadisticasModelo(miReader.getInd(), 0d, total_time, 0d);
        System.out.println(s);
    }
}
