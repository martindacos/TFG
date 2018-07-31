package es.usc.citius.aligments.mains;

import es.usc.citius.aligments.algoritmos.AlgoritmoAReduced;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;
import es.usc.citius.prodigen.domainLogic.exceptions.*;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.fitness.parser.CMParserImpl;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.fitness.parser.ParserInterface;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.population.initial.dependencies.GeneticDependenciesBuilder;

import java.io.IOException;
import java.util.Random;

public class MainA {

    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        //args = new String[2];
        //args[0] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.xes";
        //args[1] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.hn";
        //args[0] = "/home/martin/Descargas/Citius/LogsAligments/g2/grouped_g2pi300.xes";
        //args[1] = "/home/martin/Descargas/Citius/LogsAligments/g2/BadIndividual21.hn";

        ///home/martin/Descargas/PLG_Logs/10_Actividades/1000.xes /home/martin/Descargas/PLG_Logs/10_Actividades/BadIndividual.hn
        ///home/martin/Descargas/PLG_Logs/49_Actividades/1000.xes /home/martin/Descargas/PLG_Logs/49_Actividades/Individual.hn
        ///home/martin/Descargas/PLG_Logs/123_Actividades/5000.xes /home/martin/Descargas/PLG_Logs/123_Actividades/Individual.hn
        ///home/martin/Descargas/PLG_Logs/49_Actividades/1000BigNoise.xes /home/martin/Descargas/PLG_Logs/49_Actividades/Individual.hn
        ///home/martin/Descargas/PLG_Logs/SPL03-raw.xes /home/martin/Descargas/PLG_Logs/upper.hn

        ///home/martin/Documentos/projects/prodigen-backend/log-dir/RP/RP0.csv /home/martin/Documentos/projects/prodigen-backend/log-dir/RP/HM0.hn
        Readers miReader;
        switch (args.length) {
            case 2:
                //Cargamos el Modelo y el Log
                miReader = Readers.getReader(args[0], args[1]);
                miReader.getInd().print();
                //AlgoritmoA.problem(miReader, false);
                AlgoritmoAReduced.problem(miReader, true);
                //AlgoritmoAReducedLarge.problem(miReader, true);
                break;
            //Añadir un argumentos más para que entre en el default
            default:
                //Cargamos el Modelo y el Log
                miReader = Readers.getReader(args[0], args[1]);
                miReader.getInd().print();
                //Ejecutamos Token Replay para calcular el fitness
                fitnessProdigen(miReader);
                break;
        }
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
