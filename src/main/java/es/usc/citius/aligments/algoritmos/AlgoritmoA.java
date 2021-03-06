package es.usc.citius.aligments.algoritmos;

import es.usc.citius.aligments.problem.Readers;

import java.util.logging.Logger;

public class AlgoritmoA {

    private final static Logger LOGGER = Logger.getLogger("aligments");
    private static boolean print;
    private static es.usc.citius.aligments.utils.Timer timerMovs = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerAct = new es.usc.citius.aligments.utils.Timer();

    private static es.usc.citius.aligments.utils.Timer timerInicializarMarcado = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerClonarTokens = new es.usc.citius.aligments.utils.Timer();
    private static es.usc.citius.aligments.utils.Timer timerClonarPosiblesActivas = new es.usc.citius.aligments.utils.Timer();

    public static void problem(Readers miReader, boolean logging) {
        /*es.usc.citius.aligments.utils.Timer timer = new es.usc.citius.aligments.utils.Timer();
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

        final NState.State initialState = new NState.State(miReader.getInd());
        initialState.getMarcado().restartMarking();
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
       /* ActionFunction<StateMove, State> af = new ActionFunction<NState.StateMove, NState.State>() {
            @Override
            public Iterable<NState.StateMove> actionsFor(NState.State state) {
                return AlgoritmoA.validMovementsFor(state, miReader.getTrazaActual(), ejec);
            }
        };

        ActionStateTransitionFunction<StateMove, State> atf;
        atf = new ActionStateTransitionFunction<NState.StateMove, NState.State>() {
            @Override
            public NState.State apply(NState.StateMove action, NState.State state) {
                return AlgoritmoA.applyActionToState(action, state, ejec, miReader.getInd(), e);
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
                timer.resume();
                //Sólo Poñemos a Heurística. Da g() xa se encarga Hipster.
                //Heurística. Número de elementos que faltan por procesar da traza
                Double heuristicaPrecise = miReader.getTrazaActual().getHeuristica(state.getPos(), miReader.getInd(), state.getTarea()) * parametrosImpl.getC_SINCRONO();
                //Double heuristicaPrecise = miReader.getTrazaActual().getHeuristicaTokenReplay(state.getPos(), miReader.getInd(), state.getMarcado(), state.getTarea(), state);
                //Nueva heurística que tiene en cuenta tanto las tareas restantes por procesar del modelo como de la traza
                //TODO Refinar cas combinación dos elementos ou buscar unha nova solución (estima de máis)
                //Double heuristicaPrecise = miReader.getTrazaActual().getHeuristicaPrecise(state.getPos(), miReader.getInd(), state.getTarea());
                timer.pause();
                return heuristicaPrecise;
                //return 0d;
            }
        };

        ArrayList<AbstractNode> nodosSalida = new ArrayList<>();
        //Tiempo total del cálculo del algoritmo
        long total_time = 0;
        //Total de memoria consumida por el algoritmo
        double total_memoria = 0;

        //Iteramos sobre el problema de búsqueda
        //Si queremos explorar una traza en concreto debemos avanzar llamando a miReader.avanzarPos();
        timerTotal.start();
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

            long time_start, time_end;
            //Empezamos a tomar la medida del tiempo
            time_start = System.currentTimeMillis();

            //miReader.avanzarPos();
            //miReader.avanzarPos();
            //miReader.avanzarPos();
            miReader.getTrazaActual().clear();

            if (print) {
                LOGGER.log(Level.INFO, "Traza nº " + i + " -> " + miReader.getTrazaActual().toString());
            }

            AStar<StateMove, State, Double, WeightedNode<StateMove, State, Double>> astar = Hipster.createAStar(p);
            AStar.Iterator it = astar.iterator();

            int count = 0;
            while (it.hasNext()) {
                count++;
                WeightedNode n1 = (WeightedNode) it.next();
                /*if (print) {
                    Map<State, WeightedNode<StateMove, State, Double>> listaAbiertos = it.getOpen();
                    LOGGER.log(Level.FINE, "Iteración " + count + " -> Tamaño lista abiertos: " + listaAbiertos.size());
                    //Debug cuando falla el programa
                    if (count == 106000) {
                        LOGGER.log(Level.INFO, e.getStatMovs(miReader.getTrazaActual().tamTrace()));
                        if (n != null) {
                            LOGGER.log(Level.INFO, "Tenemos un nodo final");
                            String s = salida.ActualizarTrazas(miReader.getTrazaActual(), n, true, miReader.getInd());
                            LOGGER.log(Level.INFO, s);
                        } else {
                            LOGGER.log(Level.INFO, "NO tenemos un nodo final");
                        }
                        LOGGER.log(Level.INFO, "Estimación del nodo candidato :" + (double) n1.getScore());
                        LOGGER.log(Level.INFO, n1.path().toString());
                        System.exit(1);
                    }
                }*/
                /*NState.State s = (NState.State) n1.state();
                double estimacion = (double) n1.getScore();

                if (print) {
                    String sa = "";
                    sa = sa + "\n---------MARCADO--------------";
                    sa = sa + "\n" + s.getMarcado().toString();
                    sa = sa + "\nTareas que se pueden ejecutar: " + s.getMarcado().getEnabledElements();
                    sa = sa + "\nEstimacion coste estado seleccionado: " + estimacion;
                    sa = sa + "\n-----------------------";
                    LOGGER.log(Level.FINEST, sa);
                }

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
            //nodosSalida.add(n);
            //Guardamos el coste obtenido en el alineamiento
            double j = 0d;
            Iterator it2 = n.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            AbstractNode node2 = (AbstractNode) it2.next();
            NState.State s2 = (NState.State) node2.state();
            miReader.getTrazaActual().anadirTareasActivas(s2.getMarcado().getEnabledElements().size());
            //Contador donde se almacena el "menor camino" (para fitness)
            int aux = 0;
            while (it2.hasNext()) {
                WeightedNode node = (WeightedNode) it2.next();
                NState.State s = (NState.State) node.state();
                if (node.action().equals(SINCRONO)) {
                    j++;
                }
                if (node.action().equals(SINCRONO) || node.action().equals(TRAZA)) {
                    aux++;
                }
                //Almacenamos el nº de tareas activas en el modelo durante el alineamiento por cada tarea de la traza (necesario en precission)
                miReader.getTrazaActual().anadirTareasActivas(s.getMarcado().getEnabledElements().size());
            }
            e.menorCamino(aux);
//            System.out.println(mejorScore);
            double sobrante = parametrosImpl.getC_SINCRONO() * j;
            double nuevoScore = mejorScore - sobrante;
            double nuevoScoreR = Math.rint(nuevoScore * 100000) / 100000;

            miReader.getTrazaActual().setScore(nuevoScoreR);
            //Guardamos el tiempo de cálculo del alineamiento
            miReader.getTrazaActual().setTiempoC(time_end - time_start);
            if (print) {
                LOGGER.log(Level.INFO, salida.ActualizarTrazas(miReader.getTrazaActual(), n, true, miReader.getInd()));
                LOGGER.log(Level.INFO, n.path().toString());
            }

            if (print) {
                LOGGER.log(Level.INFO, e.getStatMovs());
            }
            e.resetMovs();

            //Pasamos a la siguientes traza del procesado
            miReader.avanzarPos();
            //System.out.println(i);
        }

        timerTotal.stop();
        //Calculamos el Conformance Checking del modelo
        double fitnessNuevo = e.fitnessNuevo(miReader.getTraces());
        double precission = e.precission(miReader.getTraces());
        IndividualFitness individualFitness = new IndividualFitness();
        individualFitness.setCompleteness(fitnessNuevo);
        individualFitness.setPreciseness(precission);
        miReader.getInd().setFitness(individualFitness);

        e.setMemoriaConsumida(total_memoria);

        String s = salida.estadisticasModelo(miReader.getInd(), e.getCoste(), total_time, e.getMemoriaConsumida());
        LOGGER.log(Level.INFO, s);
        //timer.resume();
        //timer.stop();
        //timerMovs.resume();
        //timerMovs.stop();
        //timerAct.resume();
        //timerAct.stop();
        LOGGER.log(Level.INFO, "\n " +
                "Tiempo cálculo función heurística : " + timer.getReadableElapsedTime() + "\n " +
                "Tiempo cálculo movimientos : " + timerMovs.getReadableElapsedTime() + "\n " +
                "Tiempo aplicar movimientos : " + timerAct.getReadableElapsedTime() + "\n " +
                "\n " +
                "Tiempo inicializar marcado : " + timerInicializarMarcado.getReadableElapsedTime() + "\n " +
                "Tiempo clonar tokens : " + timerClonarTokens.getReadableElapsedTime() + "\n " +
                "Tiempo clonar posibles activas : " + timerClonarPosiblesActivas.getReadableElapsedTime() + "\n " +
                "\n " +
                "Tiempo cálculo total : " + timerTotal.getReadableElapsedTime());

        if (print) {
            LOGGER.log(Level.INFO, "\nMovimientos ejecutados" + salida.getStatMovs());
        }

        if (print) {
            String statMovsTotal = e.getAllStatMovs();
            LOGGER.log(Level.INFO, "\nMovimientos totales" + statMovsTotal);
        }
    }

    //Devolvemos todos los movimientos posibles en función de la traza y el modelo actual
    private static Iterable<StateMove> validMovementsFor(State state, InterfazTraza trace, EjecTareas ejec) {
        timerMovs.resume();
        //boolean anadirForzadas = false;
        //boolean anadirForzadasTraza = false;
        //Creamos una lista con los movimientos posibles
        LinkedList<StateMove> movements = new LinkedList<StateMove>();
        //Limpiamos la variables de la clase auxiliar
        ejec.clear();
        //Leemos la tarea actual de la traza
        Integer e = trace.leerTarea(state.getPos());

        //Almacenamos el marcado en una clase auxiliar para su posterior copia
        ArrayList<HashMap<TIntHashSet, Integer>> tokensA = state.getMarcado().getTokens();
        ejec.setTokens(tokensA);
        ejec.setEndPlace(state.getMarcado().getEndPlace());
        ejec.setNumOfTokens(state.getMarcado().getNumberTokens());
        ejec.setStartPlace(state.getMarcado().getStartPlace());
        //TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        //possibleEnabledTasksClone.addAll(state.getMarcado().getEnabledElements());
        ejec.setPossibleEnabledTasks(state.getMarcado().getEnabledElements());

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
            /*TIntHashSet posiblesTareas = state.getTareas();
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

        /*if (print) {
            LOGGER.log(Level.FINE, "Posible movimientos del estado : " + movements);
        }
        trace.addMemoriaC(movements.size());
        //Devolvemos una coleccion con los posibles movimientos
        timerMovs.pause();
        return movements;
    }

    //Realizamos la acción correspondiente en función del movimiento
    private static State applyActionToState(StateMove action, State state, EjecTareas ejec, CMIndividual m, InterfazEstadisticas stats) {
        timerAct.resume();
        State successor = new State(state);

        //Recuperamos los datos copiados para el marcado (tenemos que clonarlos para evitar "aliasing" con otros estados)
        timerInicializarMarcado.resume();
        CMMarking marking = new CMMarking(m, new Random(666));
        //marking.restartMarking();
        marking.setEndPlace(ejec.getEndPlace());
        marking.setNumOfTokens(ejec.getNumOfTokens());
        marking.setStartPlace(ejec.getStartPlace());
        timerInicializarMarcado.pause();
        timerClonarTokens.resume();
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN = (ArrayList<HashMap<TIntHashSet, Integer>>) ejec.cloneTokens();
        marking.setTokens(tokensN);
        timerClonarTokens.pause();
        timerClonarPosiblesActivas.resume();
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(ejec.getPossibleEnabledTasks());
        marking.setPossibleEnabledTasks(possibleEnabledTasksClone);
        timerClonarPosiblesActivas.pause();

        successor.setMarcado(marking);

        //SIEMPRE necesario recalcular los elementos activos del modelo
        TIntHashSet enabledElements = successor.getMarcado().getEnabledElements();
        if (print) {
            String salida = "";
            salida = salida + "\nMARCADO ANTES";
            salida = salida + "\n" + successor.getMarcado().toString();
            salida = salida + "\nTareas que se pueden ejecutar: " + enabledElements;
            LOGGER.log(Level.FINEST, salida);
        }

        //Count all movs
        stats.countTypeMovs(action);

        switch (action) {
            case SINCRONO:
                //Avanzamos el modelo con la tarea que podemos ejecutar
                successor.avanzarMarcado(ejec.getTareaSINCRONA());
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento SINCRONO ----------------> " + ejec.getTareaSINCRONA());
                }
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(SINCRONO);
                successor.setTarea(ejec.getTareaSINCRONA());
                break;
            case MODELO:
                //Avanzamos el modelo con una tarea que no tenemos en la traza en la posición actual
                Integer t = ejec.leerTareaModelo();
                successor.setTarea(t);
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento MODELO ----------------> " + t);
                }
                successor.avanzarMarcado(t);
                successor.setMov(MODELO);
                break;
            case TRAZA:
                //Avanzamos la traza
                successor.avanzarTarea();
                successor.setMov(TRAZA);
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento TRAZA ----------------> " + ejec.getTareaTRAZA());
                }
                successor.setTarea(ejec.getTareaTRAZA());
                break;
            case MODELO_FORZADO:
                //Avanzamos el modelo con una tarea que tenemos en la traza en la posición actual
                t = ejec.leerTareaModeloForzado();
                successor.setTarea(t);
                if (print) {
                    LOGGER.log(Level.FINEST, "Tarea del movimiento MODELO_FORZADO ----------------> " + t);
                }
                successor.avanzarMarcado(t);
                successor.setMov(MODELO_FORZADO);
                break;
        }
        if (print) {
            String salida = "";
            salida = salida + "\nPos traza " + successor.getPos();
            salida = salida + "\nMARCADO DESPUES";
            salida = salida + "\n" + successor.getMarcado().toString();
            salida = salida + "\nEnabledTasks " + successor.getMarcado().getEnabledElements();
            LOGGER.log(Level.FINEST, salida);
        }

        timerAct.pause();
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
                cost = parametrosImpl.getC_MODELO_FORZADO() + ejec.tokenUsados(transition.getState().getTarea()) + PENALIZACION_FORZADO;
                break;
        }
        return cost;*/
    }
}
