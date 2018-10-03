package es.usc.citius.aligments.problem;

import es.usc.citius.prodigen.config.NameConstants;
import es.usc.citius.prodigen.domainLogic.exceptions.*;
import es.usc.citius.prodigen.domainLogic.workflow.CaseInstance;
import es.usc.citius.prodigen.domainLogic.workflow.Log;
import es.usc.citius.prodigen.domainLogic.workflow.LogEntryImpl;
import es.usc.citius.prodigen.domainLogic.workflow.LogEntryInterface;
import es.usc.citius.prodigen.domainLogic.workflow.Task.Task;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import es.usc.citius.prodigen.domainLogic.workflow.logReader.LogReaderInterface;
import es.usc.citius.prodigen.domainLogic.workflow.logReader.LogReaderXES;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marti
 */
public class Readers {

    private static Readers miReader;
    private static Log log;
    private static CMIndividual ind;
    //Posición del array de trazas que se está procesando
    private static int pos = 0;
    private static ArrayList<InterfazTraza> traces;
    private static Paths paths;
    private static List<String> orderTraces = new ArrayList<>();

    public static void reset() {
        miReader = null;
        log = null;
        ind = null;
        pos = 0;
        traces = null;
        paths = null;
        orderTraces = new ArrayList<>();
    }

    public Readers(Log miLog) {
        this.createLog(miLog);
    }

    //Como el log siempre va a ser el mismo para todos, lo creamos una sola vez
    public final void createLog(Log miLog) {
        traces = new ArrayList<>();
        log = miLog;

        ConcurrentHashMap<String, CaseInstance> traces = log.getCaseInstances();
        for (String traceKey : traces.keySet()) {
            CaseInstance trace = traces.get(traceKey);
            Integer numRepetitions = trace.getNumInstances();
            TIntArrayList tasks = trace.getTaskSequence();
            Traza traza = new Traza();
            for (int i = 0; i < tasks.size(); i++) {
                traza.anadirTarea(tasks.get(i));
            }
            traza.setNumRepeticiones(numRepetitions);
            this.traces.add(traza);
        }
    }

    public static Readers getReader() {
        if (miReader == null) {
            miReader = new Readers();
        }
        return miReader;
    }

    public static Readers getReader(ArrayList<InterfazTraza> traces, CMIndividual individual) {
        if (miReader == null) {
            miReader = new Readers(traces, individual);
        }
        return miReader;
    }

    public static Readers getReader(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        if (miReader == null) {
            miReader = new Readers(logPath, indPath);
        }
        return miReader;
    }

    public static List<String> getOrderTraces() {
        return orderTraces;
    }

    private Readers(String logPath, String indPath) throws EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        traces = new ArrayList<>();
        // Read the log.
        //LogReaderInterface reader = new LogReaderCSV();
        LogReaderInterface reader = new LogReaderXES();
        ArrayList<LogEntryInterface> entries = reader.read(null, null, new File(logPath));
        //TODO Can remove, only for test CoBeFRa
        readXesWithOrden(logPath);
        log = new Log("test","log.txt",entries);

        // Obtain the individual from the file.
        IndividualReaderInterface readerInd = new IndividualReaderHN();

        try {
            log.simplifyAndAddDummies(true, false);
            ind = readerInd.read(indPath, log);
            //ind = ModelFormatConversor.HNtoCN(ind);
        } catch (Exception ex) {
            log.simplifyAndAddDummies(true, true);
            ind = readerInd.read(indPath, log);
            //ind = ModelFormatConversor.HNtoCN(ind);
        }
        
