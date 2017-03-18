
import Problem.EjecTareas;
import Problem.NState.State;
import Problem.NState.StateMove;
import static Problem.NState.StateMove.*;
import Problem.Traza;
import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.Log;
import domainLogic.workflow.LogEntryInterface;
import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderHN;
import domainLogic.workflow.algorithms.geneticMining.individual.reader.IndividualReaderInterface;
import domainLogic.workflow.logReader.*;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.*;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.ActionFunction;
import es.usc.citius.hipster.model.function.ActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.*;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Main {
    
    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        // Read the log.
//        LogReaderInterface reader = new LogReaderXES();
//        ArrayList<LogEntryInterface> entries = reader.read(null, null, new File("C:/Users/marti/Documents/ETM.xes"));
//        Log log = new Log("test","C:/Users/marti/Documents/log.txt",entries);
//
//        // Obtain the individual from the file.
//        IndividualReaderInterface readerInd = new IndividualReaderHN();
//
//        try {
//            log.simplifyAndAddDummies(true, false);
//            modelo = readerInd.read("C:/Users/marti/Documents/ETM.hn", log);
//        } catch (NullPointerException ex) {
//            log.simplifyAndAddDummies(true, true);
//            modelo = readerInd.read("C:/Users/marti/Documents/ETM.hn", log);
//        }
//
//        HashMap<String,Task> tasks = log.getTasks();
//        
//        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
//            Task t = entry.getValue();
//            System.out.println("clave=" + entry.getKey() + ", valor=" + t.getMatrixID());
//        }
        
        //Creamos el modelo
        CMIndividual modelo = new CMIndividual(5);
        
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
        subset.add(B.getMatrixID());
        set.add(subset);
        //Creamos y añadimos el segundo subset
        subset = new TIntHashSet();
        subset.add(D.getMatrixID());
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
        subset4.add(B.getMatrixID());
        set4.add(subset4);
        //Creamos y añadimos el segundo subset
        subset4 = new TIntHashSet();
        subset4.add(D.getMatrixID());
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

        modelo.setTasks(tasks);

        final Traza test = new Traza();
        test.anadirTarea(A);
        test.anadirTarea(B);
        //test.anadirTarea(C);
        //test.anadirTarea(D);
        test.anadirTarea(E);

        final State initialState = new State(modelo);
        initialState.getMarcado().restartMarking();
        EjecTareas ejec = new EjecTareas();
        
        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, State> af = new ActionFunction<StateMove, State>() {
            @Override
            public Iterable<StateMove> actionsFor(State state) {
                return validMovementsFor(state, modelo, test, ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> atf;
        atf = new ActionStateTransitionFunction<StateMove, State>() {
            @Override
            public State apply(StateMove action, State state) {
                return applyActionToState(action, state, ejec, modelo);
            }
        };

        //Definición de la función de coste
        CostFunction<StateMove, State, Double> cf = new CostFunction<StateMove, State, Double>() {
            @Override
            public Double evaluate(Transition<StateMove, State> transition) {
                return evaluateToState(transition);
            }
        };

        //Definición de la función heurística
        HeuristicFunction<State, Double> hf = new HeuristicFunction<State, Double>() {
            @Override
            public Double estimate(State state) {
                //Sólo Poñemos a Heurística. Da g() xa se encarga Hipster.
                //Heurística. Número de elementos que faltan por procesar da traza
                return test.getHeuristica(state.getPos());
            }
        };

        SearchProblem<StateMove, State, WeightedNode<StateMove, State, Double>> p
                = ProblemBuilder.create()
                .initialState(initialState)
                .defineProblemWithExplicitActions()
                .useActionFunction(af)
                .useTransitionFunction(atf)
                .useCostFunction(cf)
                .useHeuristicFunction(hf)
                .build();

        WeightedNode n = null;
        
        double mejorScore = 0d;
        boolean parar = false;
        Iterator it = null, it2 = null;
        
        for (WeightedNode n1 : Hipster.createAStar(p)) {        
            n = n1;
            State s = (State) n1.state();
            //Final del modelo y final de la traza (para hacer skips y inserts al final)
            if (parar) {
                System.out.println("------------------SIGO------------------");
                double estimacion = (double) n.getScore();
                System.out.println("ESTIMACION "+estimacion+" MEJOR SCORE " + mejorScore);
                if (estimacion > mejorScore) break;
            }
            
            if (test.procesadoTraza(s.getPos()) && s.finalModelo()) {
                parar = true;
                if (mejorScore == 0) {
                    mejorScore = (double) n.getScore();
                    it = n.path().iterator();
                    it2 = n.path().iterator();
                } else {
                    double aux = (double) n.getScore();
                    if (aux < mejorScore) {
                        mejorScore = aux;
                        it = n.path().iterator();
                        it2 = n.path().iterator();
                    }
                }
            }
        }

        while (it.hasNext()) {
            System.out.println();
            System.out.println("------Información del nodo-------");
            WeightedNode node = (WeightedNode) it.next();
            System.out.println("    Acción realizada " + node.action());
            System.out.println("    Coste Actual g() " + node.getCost());
            System.out.println("    Heurística h() " + node.getEstimation());
            System.out.println("------Información del estado-------");
            State s = (State) node.state();
            System.out.println("    Posición de la traza " + s.getPos());
            if (s.getPos() < test.getTrace().size()) {
                System.out.println("    Tarea de la Traza " + test.leerTarea(s.getPos()).getMatrixID());
            }
            System.out.println("------------------------------------");
            System.out.println();
        }

        //La primera iteración corresponde con el Estado Inicial
        it2.next();
        System.out.println();
        System.out.println("------SALIDA VISUAL-------");
        System.out.println("    TRAZA     MODELO");
        while (it2.hasNext()) {
            WeightedNode node = (WeightedNode) it2.next();
            State s = (State) node.state();
                if (node.action().equals(OK)) {
                    System.out.println("    " + test.leerTarea(s.getPos() - 1) + "          " + s.getTarea());
                } else if (node.action().equals(SKIP)) {
                    System.out.println("    >>         "+ s.getTarea());
                } else {
                    System.out.println("    " + test.leerTarea(s.getPos() - 1) + "            >>");
                }
        }
        System.out.println();
        System.out.println("Coste del Alineamiento " + mejorScore);
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(State state, CMIndividual modelo, Traza trace, EjecTareas ejec) {
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        ejec.clear();
        
        Task e = trace.leerTarea(state.getPos());

        if (!trace.procesadoTraza(state.getPos())) {
            movements.add(INSERT);
            ejec.anadirINSERT(e);
            
            //Posibles tareas a ejecutar en el modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                int id = tasks.next();
                if (e.getMatrixID() == id) {
                    movements.add(OK);
                    ejec.anadirOk(e);
                    break;
                }
            }
        }
        if (!state.finalModelo()) {
            //Posibles tareas a ejecutar en el modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                int id = tasks.next();
                if (e == null) {
                    movements.add(SKIP);
                    ejec.anadirExecute(modelo.getTask(id).getTask());
                }else if (e.getMatrixID() != id) {
                    movements.add(SKIP);
                    ejec.anadirExecute(modelo.getTask(id).getTask());
                }
            }
        }
        System.out.println(movements);
        return movements;
    }

    //Realizamos la acción correspondiente en función del movimiento
    private static State applyActionToState(StateMove action, State state, EjecTareas ejec, CMIndividual m) {
        State successor = new State(state);
        CMMarking marcado = new CMMarking(m, new Random(666));
        
        marcado.setEndPlace(state.getMarcado().getEndPlace());
        marcado.setNumOfTokens(state.getMarcado().getNumberTokens());
        marcado.setStartPlace(state.getMarcado().getStartPlace());
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN = (ArrayList<HashMap<TIntHashSet, Integer>>) tokensA.clone();
        marcado.setTokens(tokensN);
        
        successor.setMarcado(marcado);
        
        switch (action) {
            case OK:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarMarcado(ejec.getTareaOK());
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(OK);
                successor.setTarea(ejec.getTareaOK().getId());
                break;
            case SKIP:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                Task t = ejec.leerTareaExecute();
                successor.setTarea(t.getId());
                System.out.println("TAREA A HACER EL SKIP ----------------> " + t.getId());
                successor.avanzarMarcado(t);
                successor.setMov(SKIP);
                break;
            case INSERT:
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(INSERT);
                System.out.println("TAREA A HACER EL INSERT ----------------> " + ejec.getTareaINSERT().getId());
                successor.setTarea(ejec.getTareaINSERT().getId());
                break;
        }
        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, State> transition) {
        StateMove action = transition.getAction();
        Double cost = null;
        switch (action) {
            case SKIP:
                cost = 4d;
                break;
            case INSERT:
                cost = 2d;
                break;
            case OK:
                cost = 1d;
                break;
        }
        return cost;
    }
}
