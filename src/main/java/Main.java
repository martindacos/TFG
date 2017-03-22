
import Modelo.Modelo;
import Problem.EjecTareas;
import Problem.NState.State;
import Problem.NState.StateMove;
import static Problem.NState.StateMove.*;
import Problem.Readers;
import Problem.Traza;
import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.ActionFunction;
import es.usc.citius.hipster.model.function.ActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.*;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        //Creamos el modelo
        Modelo miModelo = Modelo.getModelo();

        //Creamos el modelo desde archivo
        Readers r = new Readers("ETM.xes", "ETM.hn");
        //Modelo miModelo = Modelo.getModelo(r.getInd());
        miModelo.getInd().print();

//        ArrayList<Traza> prueba = new ArrayList<>();
//        Traza test = new Traza();
//        test.anadirTarea(0);
//        test.anadirTarea(1);
//        test.anadirTarea(2);
//        test.anadirTarea(3);
//        test.anadirTarea(4);
//        
//        Traza test2 = new Traza();
//        test2.anadirTarea(0);
//        test2.anadirTarea(1);
//        test2.anadirTarea(2);
//        test2.anadirTarea(4);
//        
//        prueba.add(test);
//        prueba.add(test2);
//        r.setTraces(prueba);

        final State initialState = new State(miModelo.getInd());
        initialState.getMarcado().restartMarking();

        EjecTareas ejec = new EjecTareas();

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, State> af = new ActionFunction<StateMove, State>() {
            @Override
            public Iterable<StateMove> actionsFor(State state) {
                return validMovementsFor(state, miModelo.getInd(), r.getTrazaActual(), ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> atf;
        atf = new ActionStateTransitionFunction<StateMove, State>() {
            @Override
            public State apply(StateMove action, State state) {
                return applyActionToState(action, state, ejec, miModelo.getInd());
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
                return r.getTrazaActual().getHeuristica(state.getPos());
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

        ArrayList<WeightedNode> nodosSalida = new ArrayList<>();
        //Tiempo total del cálculo del algoritmo
        long total_time = 0;

        for (int i = 0; i < r.getTraces().size(); i++) {
            WeightedNode n = null;
            double mejorScore = 0d;
            boolean parar = false;
            initialState.getMarcado().restartMarking();
            ejec.clear();
            
            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            for (WeightedNode n1 : Hipster.createAStar(p)) {
                State s = (State) n1.state();
                //Final del modelo y final de la traza (para hacer skips y inserts al final)
                if (parar) {
                    //System.out.println("------------------SIGO------------------");
                    double estimacion = (double) n.getScore();
                    //System.out.println("ESTIMACION " + estimacion + " MEJOR SCORE " + mejorScore);
                    if (estimacion > mejorScore) {
                        break;
                    }
                }

                if (r.getTrazaActual().procesadoTraza(s.getPos()) && s.finalModelo()) {
                    parar = true;
                    if (mejorScore == 0) {
                        mejorScore = (double) n1.getScore();
                        n = n1;
                    } else {
                        double aux = (double) n1.getScore();
                        if (aux < mejorScore) {
                            mejorScore = aux;
                            n = n1;
                        }
                    }
                }
            }
            time_end = System.currentTimeMillis();
            System.out.println("Tiempo de cálculo del alineamiento "+i+" = "+ (time_end - time_start) +" ms");
            total_time = total_time + (time_end - time_start);
            //Guardamos el nodo con los estados soluciones de la traza
            nodosSalida.add(n);
            //Guardamos el coste obtenido en el alineamiento
            r.getTrazaActual().setScore(mejorScore);
            //Pasamos a la siguientes traza del procesado
            r.avanzarPos();
        }

        double costeTotal = 0d;
        for (int i = 0; i < nodosSalida.size(); i++) {
//            Iterator it = nodosSalida.get(i).path().iterator();
//            System.out.println("-------------------TRAZA "+i+" ----------------------------");
//            while (it.hasNext()) {
//                System.out.println();
//                System.out.println("------Información del nodo-------");
//                WeightedNode node = (WeightedNode) it.next();
//                System.out.println("    Acción realizada " + node.action());
//                System.out.println("    Coste Actual g() " + node.getCost());
//                System.out.println("    Heurística h() " + node.getEstimation());
//                System.out.println("------Información del estado-------");
//                State s = (State) node.state();
//                System.out.println("    Posición de la traza " + s.getPos());
//                if (s.getPos() < r.getTrazaPos(i).getTrace().size()) {
//                    System.out.println("    Tarea de la Traza " + r.getTrazaPos(i).leerTarea(s.getPos()));
//                }
//                System.out.println(s.getMarcado().toString());
//                System.out.println("------------------------------------");
//                System.out.println();
//            }

            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial
            it2.next();
            System.out.println();
            System.out.println("------SALIDA VISUAL-------");
            System.out.println("    TRAZA     MODELO");
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                State s = (State) node.state();
                if (node.action().equals(OK)) {
                    System.out.println("    " + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "          " + s.getTarea());
                } else if (node.action().equals(SKIP)) {
                    System.out.println("    >>         " + s.getTarea());
                } else {
                    System.out.println("    " + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "          >>");
                }
            }
            System.out.println();
            System.out.println("Coste del Alineamiento " + r.getTrazaPos(i).getScore());
            costeTotal = costeTotal + r.getTrazaPos(i).getScore();
        }
        System.out.println();
        System.out.println("****************************************************************");
        System.out.println("Tiempo total de cálculo = " + total_time + " ms");
        System.out.println("Coste total = " + costeTotal);
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(State state, CMIndividual modelo, Traza trace, EjecTareas ejec) {
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        ejec.clear();

        Integer e = trace.leerTarea(state.getPos());

        if (!trace.procesadoTraza(state.getPos())) {
            movements.add(INSERT);
            ejec.anadirINSERT(e);

            //Posibles tareas a ejecutar en el modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                int id = tasks.next();
                if (e == id) {
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
                    ejec.anadirExecute(modelo.getTask(id).getTask().getMatrixID());
                } else if (e != id) {
                    movements.add(SKIP);
                    ejec.anadirExecute(modelo.getTask(id).getTask().getMatrixID());
                }
            }
        }

        //Realizamos la copia del marcado
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().cloneTokens();
        ejec.setTokens(tokensA);
        ejec.setEndPlace(state.getMarcado().getEndPlace());
        ejec.setNumOfTokens(state.getMarcado().getNumberTokens());
        ejec.setStartPlace(state.getMarcado().getStartPlace());
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        ejec.setPossibleEnabledTasks(possibleEnabledTasksClone);

        //System.out.println(movements);
        return movements;
    }

    //Realizamos la acción correspondiente en función del movimiento
    private static State applyActionToState(StateMove action, State state, EjecTareas ejec, CMIndividual m) {
        State successor = new State(state);

        //Recuperamos los datos copiados para el marcado
        CMMarking marking = new CMMarking(m, new Random(666));
        marking.restartMarking();
        marking.setEndPlace(ejec.getEndPlace());
        marking.setNumOfTokens(ejec.getNumOfTokens());
        marking.setStartPlace(ejec.getStartPlace());
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN = (ArrayList<HashMap<TIntHashSet, Integer>>) ejec.cloneTokens();
        //ArrayList<HashMap<TIntHashSet, Integer>> tokensN = (ArrayList<HashMap<TIntHashSet, Integer>>) state.getMarcado().cloneTokens();
        marking.setTokens(tokensN);
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(ejec.getPossibleEnabledTasks());
        //possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        marking.setPossibleEnabledTasks(possibleEnabledTasksClone);

        successor.setMarcado(marking);

//        System.out.println("MARCADO ANTES");
//        System.out.println(successor.getMarcado().toString());
//        System.out.println("Tareas que se pueden ejecutar: " + successor.getMarcado().getEnabledElements());

        switch (action) {
            case OK:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarMarcado(ejec.getTareaOK());
                //System.out.println("TAREA A HACER EL OK ----------------> " + ejec.getTareaOK());
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(OK);
                successor.setTarea(ejec.getTareaOK());
                break;
            case SKIP:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaExecute();
                successor.setTarea(t);
                //System.out.println("TAREA A HACER EL SKIP ----------------> " + t);
                successor.avanzarMarcado(t);
                successor.setMov(SKIP);
                break;
            case INSERT:
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(INSERT);
                //System.out.println("TAREA A HACER EL INSERT ----------------> " + ejec.getTareaINSERT());
                //successor.avanzarMarcado(ejec.getTareaINSERT());
                successor.setTarea(state.getTarea());
                break;
        }

//        System.out.println("MARCADO DESPUES");
//        System.out.println(successor.getMarcado().toString());
//        System.out.println("EnabledTasks " + successor.getMarcado().getEnabledElements());

        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, State> transition) {
        StateMove action = transition.getAction();
        Double cost = null;
        switch (action) {
            case SKIP:
                cost = 3d;
                break;
            case INSERT:
                cost = 1d;
                break;
            case OK:
                cost = 0.0001d;
                break;
        }
        return cost;
    }
}
