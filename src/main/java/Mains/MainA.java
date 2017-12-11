package Mains;

import Algoritmos.AlgoritmoA;
import Problem.Readers;
import domainLogic.exceptions.*;

import java.io.IOException;

public class MainA {
    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
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

        AlgoritmoA.problem(miReader);
    }
}
