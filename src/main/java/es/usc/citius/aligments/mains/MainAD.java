package es.usc.citius.aligments.mains;

import es.usc.citius.aligments.algoritmos.AlgoritmoAD;
import es.usc.citius.aligments.problem.Readers;
import domainLogic.exceptions.*;

import java.io.IOException;

public class MainAD {
    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        args = new String[2];
        args[0] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.xes";
        args[1] = "/home/martin/Descargas/Citius/LogsAligments/ETM2/ETM.hn";

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

        AlgoritmoAD.problem(miReader, true);
    }
}
