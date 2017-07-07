package Problem;

import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;

/**
 *
 * @author marti
 */
public interface InterfazTraza {

    //Función que permite añadir una tarea concreta a una traza
    void anadirTarea(int t);

    //Función que dada una posición recupera la tarea correspondiente de la traza
    Integer leerTarea(int pos);

    //Función queda dada una posición devuelve la heurística de la traza
    Double getHeuristica(int pos, CMIndividual m, Integer lastEjecuted);

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
}
