package Problem;

import domainLogic.workflow.Task.Task;
import java.util.ArrayList;

/**
 *
 * @author marti
 */
public class EjecTareas {

    private Task tareaOK;
    private Task tareaINSERT;
    //Posibles tareas a ejecutar en esta instancia de la traza
    private ArrayList<Task> execute;
    private Task skip;

    public EjecTareas() {
    }

    public ArrayList<Task> getExecute() {
        return execute;
    }

    public Task getSkip() {
        return skip;
    }

    public Task getTareaINSERT() {
        return tareaINSERT;
    }

    public Task getTareaOK() {
        return tareaOK;
    }

    public void clear() {
        this.tareaOK = null;
        execute = new ArrayList<Task>();
        this.skip = null;
    }

    public void anadirOk(Task a) {
        Task nueva = new Task(a);
        tareaOK = nueva;
    }

    public void anadirINSERT(Task a) {
        Task nueva = new Task(a);
        tareaINSERT = nueva;
    }
        
    public void anadirExecute(Task a) {
        Task nueva = new Task(a);
        execute.add(nueva);
    }
    
    public Task leerTareaExecute() {
        skip = new Task(execute.get(0));
        System.out.println("leerTareaExecute " + skip.getId() +" "+ skip.getMatrixID());
        execute.remove(0);
        return skip;
    }
}
