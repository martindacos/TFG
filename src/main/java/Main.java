
import Modelo.Modelo;
import Problem.EjecTareas;
import Problem.Estadisticas;
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
        Modelo miModelo;
        Readers r;

        switch (args.length) {
            case 2:
                //Cargamos el Modelo y el Log
                r = new Readers(args[0], args[1]);
                miModelo = Modelo.getModelo(r.getInd());
                miModelo.getInd().print();
                
                ArrayList<Traza> p2 = new ArrayList<>();
                Traza t2 = new Traza();
                t2.anadirTarea(0);
                t2.anadirTarea(1);
                t2.anadirTarea(2);
//                t2.anadirTarea(8);
//                t2.anadirTarea(0);
//                t2.anadirTarea(3);
//                t2.anadirTarea(6);
//                t2.anadirTarea(4);
//                t2.anadirTarea(5);
//                t2.anadirTarea(7);
                p2.add(t2);
                r.setTraces(p2);             
                //r.setTraces1();
                break;
            default:
                //Cargamos un 
                r = new Readers();
                miModelo = Modelo.getModelo();
                miModelo.getInd().print();
                ArrayList<Traza> prueba = new ArrayList<>();
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
                
                prueba.add(test);
                prueba.add(test2);
                prueba.add(test3);
                prueba.add(test4);
                prueba.add(test5);
                r.setTraces(prueba);
        }

        final State initialState = new State(miModelo.getInd());
        initialState.getMarcado().restartMarking();

        EjecTareas ejec = new EjecTareas();

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, State> af = new ActionFunction<StateMove, State>() {
            @Override
            public Iterable<StateMove> actionsFor(State state) {
                return validMovementsFor(state, r.getTrazaActual(), ejec);
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

        //System.out.println(initialState.getMarcado().toString());
        for (int i = 0; i < r.getTraces().size(); i++) {
            WeightedNode n = null;
            double mejorScore = 0d;
            boolean parar = false;
            initialState.getMarcado().restartMarking();
            ejec.clear();
            r.getTrazaActual().print();
            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            for (WeightedNode n1 : Hipster.createAStar(p)) {
                State s = (State) n1.state();
                //Final del modelo y final de la traza (para hacer skips y inserts al final)
                if (parar) {
                    //System.out.println("------------------SIGO------------------");
                    double estimacion = (double) n1.getScore();
                    //System.out.println("ESTIMACION " + estimacion + " MEJOR SCORE " + mejorScore);
                    if (estimacion > mejorScore) {
                        break;
                    }
                }

                //Añadimos que non quede ninguna tarea activa en el modelo
                if (r.getTrazaActual().procesadoTraza(s.getPos()) && s.finalModelo() && s.noEnabled()) {
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
        
        Estadisticas e = new Estadisticas();
        //Impresion del alineamiento de una manera más visual
        salidaVisual(nodosSalida,r);
        System.out.println();
        System.out.println("****************************************************************");
        System.out.println("Tiempo total de cálculo = " + total_time + " ms");
        //Imprimimos el coste del individuo
        System.out.println("Coste del individuo: " + e.costeIndividuo(r.getTraces()));
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(State state, Traza trace, EjecTareas ejec) {
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        ejec.clear();

        Integer e = trace.leerTarea(state.getPos());

        if (!trace.procesadoTraza(state.getPos())) {
            //Insetar la tarea actual de la traza
            movements.add(INSERT);
            ejec.anadirInsert(e);

            //Posibles tareas a ejecutar en el modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                int id = tasks.next();
                if (e == id) {
                    //OK si la tarea de la traza se encuentra enabled
                    movements.add(OK);
                    ejec.anadirOk(e);
                    break;
                }
            }
        }
        //En realidad podría sacar o if. Teño que facelo siempre.
        if (!state.noEnabled()) {
            //Todas as tareas activas, incluso la de la traza
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                int id = tasks.next();
                movements.add(SKIP);
                ejec.anadirSkip(id);
            }
        }else {
            //Forzamos el avanzado buscando tareas con algún token
            ArrayList<Integer> tasks = state.getTaskWithTokens();
            for (int i=0; i<tasks.size(); i++) {
                movements.add(TOTALSKIP);
                ejec.anadirTotalSkip(tasks.get(i));
            }
        }

        //Realizamos la copia del marcado
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
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
        marking.setTokens(tokensN);
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(ejec.getPossibleEnabledTasks());
        marking.setPossibleEnabledTasks(possibleEnabledTasksClone);

        successor.setMarcado(marking);

//        System.out.println("MARCADO ANTES");
//        System.out.println(successor.getMarcado().toString());
//        System.out.println("Tareas que se pueden ejecutar: " + successor.getMarcado().getEnabledElements());

        switch (action) {
            case OK:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarMarcado(ejec.getTareaOK());
                System.out.println("TAREA A HACER EL OK ----------------> " + ejec.getTareaOK());
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(OK);
                successor.setTarea(ejec.getTareaOK());
                break;
            case SKIP:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaSkip();
                successor.setTarea(t);
                System.out.println("TAREA A HACER EL SKIP ----------------> " + t);
                successor.avanzarMarcado(t);
                successor.setMov(SKIP);
                break;
            case INSERT:
                //Teño que ejecutar a tarea do modelo
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(INSERT);
                System.out.println("TAREA A HACER EL INSERT ----------------> " + ejec.getTareaINSERT());
                //Si la tarea existe en el modelo la ejecutamos
                if (Modelo.getModelo().getInd().getTask(ejec.getTareaINSERT()) != null) successor.avanzarMarcado(ejec.getTareaINSERT());
                successor.setTarea(ejec.getTareaINSERT());
                break;
            case TOTALSKIP:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                Integer ta = ejec.leerTareaTotalSkip();
                successor.setTarea(ta);
                System.out.println("TAREA A HACER EL TOTALSKIP ----------------> " + ta);
                successor.avanzarMarcado(ta);
                successor.setMov(TOTALSKIP);
                break;
        }
        System.out.println("Pos traza " + successor.getPos());
        System.out.println("MARCADO DESPUES");
        System.out.println(successor.getMarcado().toString());
        System.out.println("EnabledTasks " + successor.getMarcado().getEnabledElements());

        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, State> transition) {
        StateMove action = transition.getAction();
        Double cost = null;
        switch (action) {
            case TOTALSKIP:
                cost = 3d;
                break;
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
    
    public static void salidaVisual(ArrayList<WeightedNode> nodosSalida, Readers r) {
        for (int i = 0; i < nodosSalida.size(); i++) {
            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial
            it2.next();
            System.out.println(nodosSalida.get(i).path());
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
                } else if (node.action().equals(TOTALSKIP)) {
                    System.out.println("    >>          >>");
                } else {
                    System.out.println("    " + r.getTrazaPos(i).leerTarea(s.getPos() - 1) + "          >>");
                }
            }
            System.out.println();
            System.out.println("Coste del Alineamiento " + r.getTrazaPos(i).getScore());
        }
    }
}
