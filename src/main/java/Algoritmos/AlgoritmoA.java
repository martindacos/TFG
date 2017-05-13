package Algoritmos;


import Configuracion.ParametrosImpl;
import Problem.EjecTareas;
import Problem.InterfazSalida;
import Problem.InterfazTraza;
import Problem.NState.State;
import Problem.NState.StateMove;
import static Problem.NState.StateMove.*;
import Gui.PantallaAlgoritmo;
import Problem.EstadisticasImpl;
import Problem.InterfazEstadisticas;
import Problem.Readers;
import Problem.SalidaTerminalImpl;
import Problem.Traza;
import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
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
import java.util.LinkedList;
import java.util.Random;

public class AlgoritmoA {

    public static void main(String[] args, PantallaAlgoritmo salidaGrafica) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        Readers miReader;
        ParametrosImpl parametrosImpl;
        
        parametrosImpl = ParametrosImpl.getParametrosImpl();
        //Logs/g3/grpd_g3pi300.xes Logs/g3/g3.hn
        //balancedLogs/noise_0/a7/a7_Modif.xes balancedLogs/desiredNets/a7.hn
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
                ArrayList<InterfazTraza> prueba = new ArrayList<>();
                InterfazTraza test = new Traza();
                test.anadirTarea(0);
                test.anadirTarea(1);
                test.anadirTarea(2);
                test.anadirTarea(3);
                test.anadirTarea(4);
                test.setNumRepeticiones(1);
                
                InterfazTraza test2 = new Traza();
                test2.anadirTarea(0);
                test2.anadirTarea(1);
                test2.anadirTarea(2);
                test2.anadirTarea(4);
                test2.setNumRepeticiones(1);
                
                InterfazTraza test3 = new Traza();
                test3.anadirTarea(0);
                test3.setNumRepeticiones(1);
                
                InterfazTraza test4 = new Traza();
                test4.anadirTarea(4);
                test4.setNumRepeticiones(1);
                
                InterfazTraza test5 = new Traza();
                test5.anadirTarea(0);
                test5.anadirTarea(1);
                test5.anadirTarea(3);
                test5.anadirTarea(0);
                test5.anadirTarea(4);
                test5.setNumRepeticiones(1);
                