        //System.out.println("Log '" + log.getName() + "':");
        ConcurrentHashMap<String, CaseInstance> traces = log.getCaseInstances();
        int j = 1;
        for (String traceKey : traces.keySet()) {
            CaseInstance trace = traces.get(traceKey);
            Integer numRepetitions = trace.getNumInstances();
            //System.out.print("\t" + j + " Trace '" + trace.getId() + "' (" + numRepetitions + " repetitions): [ ");
            TIntArrayList tasks = trace.getTaskSequence();
            Traza traza = new Traza();
            traza.setId(trace.getId());
            for (int i=0; i < tasks.size(); i++) {
                //System.out.print(tasks.get(i) + " ");
                traza.anadirTarea(tasks.get(i));
            }
            traza.setNumRepeticiones(numRepetitions);
            this.traces.add(traza);
            //System.out.println("]");
            j++;
        }
        //Remove task without inputs and outputs
        /*TIntObjectHashMap<CMTask> newTasks = new TIntObjectHashMap<>();
        int contador = 0;
        for (int i=0; i<ind.getNumOfTasks(); i++) {
            CMTask task = ind.getTask(i);
            if (task.getInputs().size() > 0 || task.getOutputs().size() > 0) {
                newTasks.put(contador, task);
                contador++;
            }
        }
        ind = new CMIndividual(newTasks);*/
        this.paths = getAllPaths();
    }

    private Readers(ArrayList<InterfazTraza> traces, CMIndividual ind) {
        this.traces = traces;
        this.ind = ind;
        this.paths = getAllPaths();
    }

    private Readers(Log log, CMIndividual ind) {
        this.log = log;
        this.ind = ind;

        traces = new ArrayList<>();
        ConcurrentHashMap<String, CaseInstance> traces = log.getCaseInstances();
        for (String traceKey : traces.keySet()) {
            CaseInstance trace = traces.get(traceKey);
            Integer numRepetitions = trace.getNumInstances();
            TIntArrayList tasks = trace.getTaskSequence();
            Traza traza = new Traza();
            traza.setId(trace.getId());
            for (int i=0; i < tasks.size(); i++) {
                traza.anadirTarea(tasks.get(i));
            }
            traza.setNumRepeticiones(numRepetitions);
            this.traces.add(traza);
        }
    }

    private Readers() {
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
        this.paths = getAllPaths();
    }

    public CMIndividual getInd() {
        return ind;
    }

    public void setInd(CMIndividual ind) {
        this.pos = 0;
        this.ind = ind;
    }

    public ArrayList<InterfazTraza> getTraces() {
        return traces;
    }

    public InterfazTraza getTrace(String id) throws Exception {
        for (InterfazTraza trace : traces) {
            if (trace.getId().equals(id)) return trace;
        }
        throw new Exception();
    }

    public InterfazTraza getTrace(Integer pos) throws Exception {
        String traceID = orderTraces.get(pos);
        if (traces.stream().filter(t -> t.getId().equals(traceID)).count() > 0) {
            return traces.stream().filter(t -> t.getId().equals(traceID)).findAny().get();
        }
        return null;
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
    
    public void setTracesETM() {
        traces.clear();
        //Alineamiento de coste 0
        Traza test = new Traza();
        test.anadirTarea(8);
        test.anadirTarea(0);
        test.anadirTarea(3);
        test.anadirTarea(6);
        test.anadirTarea(4);
        test.anadirTarea(5);
        test.anadirTarea(7);
        test.setNumRepeticiones(1);
        test.setId("1");

        Traza test2 = new Traza();
        test2.anadirTarea(7);
        test2.setNumRepeticiones(1);
        test2.setId("2");
        
        //Alineamiento 1
        Traza test3 = new Traza();
        test3.anadirTarea(8);
        test3.anadirTarea(0);
        test3.anadirTarea(1);
        test3.anadirTarea(2);
        test3.anadirTarea(3);
        test3.setNumRepeticiones(1);
        test3.setId("3");
        
        //Alineamiento 2
        Traza test4 = new Traza();
        test4.anadirTarea(8);
        test4.anadirTarea(0);
        test4.anadirTarea(2);
        test4.anadirTarea(1);
        test4.setNumRepeticiones(1);
        test4.setId("4");
        
        //Alineamiento 3
        Traza test5 = new Traza();
        test5.anadirTarea(8);
        test5.anadirTarea(0);
        test5.anadirTarea(3);
        test5.anadirTarea(1);
        test5.anadirTarea(4);
        test5.anadirTarea(5);
        test5.anadirTarea(7);
        test5.setNumRepeticiones(1);
        test5.setId("5");
        
        traces.add(test);
        traces.add(test2);
        traces.add(test3);
        traces.add(test4);
        traces.add(test5);
    }
    
    public void setTracesG3() {
        traces.clear();
        //Alineamiento de coste 0
        Traza test = new Traza();
        test.anadirTarea(0);
        test.anadirTarea(1);
        test.anadirTarea(2);
        test.anadirTarea(3);
        test.anadirTarea(4);
        test.anadirTarea(5);
        test.anadirTarea(6);
        test.anadirTarea(7);
        test.anadirTarea(8);
        test.anadirTarea(9);
        test.anadirTarea(10);
        test.anadirTarea(11);
        test.anadirTarea(28);
        test.anadirTarea(17);
        test.anadirTarea(18);
        test.anadirTarea(19);
        test.setNumRepeticiones(1);

        Traza test2 = new Traza();
        test2.anadirTarea(19);
        test2.setNumRepeticiones(1);
        
        //Faltan tres tareas
        Traza test3 = new Traza();
        test3.anadirTarea(1);
        test3.anadirTarea(2);
        test3.anadirTarea(3);
        test3.anadirTarea(4);
        test3.anadirTarea(5);
        test3.anadirTarea(6);
        test3.anadirTarea(8);
        test3.anadirTarea(9);
        test3.anadirTarea(10);
        test3.anadirTarea(11);
        test3.anadirTarea(28);
        test3.anadirTarea(17);
        test3.anadirTarea(18);
        test3.setNumRepeticiones(1);

        //Alineamiento 2
        Traza test4 = new Traza();
        test4.anadirTarea(0);
        test4.anadirTarea(1);
        test4.anadirTarea(2);
        test4.anadirTarea(3);
        test4.anadirTarea(4);
        test4.anadirTarea(5);
        test4.anadirTarea(6);
        test4.anadirTarea(9);
        test4.anadirTarea(8);
        test4.anadirTarea(9);
        test4.anadirTarea(10);
        test4.anadirTarea(11);
        test4.anadirTarea(28);
        test4.anadirTarea(17);
        test4.anadirTarea(18);
        test4.anadirTarea(19);
        test4.anadirTarea(0);
        test4.setNumRepeticiones(1);
        
        //Alineamiento 3
        Traza test5 = new Traza();
        test5.anadirTarea(0);
        test5.anadirTarea(1);
        test5.anadirTarea(2);
        test5.anadirTarea(3);
        test5.anadirTarea(4);
        test5.anadirTarea(5);
        test5.anadirTarea(6);
        test5.anadirTarea(7);
        test5.anadirTarea(7);
        test5.anadirTarea(8);
        test5.anadirTarea(9);
        test5.anadirTarea(10);
        test5.anadirTarea(11);
        test5.anadirTarea(28);
        test5.anadirTarea(17);
        test5.anadirTarea(17);
        test5.anadirTarea(18);
        test5.anadirTarea(19);
        test5.setNumRepeticiones(1);
        
        traces.add(test);
        traces.add(test2);
        traces.add(test3);
        traces.add(test4);
        traces.add(test5);
    }

    public Log getLog() {
        return log;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public static Paths getPaths() {
        return paths;
    }

    public Paths getAllPaths() {
        Paths p = new Paths();
        /*int actual_task = 0;
        if (!ind.getStartTasks().isEmpty()) {
            actual_task = ind.getStartTasks().iterator().next();
        }
        TIntHashSet exploredTasks = new TIntHashSet();
        TIntHashSet notExploredTasks = new TIntHashSet();
        CMTask task = ind.getTask(actual_task);
        //Añadimos la tarea como obligatoria
        p.addRequiredTask(actual_task);
        exploredTasks.add(actual_task);
        while (task != null) {
            //Añadimos la tarea final
            if (task.getOutputs().size() == 0) {
                p.addRequiredTask(actual_task);
            }
            //Exploramos las salidas de la tarea
            Iterator<TIntHashSet> it = task.getOutputs().iterator();
            while (it.hasNext()) {
                TIntHashSet next = it.next();
                if (next.contains(actual_task)) {
                    //Añadimos la tarea como bucle
                    p.addLoopsTask(actual_task);
                    p.removeRequiredTask(actual_task);
                    next.remove(actual_task);
                }
                //Añadimos como tareas opcionales
                if (task.getOutputs().size() > 1) {
                    p.addRequiredOptionalTask(next);
                }
                TIntIterator iterator = next.iterator();
                while (iterator.hasNext()) {
                    int next1 = iterator.next();
                    //Añadimos como tareas obligatorias
                    if (task.getOutputs().size() == 1) {
                        p.addRequiredTask(next1);
                    }
                    if (!exploredTasks.contains(next1)) {
                        notExploredTasks.add(next1);
                    }
                }
            }
            //Seleccionamos la nueva tarea a explorar
            if (notExploredTasks.iterator().hasNext()) {
                actual_task = notExploredTasks.iterator().next();
                task = ind.getTask(actual_task);
                notExploredTasks.remove(actual_task);
            } else {
                task = null;
            }
        }*/

        return p;
    }

    private void readXesWithOrden(String xesPath) {
        File file = new File(xesPath);
        XesXmlParser reader = new XesXmlParser();

        try {
            List<XLog> parser = reader.parse(file);
            XLog traces = parser.get(0);
            int numCases = traces.size();
            for (int caseIndex = 0; caseIndex < numCases; caseIndex++) {
                XTrace trace = traces.get(caseIndex);
                XAttribute caseName = trace.getAttributes().get("concept:name");
                orderTraces.add(caseName.toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(LogReaderXES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
