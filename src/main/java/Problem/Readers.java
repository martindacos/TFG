package Problem;

import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.utils.ModelFormatConversor;
import domainLogic.workflow.CaseInstance;
import domainLogic.workflow.Log;
import domainLogic.workflow.LogEntryInterface;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import domainLogic.workflow.logReader.LogReaderInterface;
import domainLogic.workflow.logReader.LogReaderXES;
import gnu.trove.list.array.TIntArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author marti
 */
public class Readers {

    private Log log;
    private CMIndividual ind;
    
    private int pos = 0;
    private ArrayList<Traza> traces;
    
    public Readers(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        traces = new ArrayList<>();
        // Read the log.
        LogReaderInterface reader = new LogReaderXES();
        ArrayList<LogEntryInterface> entries = reader.read(null, null, new File(logPath));
        log = new Log("test","log.txt",entries);

        // Obtain the individual from the file.
        IndividualReaderInterface readerInd = new IndividualReaderHN();

        try {
            log.simplifyAndAddDummies(false, false);
            ind = readerInd.read(indPath, log);
            ind = ModelFormatConversor.HNtoCN(ind);
        } catch (NullPointerException ex) {
            log.simplifyAndAddDummies(false, true);
            ind = readerInd.read(indPath, log);
            ind = ModelFormatConversor.HNtoCN(ind);
        }
        
        //System.out.println("Log '" + log.getName() + "':");
        ConcurrentHashMap<String, CaseInstance> traces = log.getCaseInstances();
        for (String traceKey : traces.keySet()) {
            CaseInstance trace = traces.get(traceKey);
            //Integer numRepetitions = trace.getNumInstances();
            //System.out.print("\tTrace '" + trace.getId() + "' (" + numRepetitions + " repetitions): [ ");
            TIntArrayList tasks = trace.getTaskSequence();
            Traza traza = new Traza();
            for (int i=0; i < tasks.size(); i++) {
                //System.out.print(tasks.get(i) + " ");
                traza.anadirTarea(tasks.get(i));
            }
            this.traces.add(traza);
            //System.out.println("]");
        }
    }
    
    public Readers() {
    }

    public Log getLog() {
        return log;
    }

    public CMIndividual getInd() {
        return ind;
    }

    public ArrayList<Traza> getTraces() {
        return traces;
    }

    public void setTraces(ArrayList<Traza> traces) {
        this.traces = traces;
    }
    
    public Traza getTrazaActual() {
        return traces.get(pos);
    }
    
    public Traza getTrazaPos(int p) {
        return traces.get(p);
    }
        
    public void avanzarPos() {
        pos++;
    }
    
    public void setTraces1() {
        traces = new ArrayList<>();
        Traza test = new Traza();
        test.anadirTarea(0);
        test.anadirTarea(1);
        test.anadirTarea(2);
        test.anadirTarea(3);
        test.anadirTarea(4);

        Traza test2 = new Traza();
        test2.anadirTarea(0);
        test2.anadirTarea(1);
        test2.anadirTarea(2);
        test2.anadirTarea(4);

        Traza test3 = new Traza();
        test3.anadirTarea(0);

        Traza test4 = new Traza();
        test4.anadirTarea(4);

        Traza test5 = new Traza();
        test5.anadirTarea(0);
        test5.anadirTarea(1);
        test5.anadirTarea(3);
        test5.anadirTarea(0);
        test5.anadirTarea(4);

        traces.add(test);
        traces.add(test2);
        traces.add(test3);
        traces.add(test4);
        traces.add(test5);
    }
}
