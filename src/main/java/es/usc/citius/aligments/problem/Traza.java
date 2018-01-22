package es.usc.citius.aligments.problem;

import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.aligments.config.ParametrosImpl;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

import static es.usc.citius.aligments.problem.NState.StateMove.SINCRONO;

/**
 *
 * @author marti
 */
public class Traza implements InterfazTraza {

    private String id;
    private ArrayList<Integer> tareas;
    private double score;
    private int numRepeticiones;
    private double tiempoC;
    private double memoriaC;

    private int tareaFinalModelo;
    //Último índice de la lista de caminos procesada
    private int j;

    //Mapa con las tareas y el nº de tareas necesarias para alcanzar el final
    private Map<Integer, Integer> toFin;
    //Mapa con las tareas y el nº de tareas necesarias para alcanzar el final
    private Map<Integer, List<List<Integer>>> caminosToFin;
    //Array con el número de tareas activas en el modelo en cada una de las tareas de la traza
    private ArrayList<Integer> tareasModeloActivas;
    //Hash con la tarea y el coste estimado con TR
    private HashMap<Integer, Double> estimacionTR;

    public Traza() {
        this.tareas = new ArrayList<>();
        this.numRepeticiones = 1;
        memoriaC = 0;
        tareaFinalModelo = -1;
        toFin = new HashMap();
        caminosToFin = new HashMap<>();
        tareasModeloActivas = new ArrayList<>();
        estimacionTR = new HashMap<>();
    }

    public ArrayList<Integer> getTareasModeloActivas() {
        return tareasModeloActivas;
    }

    public ArrayList<Integer> getTareas() {
        return tareas;
    }

    public void setTareas(ArrayList<Integer> tareas) {
        this.tareas = tareas;
    }

    @Override
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int getNumRepeticiones() {
        return numRepeticiones;
    }