                prueba.add(test);
                prueba.add(test2);
                prueba.add(test3);
                prueba.add(test4);
                prueba.add(test5);
                miReader.setTraces(prueba);
        }

        final State initialState = new State(miReader.getInd());
        initialState.getMarcado().restartMarking();

        EjecTareas ejec = new EjecTareas();

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, State> af = new ActionFunction<StateMove, State>() {
            @Override
            public Iterable<StateMove> actionsFor(State state) {
                return validMovementsFor(state, miReader.getTrazaActual(), ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> atf;
        atf = new ActionStateTransitionFunction<StateMove, State>() {
            @Override
            public State apply(StateMove action, State state) {
                return applyActionToState(action, state, ejec, miReader.getInd());
            }
        };

        //Definición de la función de coste
        CostFunction<StateMove, State, Double> cf = new CostFunction<StateMove, State, Double>() {
            @Override
            public Double evaluate(Transition<StateMove, State> transition) {
                return evaluateToState(transition, parametrosImpl);
            }
        };

        //Definición de la función heurística
        HeuristicFunction<State, Double> hf = new HeuristicFunction<State, Double>() {
            @Override
            public Double estimate(State state) {
                //Sólo Poñemos a Heurística. Da g() xa se encarga Hipster.
                //Heurística. Número de elementos que faltan por procesar da traza
                return miReader.getTrazaActual().getHeuristica(state.getPos());
            }
        };

        /********************************************************************************************/
        
        /*
        Funciones para el algoritmo A* en modo "mínimo"
        */
        
        ActionFunction<StateMove, State> maf = new ActionFunction<StateMove, State>() {
            @Override
            public Iterable<StateMove> actionsFor(State state) {
                return minimumMovementsFor(state, ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> matf;
        matf = new ActionStateTransitionFunction<StateMove, State>() {
            @Override
            public State apply(StateMove action, State state) {
                return minimumActionToState(action, state, ejec, miReader.getInd());
            }
        };

        //Definición de la función de coste con un coste fijo de 3 (el de skip)
        CostFunction<StateMove, State, Double> mcf = new CostFunction<StateMove, State, Double>() {
            @Override
            public Double evaluate(Transition<StateMove, State> transition) {
                return parametrosImpl.getSKIP();
            }
        };

        /*
        Definición de la función heurística con un coste fijo de 0, ya que no
        hay tareas en la traza
        */
        HeuristicFunction<State, Double> mhf = new HeuristicFunction<State, Double>() {
            @Override
            public Double estimate(State state) {
                return 0d;
            }
        };
        
        final State minimumInitialState = new State(miReader.getInd());
        minimumInitialState.getMarcado().restartMarking();
        
        //Definimos el problema de búsqueda para camino mínimo
        SearchProblem<StateMove, State, WeightedNode<StateMove, State, Double>> mp
                = ProblemBuilder.create()
                        .initialState(minimumInitialState)
                        .defineProblemWithExplicitActions()
                        .useActionFunction(maf)
                        .useTransitionFunction(matf)
                        .useCostFunction(mcf)
                        .useHeuristicFunction(mhf)
                        .build();
        
        //Definimos el problema de búsqueda
        SearchProblem<StateMove, State, WeightedNode<StateMove, State, Double>> p
                = ProblemBuilder.create()
                        .initialState(initialState)
                        .defineProblemWithExplicitActions()
                        .useActionFunction(af)
                        .useTransitionFunction(atf)
                        .useCostFunction(cf)
                        .useHeuristicFunction(hf)
                        .build();
 
        //Nodo final
        WeightedNode mN = null;
        Double bestScore = 0d;
        boolean stop = false;

        for (WeightedNode n1 : Hipster.createAStar(mp)) {
            State s = (State) n1.state();
            if (stop) {
                double estimacion = (double) n1.getScore();
                if (estimacion > bestScore) {
                    break;
                }
            }

            if (s.finalModelo()) {
                stop = true;
                if (bestScore == 0) {
                    bestScore = (double) n1.getScore();
                    mN = n1;
                } else {
                    double aux = (double) n1.getScore();
                    if (aux < bestScore) {
                        bestScore = aux;
                        mN = n1;
                    }
                }
            }
        }

        //Guardamos el coste mínimo del camino del individuo
        InterfazEstadisticas e = new EstadisticasImpl();
        e.setCosteCorto(bestScore);
        //Creamos las interfaces de salida por terminal
        InterfazSalida salida = new SalidaTerminalImpl();     
        salidaGrafica.imprimirModelo(miReader.getInd());
        
        salida.minimumSalidaVisual(mN, bestScore);
        salidaGrafica.minimumSalidaVisual(mN, bestScore);
        
        ArrayList<WeightedNode> nodosSalida = new ArrayList<>();
        //Tiempo total del cálculo del algoritmo
        long total_time = 0;

        //System.out.println(initialState.getMarcado().toString());
        //Iteramos sobre el problema de búsqueda
        for (int i = 0; i < miReader.getTraces().size(); i++) {
            WeightedNode n = null;
            double mejorScore = 0d;
            boolean parar = false;
            initialState.getMarcado().restartMarking();
            //miReader.getTrazaActual().print();
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

                if (miReader.getTrazaActual().procesadoTraza(s.getPos()) && s.finalModelo()) {
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
            total_time = total_time + (time_end - time_start);
            //Guardamos el nodo con los estados soluciones de la traza
            nodosSalida.add(n);
            //Guardamos el coste obtenido en el alineamiento
            miReader.getTrazaActual().setScore(mejorScore);
            //Guardamos el tiempo de cálculo del alineamiento
            miReader.getTrazaActual().setTiempoC(time_end - time_start);    
            //           
            salidaGrafica.ActualizarTrazas(miReader.getTrazaActual(), n);
            //Pasamos a la siguientes traza del procesado
            miReader.avanzarPos();
        }

        //Para guardar el número de tareas activas en cada estado
        //Impresion del alineamiento de una manera más visual
        salida.salidaVisual(nodosSalida, miReader);       
        //salidaGrafica.salidaVisual(nodosSalida, miReader);
        
        //Calculamos el Conformance Checking del modelo
        double fitness = e.fitness(miReader.getTraces());
        double precission = e.precision(miReader.getTraces(), nodosSalida);
        IndividualFitness individualFitness = new IndividualFitness();
        individualFitness.setCompleteness(fitness);
        individualFitness.setPreciseness(precission);
        miReader.getInd().setFitness(individualFitness);
               
        salidaGrafica.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time);
        salida.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time);
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(State state, InterfazTraza trace, EjecTareas ejec) {
        //Creamos una lista con los movimientos posibles
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        //Limpiamos la variables de la clase auxiliar
        ejec.clear();
        //Leemos la tarea actual de la traza
        Integer e = trace.leerTarea(state.getPos());
        //Si NO acabamos de procesar la traza
        if (!trace.procesadoTraza(state.getPos())) {
            //Anadimos el movimiento posible
            movements.add(INSERT);
            //Anadimos la tarea a la clase auxiliar
            ejec.anadirInsert(e);

            //Tareas activas del modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                //Obtenemos el identificador de la tarea
                int id = tasks.next();
                //Si la tarea de la traza coincide con la activa
                if (e == id) {
                    //Anadimos el movimiento y la tarea
                    movements.add(OK);
                    ejec.anadirOk(e);
                    break;
                }
            }
        }
        //Si existen tareas activas en el modelo
        if (state.Enabled()) {
            //Tareas activas del modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            //Anadimos un movimiento por cada tarea
            while (tasks.hasNext()) {
                int id = tasks.next();
                movements.add(SKIP);
                //Anadimos la tarea a la coleccion para ejecutarla
                ejec.anadirSkip(id);
            }
        }

        //Almacenamos el marcado en una clase auxiliar para su posterior copia
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
        ejec.setTokens(tokensA);
        ejec.setEndPlace(state.getMarcado().getEndPlace());
        ejec.setNumOfTokens(state.getMarcado().getNumberTokens());
        ejec.setStartPlace(state.getMarcado().getStartPlace());
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        ejec.setPossibleEnabledTasks(possibleEnabledTasksClone);

        //System.out.println(movements);
        //Devolvemos una coleccion con los posibles movimientos
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
                //System.out.println("TAREA A HACER EL OK ----------------> " + ejec.getTareaOK());
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(OK);
                successor.setTarea(ejec.getTareaOK());
                break;
            case SKIP:
                //Avanzamos el modelo con una tarea que no tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaSkip();
                successor.setTarea(t);
                //System.out.println("TAREA A HACER EL SKIP ----------------> " + t);
                successor.avanzarMarcado(t);
                successor.setMov(SKIP);
                break;
            case INSERT:
                //Teño que ejecutar a tarea do modelo
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(INSERT);
                //System.out.println("TAREA A HACER EL INSERT ----------------> " + ejec.getTareaINSERT());
                successor.setTarea(ejec.getTareaINSERT());
                break;
        }
//        System.out.println("Pos traza " + successor.getPos());
//        System.out.println("MARCADO DESPUES");
//        System.out.println(successor.getMarcado().toString());
//        System.out.println("EnabledTasks " + successor.getMarcado().getEnabledElements());

        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, State> transition, ParametrosImpl parametrosImpl) {
        StateMove action = transition.getAction();
        Double cost = null;
        switch (action) {
            case SKIP:
                cost = parametrosImpl.getSKIP();
                break;
            case INSERT:
                cost = parametrosImpl.getINSERT();
                break;
            case OK:
                cost = parametrosImpl.getOK();
                break;
        }
        return cost;
    }
    
    /*
    Funciones de búsqueda para obtener el coste mínimo de un camino del modelo
    */
    
    //Devolvemos el movimiento de SKIP para las posibles tareas
    private static Iterable<StateMove> minimumMovementsFor(State state, EjecTareas ejec) {
        //Creamos una lista con los movimientos posibles
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        //Limpiamos la variables de la clase auxiliar
        ejec.clear();
        //Si existen tareas activas en el modelo
        if (state.Enabled()) {
            //Tareas activas del modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            //Anadimos un movimiento por cada tarea
            while (tasks.hasNext()) {
                int id = tasks.next();
                movements.add(SKIP);
                //Anadimos la tarea a la coleccion para ejecutarla
                ejec.anadirSkip(id);
            }
        }

        //Almacenamos el marcado en una clase auxiliar para su posterior copia
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
        ejec.setTokens(tokensA);
        ejec.setEndPlace(state.getMarcado().getEndPlace());
        ejec.setNumOfTokens(state.getMarcado().getNumberTokens());
        ejec.setStartPlace(state.getMarcado().getStartPlace());
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        ejec.setPossibleEnabledTasks(possibleEnabledTasksClone);

        //Devolvemos una coleccion con los posibles movimientos
        return movements;
    }

    //Realizamos la acción correspondiente al movimiento
    private static State minimumActionToState(StateMove action, State state, EjecTareas ejec, CMIndividual m) {
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

        switch (action) {
            case SKIP:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaSkip();
                successor.setTarea(t);
                successor.avanzarMarcado(t);
                successor.setMov(SKIP);
                break;
        }

        return successor;
    }
}
