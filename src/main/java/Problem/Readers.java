/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Problem;

import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.Log;
import domainLogic.workflow.LogEntryInterface;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import domainLogic.workflow.logReader.LogReaderInterface;
import domainLogic.workflow.logReader.LogReaderXES;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author marti
 */
public class Readers {

    private Log log;
    private CMIndividual ind;
    
    public Readers(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        // Read the log.
        LogReaderInterface reader = new LogReaderXES();
        ArrayList<LogEntryInterface> entries = reader.read(null, null, new File(logPath));
        log = new Log("test","log.txt",entries);

        // Obtain the individual from the file.
        IndividualReaderInterface readerInd = new IndividualReaderHN();

        try {
            log.simplifyAndAddDummies(true, false);
            ind = readerInd.read(indPath, log);
        } catch (NullPointerException ex) {
            log.simplifyAndAddDummies(true, true);
            ind = readerInd.read(indPath, log);
        }
    }

    public Log getLog() {
        return log;
    }

    public CMIndividual getInd() {
        return ind;
    }
}
