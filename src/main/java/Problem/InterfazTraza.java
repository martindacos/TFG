package Problem;

import domainLogic.workflow.Task.Task;

/**
 *
 * @author marti
 */
public interface InterfazTraza {
    
    //Función que permite añadir una tarea concreta a una traza
    public void anadirTarea(Task a);
    //Función que dada una posición recupera la tarea correspondiente de la traza
    public Task leerTarea(int pos);
    //Función queda dada una posición devuelve la heurística de la traza
    public Double getHeuristica(int pos);
    //Función que dada una posición indica si la traza fue procesada
    public boolean procesadoTraza(int pos);
}