    @Override
    public void setNumRepeticiones(int numRepeticiones) {
        this.numRepeticiones = numRepeticiones;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public int tamTrace() {
        return tareas.size();
    }

    @Override
    public void anadirTarea(int t) {
        tareas.add(t);
    }

    @Override
    public Integer leerTarea(int p) {
        if (p < tareas.size()) {
            return tareas.get(p);
        } else {
            return null;
        }
    }

    @Override
    public Double getHeuristica(int pos, CMIndividual m, Integer lastEjecuted) {
        double tareasFin, r, resultado;

        Integer tarea = leerTarea(pos);
        if (tarea == null) {
            if (lastEjecuted == null) {
                r = tareas.size() - pos;
                return r;
            } else {
                tarea = lastEjecuted;
            }
        }

        //TODO Eliminar. Para checkear que se calcula ben a distancia
//        if (toFin.containsKey(tarea)) {
//            r = toFin.get(tarea);
//            return r;
//        }
//        tareasFin = tareasToFin(tarea, m);
        //TODO Esto deberíase comprobar no CMIndividual
        //Comprobamos que no hemos calculado ya la distancia a la tarea final
        if (toFin.containsKey(tarea)) {
            //Recuperamos la distancia calculada
            tareasFin = toFin.get(tarea);
        } else {
            //Calculamos la nueva distancia
            tareasFin = tareasToFin(tarea, m);
        }

        r = tareas.size() - pos;
        //System.out.println("Tarea " + tarea + " Maximo entre (tareas restantes modelo) " + tareasFin + " y (tareas restantes traza) " + r);
        resultado = Math.max(tareasFin, r);
        return resultado;
    }

    @Override
    public Double getHeuristicaPrecise(int pos, CMIndividual m, Integer lastEjecuted) {
        double r;
        List<List<Integer>> caminosFin;

        Integer tarea = leerTarea(pos);
        if (tarea == null) {
            if (lastEjecuted == null) {
                r = tareas.size() - pos;
                return r;
            } else {
                tarea = lastEjecuted;
            }
        }

        //Comprobamos que no hemos calculado ya la distancia a la tarea final
        if (caminosToFin.containsKey(tarea)) {
            //Recuperamos la distancia calculada
            caminosFin = caminosToFin.get(tarea);
        } else {
            //Calculamos la nueva distancia
            caminosFin = tareasToFinPrecise(tarea, m);
        }

        //Recuperamos las tareas no procesadas en la traza
        ArrayList<Integer> tareasNoProcesadas = new ArrayList<>();
        for (int i=pos; i <= tareas.size() - 1; i++) {
            tareasNoProcesadas.add(tareas.get(i));
        }

        if (caminosFin != null && caminosFin.size() > 0) {
            //Primer tamaño del camino
            int size = caminosFin.get(0).size();
            double newCost;
            double newCostS;

            j = 0;
            newCost = newHeuristic(caminosFin, size, tareasNoProcesadas);
            newCostS = newCost;
            size++;
            int maxSize = caminosFin.get(caminosFin.size() - 1).size();
            while (size <= maxSize) {
                if (newCostS < newCost) {
                    newCost = newCostS;
                }
                newCostS = newHeuristic(caminosFin, size, tareasNoProcesadas);
                size++;
            }
            return newCost;
        } else {
            return 0d;
        }
    }

    @Override
    public Double getHeuristicaPrecise2(int pos, CMIndividual m, Integer lastEjecuted) {
        double r;
        double costeCaminosFin;

        Integer tarea = leerTarea(pos);
        if (tarea == null) {
            if (lastEjecuted == null) {
                r = tareas.size() - pos;
                return r;
            } else {
                tarea = lastEjecuted;
            }
        }

        //Recuperamos las tareas no procesadas en la traza
        ArrayList<Integer> tareasNoProcesadas = new ArrayList<>();
        for (int i=pos; i <= tareas.size() - 1; i++) {
            tareasNoProcesadas.add(tareas.get(i));
        }

        //Calculamos la nueva distancia
        costeCaminosFin = tareasToFinPrecise2(tarea, m, tareasNoProcesadas);

        return costeCaminosFin;
    }

    @Override
    public Double getHeuristicaTokenReplay(int pos, CMIndividual m, CMMarking oldMarking, Integer lastEjecuted, NState.State state) {
        //Si ya calculamos la heurística en un estado anterior
        if (state.getMov() != null && state.getHeuristica() != Double.MAX_VALUE) {
            //Recuperamos la heurística calculada
            double newHeuristic = state.getHeuristica();
            switch (state.getMov()) {
                case SINCRONO:
                    newHeuristic = newHeuristic - ParametrosImpl.getC_SINCRONO();
                    break;
                default:
                    newHeuristic = newHeuristic - ParametrosImpl.getC_TRAZA();
                    break;
            }
            //Comprobamos que la heurística no sea negativa
            if (newHeuristic < 0) {
                newHeuristic = 0;
            }
            //Guardamos la nueva heurística en el estado
            state.setHeuristica(newHeuristic);
            return newHeuristic;
        } else {
            //La primera vez entramos aquí
            double v = executeTR(pos, m, oldMarking, state);
            return v;
        }
    }

    private double executeTR(int pos, CMIndividual m, CMMarking oldMarking, NState.State state) {
        //Copiamos el marcado
        CMMarking marking = new CMMarking(m, new Random(666));
        //marking.restartMarking();
        marking.setEndPlace(oldMarking.getEndPlace());
        marking.setNumOfTokens(oldMarking.getNumberTokens());
        marking.setStartPlace(oldMarking.getStartPlace());
        ArrayList<HashMap<TIntHashSet, Integer>> tokensN = (ArrayList<HashMap<TIntHashSet, Integer>>) this.cloneTokens(oldMarking.getTokens());
        marking.setTokens(tokensN);
        TIntHashSet possibleEnabledTasksClone = new TIntHashSet();
        possibleEnabledTasksClone.addAll(oldMarking.getEnabledElements());
        marking.setPossibleEnabledTasks(possibleEnabledTasksClone);

        int modelo = 0;
        int traza = 0;
        int sincronas = 0;
        //Para todas las tareas de la traza (o las restantes)
        for (int i = pos; i < tareas.size(); i++) {
            Integer currentTaskID = tareas.get(i);
            //Ejecutamos la tarea en el modelo, guardando el número de tokens necesarios para su ejecución
            int numOfTokens = marking.execute(currentTaskID);
            //Si son necesarios tokens adicionales
            if (numOfTokens > 0) {
                //Estimamos que es necesario realizar un movimientos en la traza
                traza++;
                //TODO puede no ser necesario almacenar esto
                estimacionTR.put(currentTaskID, ParametrosImpl.getC_TRAZA());
            } else {
                //Si no son necesarios tokens adicionales, es un movimiento síncrono
                sincronas++;
                //TODO puede no ser necesario almacenar esto
                estimacionTR.put(currentTaskID, ParametrosImpl.getC_SINCRONO());
            }
        }
        //Calculamos la nueva heurística en base a los movimientos estimados
        double newHeuristic = calHeuristicCost(sincronas, modelo, traza);
        //Guardamos la heurística en el estado
        state.setHeuristica(newHeuristic);
        return newHeuristic;
    }

    public double newHeuristic(List<List<Integer>> caminosFin, int size, ArrayList<Integer> tareasNoProcesadas) {
        double coste = Double.MAX_VALUE;

        while (true) {
            int tareasSincronas = 0;
            int tareasModelo = 0;
            int tareasTraza = 0;

            if (j >= caminosFin.size() || caminosFin.get(j).size() > size) {
                break;
            } else if (caminosFin.get(j).size() == size) {
                //Calculamos el mínimo coste para las tareas restantes por procesar de la traza y del modelo
                List<Integer> camino = caminosFin.get(j);
                List<Integer> caminoCopy = new ArrayList<>(camino);
                for (int t : tareasNoProcesadas) {
                    if (caminoCopy.contains(t)) {
                        tareasSincronas++;
                        caminoCopy.remove((Object)t);
                    } else {
                        tareasTraza++;
                    }
                }
                //Tareas que faltan por ejecutar en el modelo y no estan en la traza
                tareasModelo = caminoCopy.size();
                double newCost = calHeuristicCost(tareasSincronas, tareasModelo, tareasTraza);
                if (newCost < coste) {
                    coste = newCost;
                }
            }
            j++;
        }

        return coste;
    }

    public double heuristicaCamino(List<Integer> camino, ArrayList<Integer> tareasNoProcesadas) {
        double coste = Double.MAX_VALUE;
        int tareasSincronas = 0;
        int tareasModelo;
        int tareasTraza = 0;

        List<Integer> caminoCopy = new ArrayList<>(camino);
        for (int t : tareasNoProcesadas) {
            if (caminoCopy.contains(t)) {
                tareasSincronas++;
                caminoCopy.remove((Object) t);
            } else {
                tareasTraza++;
            }
        }
        //Tareas que faltan por ejecutar en el modelo y no estan en la traza
        tareasModelo = caminoCopy.size();
        double newCost = calHeuristicCost(tareasSincronas, tareasModelo, tareasTraza);
        if (newCost < coste) {
            coste = newCost;
        }

        return coste;
    }

    public double calHeuristicCost(int tareasSincronas, int tareasModelo, int tareasTraza) {
        double r = tareasSincronas * ParametrosImpl.getC_SINCRONO() + tareasModelo * ParametrosImpl.getC_MODELO()
                + tareasTraza * ParametrosImpl.getC_TRAZA();
        return r;
    }

    //Ya acabé la traza. La última posicion quedaría fuera. 
    @Override
    public boolean procesadoTraza(int pos) {
        return pos >= tareas.size();
    }

    @Override
    public void print() {
        System.out.println();
        for (int i = 0; i < tareas.size(); i++) {
            System.out.print(tareas.get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < tareas.size(); i++) {
            s = s + tareas.get(i) + " ";
        }
        return s;
    }

    @Override
    public double getTiempoC() {
        return tiempoC;
    }

    @Override
    public void setTiempoC(double tiempoC) {
        this.tiempoC = tiempoC;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public double getScoreRepetido() {
        return this.score * this.numRepeticiones;
    }

    @Override
    public double getMemoriaC() {
        return memoriaC;
    }

    @Override
    public void anadirTareasActivas(int n) {
        tareasModeloActivas.add(n);
    }

    @Override
    public void addMemoriaC(double c) {
        memoriaC = memoriaC + c;
    }

    @Override
    public void clear() {
        score = 0;
        tiempoC = 0;
        memoriaC = 0;
    }

    /*Función que dada una tarea nos indica el número mínimo de tareas necesarias
    para llegar a la tarea final
    TODO esto podémolo poñer dentro do individuo en ProDiGen, de maneira que se
    o calculamos para unha traza xa o podemos reutilizar para todas
     */
    private double tareasToFin(Integer task, CMIndividual m) {

        //Obtenemos la tarea final del modelo si no la tenemos almacenada ya
        if (this.tareaFinalModelo == -1) {
            TIntIterator it = m.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                //En caso de que no podamos devolver la tarea final devolvemos un error
                return -1;
            }
            //Si la tarea ya es la final devolvemos 0
        } else if (task == tareaFinalModelo) {
            toFin.put(task, 0);
            return 0;
        }

        //Creamos una lista con listas de todas las salidas de una tarea hasta
        //llegar al final
        List<List<Integer>> tareasRestantes = new ArrayList();
        //Lista con las tareas ya añadidas
        TIntHashSet anadidas = new TIntHashSet();
        //Lista con la tarea inicial de la que queremos saber el camino hasta el final
        List<Integer> primera = new ArrayList();
        primera.add(task);
        tareasRestantes.add(primera);

        int lista = 0;
        label1:
        while (lista < tareasRestantes.size()) {
            ArrayList<Integer> nuevasTareas = new ArrayList();
            for (int i = 0; i < tareasRestantes.get(lista).size(); i++) {
                Iterator<TIntHashSet> set = m.getTask(tareasRestantes.get(lista).get(i)).getOutputs().iterator();
                while (set.hasNext()) {
                    TIntIterator it = set.next().iterator();
                    while (it.hasNext()) {
                        int tarea = it.next();
                        if (!anadidas.contains(tarea)) {
                            if (tarea == tareaFinalModelo) {
                                tareasRestantes.add(nuevasTareas);
                                break label1;
                            } else {
                                anadidas.add(tarea);
                                nuevasTareas.add(tarea);
                            }
                        }
                    }
                }
            }
            if (nuevasTareas.size() > 0) {
                tareasRestantes.add(nuevasTareas);
            } else {
                break;
            }
            lista++;
        }

        /*Guardamos en el mapa la tarea y el nº de tareas necesarias para alcanzar
        la tarea final */
        Integer tareasToFin = tareasRestantes.size() - 1;
        toFin.put(task, tareasToFin);
        // Le tenemos que restar la lista que contiene la tarea inicial 
        return tareasToFin;
    }

    //Obtenemos el camino con el menor coste (en función de la traza) para alcanzar el final del modelo
    private List<List<Integer>> tareasToFinPrecise(Integer task, CMIndividual m) {

        //Obtenemos la tarea final del modelo si no la tenemos almacenada ya
        if (this.tareaFinalModelo == -1) {
            TIntIterator it = m.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                //En caso de que no podamos devolver la tarea final devolvemos un error
                return null;
            }
            //Si la tarea ya es la final devolvemos 0
        } else if (task == tareaFinalModelo) {
            //Devolvemos el coste de procesar las tareas restantes de la traza
            return new ArrayList<>();
        }

        List<List<Integer>> caminos = new ArrayList();
        //Set con la tarea inicial de la que queremos saber el camino hasta el final
        List<Integer> primera = new ArrayList<>();
        primera.add(task);
        caminos.add(primera);
        List<List<Integer>> nuevosCaminos = new ArrayList();
        List<List<Integer>> caminosFinalizados = new ArrayList();

        while (caminos.size() > 0) {
            for (int i = 0; i < caminos.size(); i++) {
                List<Integer> camino1 = caminos.get(i);
                //Obtenemos la última tarea del camino
                Integer integer = camino1.get(camino1.size() - 1);
                //Obtenemos sus salidas
                Iterator<TIntHashSet> set = m.getTask(integer).getOutputs().iterator();
                //Lista con las nuevas tareas que le añadimos a los caminos
                List<Integer> nuevasTareas = new ArrayList<>();
                while (set.hasNext()) {
                    TIntIterator it = set.next().iterator();
                    //Creamos un nuevo camino por cada tarea de salida
                    while (it.hasNext()) {
                        int tarea = it.next();
                        List<Integer> nuevoCamino = new ArrayList<>(camino1);
                        //Si el camino no contiene la nueva tarea
                        if (!nuevoCamino.contains(tarea)) {
                            nuevoCamino.add(tarea);
                            nuevasTareas.add(tarea);
                            if (tarea == tareaFinalModelo) {
                                caminosFinalizados.add(nuevoCamino);
                            } else {
                                nuevosCaminos.add(nuevoCamino);
                            }
                        }
                        //Si ya contiene esa tarea descartamos el camino (ya se va a explorar)
                    }
                    //Combinamos los posibles caminos (importa el orden!)
                }
            }
            //Limpiamos los caminos viejos
            caminos.clear();
            caminos = new ArrayList<>(nuevosCaminos);
            nuevosCaminos.clear();
        }

        /*Guardamos en el mapa la tarea y los caminos para alcanzar
        la tarea final */
        caminosToFin.put(task, caminosFinalizados);

        //Devolvemos todos los posibles caminos hasta la tarea final
        return caminosFinalizados;
    }

    //Obtenemos el camino con el menor coste (en función de la traza) para alcanzar el final del modelo
    private double tareasToFinPrecise2(Integer task, CMIndividual m, ArrayList<Integer> tareasNoProcesadas) {

        //Obtenemos la tarea final del modelo si no la tenemos almacenada ya
        if (this.tareaFinalModelo == -1) {
            TIntIterator it = m.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                //En caso de que no podamos devolver la tarea final devolvemos un error
                return 0d;
            }
            //Si la tarea ya es la final devolvemos 0
        } else if (task == tareaFinalModelo) {
            //Devolvemos el coste de procesar las tareas restantes de la traza
            return heuristicaCamino(new ArrayList<>(), tareasNoProcesadas);
        }

        List<List<Integer>> caminos = new ArrayList();
        //Set con la tarea inicial de la que queremos saber el camino hasta el final
        List<Integer> primera = new ArrayList<>();
        primera.add(task);
        caminos.add(primera);
        List<List<Integer>> nuevosCaminos = new ArrayList();
        List<List<Integer>> caminosFinalizados = new ArrayList();
        double costeMenorCamino = Double.MAX_VALUE;

        while (caminos.size() > 0) {
            for (int i = 0; i < caminos.size(); i++) {
                List<Integer> camino1 = caminos.get(i);
                //Obtenemos la última tarea del camino
                Integer integer = camino1.get(camino1.size() - 1);
                //Obtenemos sus salidas
                Iterator<TIntHashSet> set = m.getTask(integer).getOutputs().iterator();
                while (set.hasNext()) {
                    TIntIterator it = set.next().iterator();
                    //Creamos un nuevo camino por cada tarea de salida
                    while (it.hasNext()) {
                        int tarea = it.next();
                        List<Integer> nuevoCamino = new ArrayList<>(camino1);
                        //Si el camino no contiene la nueva tarea
                        if (!nuevoCamino.contains(tarea)) {
                            nuevoCamino.add(tarea);
                            double costeNuevoFin = heuristicaCamino(nuevoCamino, tareasNoProcesadas);
                            if (tarea == tareaFinalModelo) {
                                caminosFinalizados.add(nuevoCamino);
                                if (costeNuevoFin < costeMenorCamino) {
                                    costeMenorCamino = costeNuevoFin;
                                }
                            } else {
                                //Solo añadimos el nuevo camino si su coste es menor que el menor actual
                                if (costeNuevoFin <= costeMenorCamino) {
                                    nuevosCaminos.add(nuevoCamino);
                                }
                            }
                        }
                        //Si ya contiene esa tarea descartamos el camino (ya se va a explorar)
                    }
                }
            }
            //Limpiamos los caminos viejos
            caminos.clear();
            caminos = new ArrayList<>(nuevosCaminos);
            nuevosCaminos.clear();
        }

        /*Guardamos en el mapa la tarea y los caminos para alcanzar
        la tarea final */
        //caminosToFin.put(task, caminosFinalizados);

        //Devolvemos todos los posibles caminos hasta la tarea final
        return costeMenorCamino;
    }

    private ArrayList<HashMap<TIntHashSet, Integer>> cloneTokens(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
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
