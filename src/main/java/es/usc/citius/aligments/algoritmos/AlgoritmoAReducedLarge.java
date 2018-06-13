package es.usc.citius.aligments.algoritmos;

import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.properties.IndividualFitness;
import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.estadisticas.EstadisticasImpl;
import es.usc.citius.aligments.estadisticas.InterfazEstadisticas;
import es.usc.citius.aligments.problem.EjecTareas;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState.StateMove;
import es.usc.citius.aligments.problem.NStateLarge;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;
import es.usc.citius.hipster.algorithm.AStar;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.ActionFunction;
import es.usc.citius.hipster.model.function.ActionStateTransitionFunction;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.ProblemBuilder;
import es.usc.citius.hipster.model.problem.SearchProblem;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.*;

import static es.usc.citius.aligments.config.Parametros.PENALIZACION_FORZADO;
import static es.usc.citius.aligments.problem.NState.StateMove.*;

public class AlgoritmoAReducedLarge {

    private final static Logger LOGGER = Logger.getLogger("aligments");
    private static boolean print;
    private static es.usc.citius.aligments.utils.Timer timerMovs = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerAct = new es.usc.citius.aligments.utils.Timer();

    private static es.usc.citius.aligments.utils.Timer timerInicializarMarcado = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerClonarTokens = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerClonarPosiblesActivas = new es.usc.citius.aligments.utils.Timer();

    private static int contadorInstanciasMarcado = 0;

