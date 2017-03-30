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
import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import domainLogic.workflow.logReader.LogReaderInterface;
import domainLogic.workflow.logReader.LogReaderXES;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author marti
 */
public class Readers {

    private static Readers miReader;
    private Log log;
    private CMIndividual ind;
    //Posición del array de trazas que se está procesando
    private int pos = 0;
    private ArrayList<InterfazTraza> traces;

    public static Readers getReader() {
        if (miReader == null) {
            miReader = new Readers();
        }
        return miReader;
    }
    
    public static Readers getReader(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        if (miReader == null) {
            miReader = new Readers(logPath, indPath);
        }
        return miReader;
    }
    
    protected Readers(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
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
    
    protected Readers() {
        //Modelo fijo de prueba
        ind = new CMIndividual(5);

        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task D = new Task("D");
        CMTask cmD = new CMTask(D);

        Task B = new Task("B");
        CMTask cmB = new CMTask(B);
        Task C = new Task("C");
        CMTask cmC = new CMTask(C);

        Task E = new Task("E");
        E.setType(1);
        CMTask cmE = new CMTask(E);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(D.getMatrixID());
        //subset.add(B.getMatrixID());
        set.add(subset);
        //Creamos y añadimos el segundo subset
        subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        subset.add(C.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de outputs
        CMSet set2 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset2 = new TIntHashSet();
        subset2.add(E.getMatrixID());
        set2.add(subset2);

        //Asignamos outputs
        cmD.setOutputs(set2);
        cmB.setOutputs(set2);
        cmC.setOutputs(set2);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        set3.add(subset3);

        cmD.setInputs(set3);
        cmB.setInputs(set3);
        cmC.setInputs(set3);

        //Creamos el set de inputs
        CMSet set4 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset4 = new TIntHashSet();
        subset4.add(D.getMatrixID());
        //subset4.add(B.getMatrixID());
        set4.add(subset4);
        //Creamos y añadimos el segundo subset
        subset4 = new TIntHashSet();
        subset4.add(B.getMatrixID());
        subset4.add(C.getMatrixID());
        set4.add(subset4);

        //Asignamos inputs
        cmE.setInputs(set4);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmD);
        tasks.put(2, cmB);
        tasks.put(3, cmC);
        tasks.put(4, cmE);

        ind.setTasks(tasks);
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
