package es.usc.citius.aligments.problem;

import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;

import java.util.ArrayList;

/**
 *
 * @author marti
 */
public interface InterfazTraza {

    //Función que permite añadir una tarea concreta a una traza
    void anadirTarea(int t);

    //Función que dada una posición recupera la tarea correspondiente de la traza
    Integer leerTarea(int pos);

    //Función queda dada una posición, el modelo y la última tarea ejecutada devuelve la heurística de la traza
    Double getHeuristica(int pos, CMIndividual m, Integer lastEjecuted);

    //Función queda dada una posición, el modelo y la última tarea ejecutada devuelve la heurística de la traza
    Double getHeuristicaPrecise(int pos, CMIndividual m, Integer lastEjecuted);

    //Función queda dada una posición, el modelo y la última tarea ejecutada devuelve la heurística de la traza
    Double getHeuristicaCajas(int pos, CMIndividual m, Integer lastEjecuted, Integer trazaMovs, Integer sincroMovs);

    //Función queda dada una posición, el modelo y la última tarea ejecutada devuelve la heurística de la traza
    Double getHeuristicaPrecise2(int pos, CMIndividual m, Integer lastEjecuted);

    //Función queda dada una posición, el modelo y la última tarea ejecutada devuelve la heurística de la traza
    //Double getHeuristicaTokenReplay(int pos, CMIndividual m, CMMarking marking, Integer lastEjecuted, NState.State state);

    //Función que dada una posición indica si la traza fue procesada
    boolean procesadoTraza(int pos);

    double getScore();

    double getScoreRepetido();

    int tamTrace();

    int getNumRepeticiones();

    void setNumRepeticiones(int numRepeticiones);

    void setScore(double mejorScore);

    void print();

    void setTiempoC(double t);

    String getId();

    double getTiempoC();

    //Reseteamos los valores de la traza
    void clear();

    void addMemoriaC(double c);

    double getMemoriaC();

    void anadirTareasActivas(int n);

    ArrayList<Integer> getTareasModeloActivas();
}
