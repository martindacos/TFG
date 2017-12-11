package es.usc.citius.aligments.algoritmos;

import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.estadisticas.EstadisticasImpl;
import es.usc.citius.aligments.estadisticas.InterfazEstadisticas;
import es.usc.citius.aligments.problem.EjecTareas;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState;
import es.usc.citius.aligments.problem.NState.State;
import es.usc.citius.aligments.problem.NState.StateMove;
import static es.usc.citius.aligments.problem.NState.StateMove.*;

import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
import es.usc.citius.hipster.algorithm.AStar;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.ActionFunction;
import es.usc.citius.hipster.model.function.ActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.*;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class AlgoritmoA {

    public static void problem(Readers miReader) {
        ParametrosImpl parametrosImpl;

        parametrosImpl = ParametrosImpl.getParametrosImpl();

        final NState.State initialState = new NState.State(miReader.getInd());
        initialState.getMarcado().restartMarking();

        EjecTareas ejec = new EjecTareas();

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, State> af = new ActionFunction<NState.StateMove, NState.State>() {
            @Override
            public Iterable<NState.StateMove> actionsFor(NState.State state) {
                return AlgoritmoA.validMovementsFor(state, miReader.getTrazaActual(), ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> atf;
        atf = new ActionStateTransitionFunction<NState.StateMove, NState.State>() {
            @Override
            public NState.State apply(NState.StateMove action, NState.State state) {
                return AlgoritmoA.applyActionToState(action, state, ejec, miReader.getInd());
            }
        };

        //Definición de la función de coste
        CostFunction<StateMove, State, Double> cf = new CostFunction<NState.StateMove, NState.State, Double>() {
            @Override
            public Double evaluate(Transition<NState.StateMove, NState.State> transition) {
                return AlgoritmoA.evaluateToState(transition, parametrosImpl, ejec);
            }
        };

        //Definición de la función heurística
        HeuristicFunction<State, Double> hf = new HeuristicFunction<NState.State, Double>() {
            @Override
            public Double estimate(NState.State state) {
                //Sólo Poñemos a Heurística. Da g() xa se encarga Hipster.
                //Heurística. Número de elementos que faltan por procesar da traza
                return miReader.getTrazaActual().getHeuristica(state.getPos(), miReader.getInd(), state.getTarea()) * parametrosImpl.getC_SINCRONO();
                //return 0d;
            }
        };

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

        //System.out.println(initialState.getMarcado().toString());
        //Iteramos sobre el problema de búsqueda
        for (int i = 0; i < miReader.getTraces().size(); i++) {
            initialState.getMarcado().restartMarking();
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

            WeightedNode n = null;
            double mejorScore = 0d;
            boolean parar = false;

            miReader.getTrazaActual().print();
            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            miReader.getTrazaActual().clear();
            AStar<StateMove, State, Double, WeightedNode<StateMove, State, Double>> astar = Hipster.createAStar(p);
            AStar.Iterator it = astar.iterator();

            while (it.hasNext()) {
//                Map<State, WeightedNode<StateMove, State, Double>> listaAbiertos = it.getOpen();
//                for (Map.Entry entry : listaAbiertos.entrySet()) {
//                    System.out.println(entry.getValue().toString());
//                }

                WeightedNode n1 = (WeightedNode) it.next();

                NState.State s = (NState.State) n1.state();
                double estimacion = (double) n1.getScore();
                //System.out.println("Estimacion coste estado seleccionado: " + estimacion);
                //Final del modelo y final de la traza (para hacer skips y inserts al final)
                if (parar) {
                    //System.out.println("------------------SIGO------------------");
                    //System.out.println("ESTIMACION " + estimacion + " MEJOR SCORE " + mejorScore);
                    if (estimacion > mejorScore) {
                        break;
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
            double j = 0d;
            Iterator it2 = n.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
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
            salida.ActualizarTrazas(miReader.getTrazaActual(), n, true, miReader.getInd());

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
    private static Iterable<StateMove> validMovementsFor(State state, InterfazTraza trace, EjecTareas ejec) {
        boolean anadirForzadas = false;
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
//        //System.out.println("Marcado en la seleccion de movimientos " + state.getMarcado().toString());
//        System.out.println("-----------------------");
        //Si NO acabamos de procesar la traza
        if (state.Enabled()) {
            //La tarea de la traza ya habia sido ejecutada en el modelo o la acabamos de procesar
            if (e == null) {
                anadirForzadas = true;
            } else if (state.isEjecutedTask(e)) {
                anadirForzadas = true;
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

        if (anadirForzadas) {
            //Añadimos la tarea de la traza
            if (!trace.procesadoTraza(state.getPos())) {
                //Anadimos el movimiento posible
                movements.add(MODELO_FORZADO);
                //Anadimos la tarea de la traza a la clase auxiliar
                ejec.anadirTareaForzada(e);
            }
            //Buscamos las tareas que tienen algún token en su entrada
            ejec.tareasTokensEntrada(state.getMarcado().getTokens());
            //Contamos el número de tokens necesarios para ejecutarlas
            Integer numeroTareas = ejec.tareasTokensRestantes(state.getMarcado().getTokens());
            for (int i = 1; i < numeroTareas; i++) {
                movements.add(MODELO_FORZADO);
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

        //System.out.println("Posible movimientos del estado : " + movements);
        trace.addMemoriaC(movements.size());
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
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
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
}
