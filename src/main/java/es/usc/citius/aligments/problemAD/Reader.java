package es.usc.citius.aligments.problemAD;

import es.usc.citius.aligments.problem.*;
import es.usc.citius.prodigen.domainLogic.exceptions.EmptyLogException;
import es.usc.citius.prodigen.domainLogic.exceptions.InvalidFileExtensionException;
import es.usc.citius.prodigen.domainLogic.exceptions.MalformedFileException;
import es.usc.citius.prodigen.domainLogic.exceptions.NonFinishedWorkflowException;
import es.usc.citius.prodigen.domainLogic.exceptions.WrongLogEntryException;
import es.usc.citius.prodigen.domainLogic.workflow.CaseInstance;
import es.usc.citius.prodigen.domainLogic.workflow.Log;
import es.usc.citius.prodigen.domainLogic.workflow.LogEntryInterface;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import es.usc.citius.prodigen.domainLogic.workflow.logReader.LogReaderInterface;
import es.usc.citius.prodigen.domainLogic.workflow.logReader.LogReaderXES;
import gnu.trove.list.array.TIntArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author marti
 */
public class Reader {

    private Log log;
    private CMIndividual ind;
    //Posición del array de trazas que se está procesando
    private int pos = 0;
    private ArrayList<InterfazTraza> traces;

    public Reader(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        traces = new ArrayList<>();
        // Read the log.
        LogReaderInterface reader = new LogReaderXES();
        ArrayList<LogEntryInterface> entries = reader.read(null, null, new File(logPath));
        log = new Log("test", "log.txt", entries);

        // Obtain the individual from the file.
        IndividualReaderInterface readerInd = new IndividualReaderHN();

        try {
            log.simplifyAndAddDummies(true, false);
            ind = readerInd.read(indPath, log);
            //ind = ModelFormatConversor.HNtoCN(ind);
        } catch (NullPointerException ex) {
            log.simplifyAndAddDummies(true, true);
            ind = readerInd.read(indPath, log);
            //ind = ModelFormatConversor.HNtoCN(ind);
        }

        System.out.println("Log '" + log.getName() + "':");
        ConcurrentHashMap<String, CaseInstance> traces = log.getCaseInstances();
        int j = 1;
        for (String traceKey : traces.keySet()) {
            CaseInstance trace = traces.get(traceKey);
            Integer numRepetitions = trace.getNumInstances();
            System.out.print("\t" + j + " Trace '" + trace.getId() + "' (" + numRepetitions + " repetitions): [ ");
            TIntArrayList tasks = trace.getTaskSequence();
            Traza traza = new Traza();
            traza.setId(trace.getId());
            for (int i = 0; i < tasks.size(); i++) {
                System.out.print(tasks.get(i) + " ");
                traza.anadirTarea(tasks.get(i));
            }
            traza.setNumRepeticiones(numRepetitions);
            this.traces.add(traza);
            System.out.println("]");
            j++;
        }
    }

    public CMIndividual getInd() {
        return ind;
    }

    public ArrayList<InterfazTraza> getTraces() {
        return traces;
    }

    public void setTraces(ArrayList<InterfazTraza> traces) {
        this.traces = traces;
    }

    public InterfazTraza getTrazaActual() {
        return traces.get(pos);
    }

    public InterfazTraza getTrazaPos(int p) {
        return traces.get(p);
    }

    public void avanzarPos() {
        pos++;
    }
}
