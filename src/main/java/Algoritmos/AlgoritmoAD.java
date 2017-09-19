package Algoritmos;

import Configuracion.ParametrosImpl;
import Problem.EjecTareas;
import Salida.InterfazSalida;
import Problem.InterfazTraza;
import Problem.NState.State;
import Problem.NState.StateMove;
import static Problem.NState.StateMove.*;
import Gui.PantallaAlgoritmo;
import Estadisticas.EstadisticasImpl;
import Estadisticas.InterfazEstadisticas;
import Problem.Readers;
import Salida.SalidaTerminalImpl;
import domainLogic.exceptions.EmptyLogException;
import domainLogic.exceptions.InvalidFileExtensionException;
import domainLogic.exceptions.MalformedFileException;
import domainLogic.exceptions.NonFinishedWorkflowException;
import domainLogic.exceptions.WrongLogEntryException;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
import es.usc.citius.hipster.algorithm.ADStarForward;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.function.TransitionFunction;
import es.usc.citius.hipster.model.function.impl.ADStarNodeExpander;
import es.usc.citius.hipster.model.function.impl.ADStarNodeFactory;
import es.usc.citius.hipster.model.function.impl.BinaryOperation;
import es.usc.citius.hipster.model.function.impl.ScalarOperation;
import es.usc.citius.hipster.model.impl.ADStarNodeImpl;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AlgoritmoAD {

    public static void main(String[] args, PantallaAlgoritmo salidaGrafica) throws IOException, EmptyLogException, WrongLogEntryException, NonFinishedWorkflowException, InvalidFileExtensionException, MalformedFileException {
        Readers miReader;
        ParametrosImpl parametrosImpl;

        parametrosImpl = ParametrosImpl.getParametrosImpl();
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

        final State initialState = new State(miReader.getInd());
        initialState.getMarcado().restartMarking();

        EjecTareas ejec = new EjecTareas();

        /*Funciones para el algoritmo AD* */
        TransitionFunction<StateMove, State> tf = new TransitionFunction<StateMove, State>() {
            @Override
            public Iterable<Transition<StateMove, State>> transitionsFrom(State state) {
                return successorFunction(state, miReader.getTrazaActual(), ejec, miReader.getInd());
            }
        };

        TransitionFunction<StateMove, State> pf = new TransitionFunction<StateMove, State>() {
            @Override
            public Iterable<Transition<StateMove, State>> transitionsFrom(State state) {
                return predecessorFunction(state);
            }
        };

        //Definición de la función de coste
        CostFunction<StateMove, State, Double> cf = new CostFunction<StateMove, State, Double>() {
            @Override
            public Double evaluate(Transition<StateMove, State> transition) {
                return evaluateToState(transition, parametrosImpl, ejec);
            }
        };

        //Definición de la función heurística
        HeuristicFunction<State, Double> hf = new HeuristicFunction<State, Double>() {
            @Override
            public Double estimate(State state) {
                double heu = miReader.getTrazaActual().getHeuristica(state.getPos(), miReader.getInd(), state.getTarea()) * parametrosImpl.getC_SINCRONO();
                //double heu = 0d;
                return heu;
            }
        };

        final State finalState = null;

        //Guardamos el coste mínimo del camino del individuo
        InterfazEstadisticas e = new EstadisticasImpl();
        //Creamos las interfaces de salida por terminal
        InterfazSalida salida = new SalidaTerminalImpl();
        //salidaGrafica.setTotalTrazas(miReader.getTraces().size());

        ArrayList<AbstractNode> nodosSalida = new ArrayList<>();
        //Tiempo total del cálculo del algoritmo
        long total_time = 0;

        //Total de memoria consumida por el algoritmo
        double total_memoria = 0;

        System.out.println(initialState.getMarcado().toString());
        ADStarForward.Iterator it;

        //Iteramos sobre el problema de búsqueda
        for (int i = 0; i < miReader.getTraces().size(); i++) {

            //Definimos el problema de búsqueda
            //create components for the algorithm (factory of nodes)
            ADStarNodeFactory factory = new ADStarNodeFactory<>(
                    BinaryOperation.doubleAdditionOp(),
                    ScalarOperation.doubleMultiplicationOp(),
                    hf
            );

            //crate components for the algorithm (node expander)
            ADStarNodeExpander expander = new ADStarNodeExpander<>(
                    tf,
                    pf,
                    cf,
                    hf,
                    BinaryOperation.doubleAdditionOp(),
                    ScalarOperation.doubleMultiplicationOp(),
                    factory,
                    parametrosImpl.getE_INICIAL()
            );

            initialState.getMarcado().restartMarking();
            it = new ADStarForward<>(initialState, finalState, expander).iterator();

            ADStarNodeImpl n = null;
            double mejorScore = 0d;
            boolean parar = false;

            //miReader.avanzarPos();
            miReader.getTrazaActual().print();
            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            miReader.getTrazaActual().clear();

            while (it.hasNext()) {
//                Map<State, ADStarNodeImpl> listaAbiertos = it.getOpen();
//                System.out.println("Tamaño lista abiertos: " + listaAbiertos.size());

                ADStarNodeImpl n1 = (ADStarNodeImpl) it.next();
                State s = (State) n1.state();
                double estimacion = (double) n1.getScore();
                //System.out.println("Estimación: " + estimacion);

                //Final del modelo y final de la traza (para hacer skips y inserts al final)
                if (parar) {
                    //System.out.println("------------------SIGO------------------");

                    //System.out.println("ESTIMACION " + estimacion + " MEJOR SCORE " + mejorScore);
                    if (estimacion > mejorScore) {
                        //Paramos el bucle cuando el valor de epsilon actual es 1 o el valor mínimo
                        //System.out.println("Epsilon actual: " + it.getEpsilon() + " Epsilon final: " + parametrosImpl.getE_FINAL());
                        if (it.getEpsilon() <= 1d || it.getEpsilon() <= parametrosImpl.getE_FINAL()) {
                            break;
                        }
                        //Reducimos el parámetro de epsilon para refinar la búsqueda
                        it.setEpsilon(AlgoritmoAD.disminuirEpsilon(parametrosImpl, it.getEpsilon()));
                    }
                }

                if (miReader.getTrazaActual().procesadoTraza(s.getPos()) && s.finalModelo()) {
                    parar = true;
                    if (mejorScore == 0) {
                        mejorScore = (double) n1.getCost();
                        n = n1;
                    } else {
                        double aux = (double) n1.getCost();
                        if (aux < mejorScore) {
                            mejorScore = aux;
                            n = n1;
                        }
                    }
                } else if (miReader.getTrazaActual().procesadoTraza(s.getPos()) && s.finalModelo(miReader.getInd())) {
                    parar = true;
                    if (mejorScore == 0) {
                        mejorScore = (double) n1.getCost() + parametrosImpl.getC_MODELO();
                        n = n1;
                    } else {
                        double aux = (double) n1.getCost() + parametrosImpl.getC_MODELO();
                        if (aux < mejorScore) {
                            mejorScore = aux;
                            n = n1;
                        }
                    }
                }
            }
            time_end = System.currentTimeMillis();
            total_time = total_time + (time_end - time_start);
            total_memoria = total_memoria + miReader.getTrazaActual().getMemoriaC();
            //Guardamos el nodo con los estados soluciones de la traza
            nodosSalida.add(n);
            //Guardamos el coste obtenido en el alineamiento
            int j = 0;
            Iterator it2 = n.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            while (it2.hasNext()) {
                ADStarNodeImpl node = (ADStarNodeImpl) it2.next();
                if (node.action().equals(SINCRONO)) {
                    j++;
                }
            }
//            System.out.println(mejorScore);
            double sobrante = parametrosImpl.getC_SINCRONO() * j;
            double nuevoScore = mejorScore - sobrante;
            double nuevoScoreR = Math.rint(nuevoScore * 100000) / 100000;

            miReader.getTrazaActual().setScore(nuevoScoreR);
            //Guardamos el tiempo de cálculo del alineamiento
            miReader.getTrazaActual().setTiempoC(time_end - time_start);
            //Imprimimos el alineamiento calculado y sus estadísticas           
            //salidaGrafica.ActualizarTrazas(miReader.getTrazaActual(), n);
            salida.ActualizarTrazas(miReader.getTrazaActual(), n, false, miReader.getInd());

            //Pasamos a la siguientes traza del procesado
            miReader.avanzarPos();
        }

        //Calculamos el Conformance Checking del modelo
        double fitnessNuevo = e.fitnessNuevo(miReader.getTraces(), nodosSalida);
        double precission = e.precission(miReader.getTraces(), nodosSalida);
        IndividualFitness individualFitness = new IndividualFitness();
        individualFitness.setCompleteness(fitnessNuevo);
        individualFitness.setPreciseness(precission);
        miReader.getInd().setFitness(individualFitness);

        e.setMemoriaConsumida(total_memoria);

        //salidaGrafica.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time);
        salida.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time, e.getMemoriaConsumida());
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<Transition<StateMove, State>> successorFunction(State state, InterfazTraza trace, EjecTareas ejec, CMIndividual m) {
        boolean anadirForzadas = false;
        boolean anadirForzadasTraza = false;
        //Creamos una lista con los movimientos posibles
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        //Limpiamos la variables de la clase auxiliar
        ejec.clear();
        //Leemos la tarea actual de la traza
        Integer e = trace.leerTarea(state.getPos());

//        System.out.println("-----------------------");
//        System.out.println("Movimiento efectuado : " + state.getMov());
//        System.out.println("Tarea sobre la que se hizo el movimiento : " + state.getTarea());
//
//        System.out.println("Pos de la traza (lo contiene el estado) : " + state.getPos());
//        System.out.println("Tarea de la traza : " + e);
//        System.out.println("Marcado en la seleccion de movimientos " + state.getMarcado().toString());
//        System.out.println("-----------------------");
        //Almacenamos el marcado en una clase auxiliar para su posterior copia
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
        ejec.setTokens(tokensA);
        ejec.setEndPlace(state.getMarcado().getEndPlace());
        ejec.setNumOfTokens(state.getMarcado().getNumberTokens());
        ejec.setStartPlace(state.getMarcado().getStartPlace());
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        ejec.setPossibleEnabledTasks(possibleEnabledTasksClone);

//        if (e == null) {
//            anadirForzadas = true;
//        } else if (state.isEjecutedTask(e)) {
//            anadirForzadasTraza = true;
//        }
        //Si existen elementos activos en el modelo
        if (state.Enabled()) {
            if (e == null) {
                anadirForzadas = true;
            } else if (state.isEjecutedTask(e)) {
                anadirForzadasTraza = true;
            }
            //Tareas activas del modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            //Anadimos un movimiento por cada tarea
            while (tasks.hasNext()) {
                int id = tasks.next();
                movements.add(MODELO);
                //Anadimos la tarea a la coleccion para ejecutarla
                ejec.anadirModelo(id);
            }
        } else {
            anadirForzadasTraza = true;
            anadirForzadas = true;
        }

        //Si NO acabamos de procesar la traza
        if (!trace.procesadoTraza(state.getPos())) {
            //Anadimos el movimiento posible
            movements.add(TRAZA);
            //Anadimos la tarea a la clase auxiliar
            ejec.anadirTraza(e);

            //Tareas activas del modelo
            TIntHashSet posiblesTareas = state.getTareas();
            TIntIterator tasks = posiblesTareas.iterator();
            while (tasks.hasNext()) {
                //Obtenemos el identificador de la tarea
                int id = tasks.next();
                //Si la tarea de la traza coincide con la activa
                if (e == id) {
                    //Anadimos el movimiento y la tarea
                    movements.add(SINCRONO);
                    ejec.anadirSincrono(e);
                    break;
                }
            }
        }

        //Forzamos las tareas que tienen algún token en su entrada
        if (anadirForzadas) {
            //Buscamos las tareas que tienen algún token en su entrada
            ejec.tareasTokensEntrada(state.getMarcado().getTokens());
        }

        //Solo forzamos las tareas restantes de la traza
        if (anadirForzadasTraza) {
            //Añadimos como tareas forzados las tareas restantes de la traza
            ejec.addTareasTraza(trace, state.getPos());
        }

        if (anadirForzadas || anadirForzadasTraza) {
            //Contamos el número de tokens necesarios para ejecutarlas
            Integer numeroTareas = ejec.tareasTokensRestantes(state.getMarcado().getTokens());
            for (int i = 0; i < numeroTareas; i++) {
                movements.add(MODELO_FORZADO);
            }
        }

        //System.out.println(movements);
        trace.addMemoriaC(movements.size());
        //Devolvemos una coleccion con los posibles movimientos
        List<Transition<StateMove, State>> it = new ArrayList();
        //Devolvemos una coleccion con los posibles movimientos
        for (StateMove action : movements) {
            State successor = AlgoritmoAD.applyActionToState(action, state, ejec, m);
            Transition t = new Transition(state, action, successor);
            it.add(t);
        }

        return it;
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
            case SINCRONO:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarMarcado(ejec.getTareaSINCRONA());
                //System.out.println("TAREA A HACER EL OK ----------------> " + ejec.getTareaSINCRONA());
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(SINCRONO);
                successor.setTarea(ejec.getTareaSINCRONA());
                break;
            case MODELO:
                //Avanzamos el modelo con una tarea que no tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaModelo();
                successor.setTarea(t);
                //System.out.println("TAREA A HACER EL SKIP ----------------> " + t);
                successor.avanzarMarcado(t);
                successor.setMov(MODELO);
                break;
            case TRAZA:
                //Teño que ejecutar a tarea do modelo
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(TRAZA);
                //System.out.println("TAREA A HACER EL INSERT ----------------> " + ejec.getTareaTRAZA());
                successor.setTarea(ejec.getTareaTRAZA());
                break;
            case MODELO_FORZADO:
                t = ejec.leerTareaModeloForzado();
                successor.setTarea(t);
                //System.out.println("TAREA A HACER EL ARTIFICIAL ----------------> " + t);
                successor.avanzarMarcado(t);
                successor.setMov(MODELO_FORZADO);
                break;
        }
//        System.out.println("Pos traza " + successor.getPos());
//        System.out.println("MARCADO DESPUES");
//        System.out.println(successor.getMarcado().toString());
//        System.out.println("EnabledTasks " + successor.getMarcado().getEnabledElements());

        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, State> transition, ParametrosImpl parametrosImpl, EjecTareas ejec) {
        StateMove action = transition.getAction();
        Double cost = null;
        switch (action) {
            case MODELO:
                cost = parametrosImpl.getC_MODELO();
                break;
            case TRAZA:
                cost = parametrosImpl.getC_TRAZA();
                break;
            case SINCRONO:
                cost = parametrosImpl.getC_SINCRONO();
                break;
            case MODELO_FORZADO:
                cost = parametrosImpl.getC_MODELO_FORZADO() + ejec.tokenUsados(transition.getState().getTarea()) + 1;
                break;
        }
        return cost;
    }

    private static double disminuirEpsilon(ParametrosImpl p, double epsilonActual) {
        double epsilon;

        //Restamos al valor de epsilon actual su intervalo
        epsilon = epsilonActual - p.getE_INTERVALO();
        //Comprobamos que el nuevo valor no es inferior al mínimo
        if (epsilon < p.getE_FINAL()) {
            epsilon = p.getE_FINAL();
        }
        //Si el valor es inferior a 1 no sería un epsilon valido
        if (epsilon < 1d) {
            epsilon = 1d;
        }

        return epsilon;
    }

    private static Iterable<Transition<StateMove, State>> predecessorFunction(State state) {
        System.out.println("Not implemented yet");
        return null;
    }
}