    public static void problem(Readers miReader, boolean logging) {
        es.usc.citius.aligments.utils.Timer timer = new es.usc.citius.aligments.utils.Timer();
        es.usc.citius.aligments.utils.Timer timerTotal = new es.usc.citius.aligments.utils.Timer();

        print = logging;
        if (print) {
            Handler fileHandler = null;
            try {
                fileHandler = new FileHandler("./aligments.log", false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            LOGGER.addHandler(fileHandler);
            //Evitar que el log salga por pantalla
            //LOGGER.setUseParentHandlers(false);
            //Definimos el nivel del log
            LOGGER.setLevel(Level.INFO);
        }

        ParametrosImpl parametrosImpl;

        parametrosImpl = ParametrosImpl.getParametrosImpl();

        final NStateLarge.StateLarge initialState = new NStateLarge.StateLarge(miReader.getInd());
        initialState.getMarcado().restartMarking();
        initialState.restartState();
        if (print) {
            LOGGER.log(Level.INFO, initialState.getMarcado().toString());
            LOGGER.log(Level.INFO, "Tareas que se pueden ejecutar: " + initialState.getMarcado().getEnabledElements());
        }

        EjecTareas ejec = new EjecTareas();
        //Guardamos el coste mínimo del camino del individuo
        InterfazEstadisticas e = new EstadisticasImpl();
        //Creamos las interfaces de salida por terminal
        InterfazSalida salida = new SalidaTerminalImpl();

        /*Funciones para el algoritmo A* */
        ActionFunction<StateMove, NStateLarge.StateLarge> af = new ActionFunction<StateMove, NStateLarge.StateLarge>() {
            @Override
            public Iterable<StateMove> actionsFor(NStateLarge.StateLarge state) {
                return AlgoritmoAReducedLarge.validMovementsFor(state, miReader.getTrazaActual(), ejec, miReader.getInd());
            }
        };

        ActionStateTransitionFunction<StateMove, NStateLarge.StateLarge> atf;
        atf = new ActionStateTransitionFunction<StateMove, NStateLarge.StateLarge>() {
            @Override
            public NStateLarge.StateLarge apply(StateMove action, NStateLarge.StateLarge state) {
                return AlgoritmoAReducedLarge.applyActionToState(action, state, ejec, miReader.getInd(), e);
            }
        };

        //Definición de la función de coste
        CostFunction<StateMove, NStateLarge.StateLarge, Double> cf = new CostFunction<StateMove, NStateLarge.StateLarge, Double>() {
            @Override
            public Double evaluate(Transition<StateMove, NStateLarge.StateLarge> transition) {
                return AlgoritmoAReducedLarge.evaluateToState(transition, parametrosImpl, ejec);
            }
        };

        //Definición de la función heurística
        HeuristicFunction<NStateLarge.StateLarge, Double> hf = new HeuristicFunction<NStateLarge.StateLarge, Double>() {
            @Override
            public Double estimate(NStateLarge.StateLarge state) {
                timer.resume();
                //Sólo Poñemos a Heurística. Da g() xa se encarga Hipster.
                //Heurística. Número de elementos que faltan por procesar da traza
                Double heuristicaPrecise = miReader.getTrazaActual().getHeuristica(state.getPos(), miReader.getInd(), state.getTarea());
                //Double heuristicaPrecise = miReader.getTrazaActual().getHeuristicaCajas(state.getPos(), miReader.getInd(), state.getTarea(), state.getTrazaMovs(), state.getSincroMovs());
                //Double heuristicaPrecise = miReader.getTrazaActual().getHeuristicaTokenReplay(state.getPos(), miReader.getInd(), state.getMarcado(), state.getTarea(), state);
                //Nueva heurística que tiene en cuenta tanto las tareas restantes por procesar del modelo como de la traza
                //TODO Refinar cas combinación dos elementos ou buscar unha nova solución (estima de máis)
                //Double heuristicaPrecise = miReader.getTrazaActual().getHeuristicaPrecise(state.getPos(), miReader.getInd(), state.getTarea());
                timer.pause();
                return heuristicaPrecise;
                //return 0d;
            }
        };

        //Tiempo total del cálculo del algoritmo
        long total_time = 0;
        //Total de memoria consumida por el algoritmo
        double total_memoria = 0;

        //Iteramos sobre el problema de búsqueda
        //Si queremos explorar una traza en concreto debemos avanzar llamando a miReader.avanzarPos();
        timerTotal.start();

        //miReader.setPos(114);
        for (int i = 0; i < miReader.getTraces().size(); i++) {
            initialState.getMarcado().restartMarking();
            initialState.restartState();

            //Definimos el problema de búsqueda
            SearchProblem<StateMove, NStateLarge.StateLarge, WeightedNode<StateMove, NStateLarge.StateLarge, Double>> p
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

            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            miReader.getTrazaActual().clear();

            if (print) {
                LOGGER.log(Level.INFO, "Traza nº " + i + " -> " + miReader.getTrazaActual().toString());
            }

            AStar<StateMove, NStateLarge.StateLarge, Double, WeightedNode<StateMove, NStateLarge.StateLarge, Double>> astar = Hipster.createAStar(p);
            AStar.Iterator it = astar.iterator();

            while (it.hasNext()) {
                WeightedNode n1 = (WeightedNode) it.next();
                NStateLarge.StateLarge s = (NStateLarge.StateLarge) n1.state();

                double score = (double) n1.getScore();

                if (print) {
                    String sa = "";
                    //sa = sa + "\n---------MARCADO--------------";
                    //sa = sa + "\n" + s.getMarcado().toString();
                    sa = sa + "\nTareas que se pueden ejecutar: " + s.getMarcado().getEnabledElements();
                    sa = sa + "\nEstimación estado seleccionado: " + n1.getEstimation();
                    sa = sa + "\nScore estado seleccionado: " + score;
                    sa = sa + "\n-----------------------";
                    LOGGER.log(Level.FINEST, sa);
                }

                //Final del modelo y final de la traza (para hacer skips y inserts al final)
                if (parar) {
                    //System.out.println("------------------SIGO------------------");
                    //System.out.println("ESTIMACION " + estimacion + " MEJOR SCORE " + mejorScore);
                    if (score > mejorScore) {
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
                //Para modelos que no tiene tarea final
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
            if (parar == false) {
                System.err.print("Error. No se encontró ningún aligment para la traza nº " + i);
                System.exit(2);
            }
            time_end = System.currentTimeMillis();
            total_time = total_time + (time_end - time_start);
            total_memoria = total_memoria + miReader.getTrazaActual().getMemoriaC();
            //Guardamos el coste obtenido en el alineamiento
            double j = 0d;
            Iterator it2 = n.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            WeightedNode node2 = (WeightedNode) it2.next();
            double estimationInicial = (double) node2.getEstimation();
            NStateLarge.StateLarge s2 = (NStateLarge.StateLarge) node2.state();
            //miReader.getTrazaActual().anadirTareasActivas(s2.getMarcado().getEnabledElements().size());
            miReader.getTrazaActual().anadirTareasActivas(s2.getPossibleEnabledTasks().size());
            //Contador donde se almacena el "menor camino" (para fitness)
            int aux = 0;
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NStateLarge.StateLarge s = (NStateLarge.StateLarge) node.state();
                if (node.action().equals(SINCRONO)) {
                    j++;
                }
                if (node.action().equals(SINCRONO) || node.action().equals(TRAZA)) {
                    aux++;
                }
                //Almacenamos el nº de tareas activas en el modelo durante el alineamiento por cada tarea de la traza (necesario en precission)
                //miReader.getTrazaActual().anadirTareasActivas(s.getMarcado().getEnabledElements().size());
                //System.out.println("Traza " + i + " Tarea "+ j + " " + s.getMarcado().getEnabledElements().size());
                miReader.getTrazaActual().anadirTareasActivas(s.getPossibleEnabledTasks().size());
            }
            e.menorCamino(aux);
            double sobrante = parametrosImpl.getC_SINCRONO() * j;
            double nuevoScore = mejorScore - sobrante;
            double nuevoScoreR = Math.rint(nuevoScore * 100000) / 100000;

            if (estimationInicial > (mejorScore + 0.001)) {
                System.err.print("Error en la estimación inicial (es más alta que el coste obtenido por el alineamiento) en la traza nº " + i);
                System.exit(1);
            }
            miReader.getTrazaActual().setScore(nuevoScoreR);
            //Guardamos el tiempo de cálculo del alineamiento
            miReader.getTrazaActual().setTiempoC(time_end - time_start);
            if (print) {
                //System.out.println(salida.ActualizarTrazas(miReader.getTrazaActual(), n, true, miReader.getInd()));
                //System.out.println(n.path().toString());
                LOGGER.log(Level.INFO, salida.ActualizarTrazasOld(miReader.getTrazaActual(), n, true, miReader.getInd()));
                LOGGER.log(Level.INFO, n.path().toString());
            }

            if (print) {
                //System.out.println(e.getStatMovs());
                LOGGER.log(Level.INFO, e.getStatMovs());
            }
            e.resetMovs();

            //Pasamos a la siguientes traza del procesado
            miReader.avanzarPos();
        }

        timerTotal.stop();
        //Calculamos el Conformance Checking del modelo
        double fitnessNuevo = e.fitnessNuevo(miReader.getTraces());
        double precission = e.precission(miReader.getTraces());
        IndividualFitness individualFitness = new IndividualFitness(miReader.getInd().getNumOfTasks());
        individualFitness.setCompleteness(fitnessNuevo);
        individualFitness.setPreciseness(precission);
        miReader.getInd().setFitness(individualFitness);

        e.setMemoriaConsumida(total_memoria);

        String s = salida.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time, e.getMemoriaConsumida());
        LOGGER.log(Level.INFO, s);
        LOGGER.log(Level.INFO, "\n " +
                "Tiempo cálculo función heurística : " + timer.getReadableElapsedTime() + "\n " +
                "Tiempo cálculo movimientos : " + timerMovs.getReadableElapsedTime() + "\n " +
                "Tiempo aplicar movimientos : " + timerAct.getReadableElapsedTime() + "\n " +
                "\n " +
                "Tiempo inicializar marcado : " + timerInicializarMarcado.getReadableElapsedTime() + "\n " +
                "Tiempo clonar tokens : " + timerClonarTokens.getReadableElapsedTime() + "\n " +
                "Tiempo clonar posibles activas : " + timerClonarPosiblesActivas.getReadableElapsedTime() + "\n " +
                "\n " +
                "Tiempo cálculo total : " + timerTotal.getReadableElapsedTime() +
                "\n " +
                "Nº Instancias marcado : " + contadorInstanciasMarcado);

        if (print) {
            LOGGER.log(Level.INFO, "\nMovimientos ejecutados" + salida.getStatMovs());
        }

        if (print) {
            String statMovsTotal = e.getAllStatMovs();
            LOGGER.log(Level.INFO, "\nMovimientos totales" + statMovsTotal);
        }
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(NStateLarge.StateLarge state, InterfazTraza trace, EjecTareas ejec, CMIndividual individual) {
        timerMovs.resume();
        //TODO Importante: se actualiza el marcado del estado
        if (state.getMov() != null) {
            applyMovs(state, individual);
        }
        //boolean anadirForzadas = false;
        //boolean anadirForzadasTraza = false;
        //Creamos una lista con los movimientos posibles
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        //Limpiamos la variables de la clase auxiliar
        ejec.clear();
        //Leemos la tarea actual de la traza
        Integer e = trace.leerTarea(state.getPos());

        if (print) {
            String salida = "";
            salida = salida + "\n-----------------------";
            salida = salida + "\nMovimiento efectuado : " + state.getMov();
            salida = salida + "\nTarea sobre la que se hizo el movimiento : " + state.getTarea();

            salida = salida + "\nPos de la traza (lo contiene el estado) : " + state.getPos();
            salida = salida + "\nTarea de la traza : " + e;
            //System.out.println("Marcado en la seleccion de movimientos " + state.getMarcado().toString());
            salida = salida + "\n-----------------------";
            LOGGER.log(Level.FINEST, salida);
        }

        //Si HAY tareas activas en el modelo
        if (state.Enabled()) {
            //La tarea de la traza ya habia sido ejecutada en el modelo o la acabamos de procesar
            /*if (e == null) {
                anadirForzadas = true;
            } else if (state.isEjecutedTask(e)) {
                anadirForzadasTraza = true;
            }*/

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
            //anadirForzadas = true;
            //anadirForzadasTraza = true;
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
        /*if (anadirForzadas) {
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
        }*/

        if (print) {
            LOGGER.log(Level.FINE, "Posible movimientos del estado : " + movements);
        }
        trace.addMemoriaC(movements.size());
        //Devolvemos una coleccion con los posibles movimientos
        timerMovs.pause();
        return movements;
    }

    //Realizamos la acción correspondiente en función del movimiento
    private static NStateLarge.StateLarge applyActionToState(StateMove action, NStateLarge.StateLarge state, EjecTareas ejec, CMIndividual m, InterfazEstadisticas stats) {
        timerAct.resume();
        NStateLarge.StateLarge successor = new NStateLarge.StateLarge(state);

        //Count all movs
        stats.countTypeMovs(action);

        switch (action) {
            case SINCRONO:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarTarea();
                successor.setMov(SINCRONO);
                successor.setTarea(ejec.getTareaSINCRONA());
                break;
            case MODELO:
                //Avanzamos el modelo con una tarea que no tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaModelo();
                successor.setTarea(t);
                successor.setMov(MODELO);
                break;
            case TRAZA:
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(TRAZA);
                successor.setTarea(ejec.getTareaTRAZA());
                break;
            case MODELO_FORZADO:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                t = ejec.leerTareaModeloForzado();
                successor.setTarea(t);
                successor.setMov(MODELO_FORZADO);
                break;
        }

        timerAct.pause();
        return successor;
    }

    //La función de coste depende del movimiento ejecutado
    private static Double evaluateToState(Transition<StateMove, NStateLarge.StateLarge> transition, ParametrosImpl parametrosImpl, EjecTareas ejec) {
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
                cost = parametrosImpl.getC_MODELO_FORZADO() + ejec.tokenUsados(transition.getState().getTarea()) + PENALIZACION_FORZADO;
                break;
        }
        return cost;
    }

    private static void applyMovs(NStateLarge.StateLarge state, CMIndividual individual) {
        contadorInstanciasMarcado++;

        //Recuperamos los datos copiados para el marcado (tenemos que clonarlos para evitar "aliasing" con otros estados)
        timerInicializarMarcado.resume();
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN2 = cloneTokens(state.getTokens());
        state.setTokens(tokensN2);
        state.getMarcado().setTokens(tokensN2);
        TIntHashSet possibleEnabledTasksClone2 = new TIntHashSet(state.getPossibleEnabledTasks());
        state.setPossibleEnabledTasks(possibleEnabledTasksClone2);
        state.getMarcado().setPossibleEnabledTasks(possibleEnabledTasksClone2);
        timerInicializarMarcado.pause();
        /*CMMarking marking = new CMMarking(individual, new Random(666));
        marking.setEndPlace(state.getMarcado().getEndPlace());
        marking.setNumOfTokens(state.getMarcado().getNumberTokens());
        marking.setStartPlace(state.getMarcado().getStartPlace());
        timerInicializarMarcado.pause();
        timerClonarTokens.resume();
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN = cloneTokens(state.getMarcado().getTokens());
        marking.setTokens(tokensN);
        timerClonarTokens.pause();
        timerClonarPosiblesActivas.resume();
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        marking.setPossibleEnabledTasks(possibleEnabledTasksClone);
        marking.getEnabledElements();
        timerClonarPosiblesActivas.pause();

        state.setMarcado(marking);*/

        switch (state.getMov()) {
            case SINCRONO:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                state.avanzarMarcado(state.getTarea());
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento SINCRONO ----------------> " + state.getTarea());
                }
                break;
            case MODELO:
                //Avanzamos el modelo con una tarea que no tenemos en la traza en la posición actual
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento MODELO ----------------> " + state.getTarea());
                }
                state.avanzarMarcado(state.getTarea());
                break;
            case TRAZA:
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento TRAZA ----------------> " + state.getTarea());
                }
                break;
            case MODELO_FORZADO:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento MODELO_FORZADO ----------------> " + state.getTarea());
                }
                state.avanzarMarcado(state.getTarea());
                break;
        }
    }

    public static ArrayList<HashMap<TIntHashSet, Integer>> cloneTokens(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
        ArrayList<HashMap<TIntHashSet, Integer>> clone = new ArrayList<>();

        for (HashMap<TIntHashSet, Integer> token : tokens) {
            HashMap<TIntHashSet, Integer> tokenClone = new HashMap<>();
            for (TIntHashSet tokenKey : token.keySet()) {
                TIntHashSet tokenKeyClone = new TIntHashSet();
                tokenKeyClone.addAll(tokenKey);
                tokenClone.put(tokenKeyClone, token.get(tokenKey));
            }
            clone.add(tokenClone);
        }

        return clone;
    }
}
