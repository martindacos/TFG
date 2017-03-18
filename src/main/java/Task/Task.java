package Task;

import java.util.Objects;

/**
 * Clase que representa a unha tarefa do Log
 *
 * @author Vane
 * @since 24/02/2011
 */
public final class Task {
    
    
    private static int idCounter = 0;
    
    /* ---------------------- Constants ------------------------------*/
    public static final int INVALID_TASK = -1;
    public static final int INITIAL = 0;
    public static final int FINAL = 1;
    public static final int INITIAL_AND_FINAL = 2;
    public static final int OTHER = 10;

    /* ----------------------- Atributes -----------------------------*/
    /**
     * Identificador único da tarefa
     */
    protected final String id;
    /**
     * Outras propiedades da tarefa
     */
    protected TaskProperties properties;
    /**
     * Tipo de tarefa segunda a súa posicion na secuencia
     */
    private int type = OTHER;
    /**
     * Identificador dentro de la matriz
     */
    protected final int matrixID;

    public static void restartcount() {
        idCounter = 0;
    }

    /* --------------------- Constructors ----------------------------*/
    public Task(String id) {
        this.id = id;
        this.matrixID = idCounter++;
    }
    
    public Task(Task task) {
        this.matrixID = task.matrixID;
        this.id = task.id;
        this.type = task.type;
        this.properties = task.properties;
    }

    /* ------------------ Getters and setters-------------------------*/
    public String getId() {
        return id;
    }
    
    public int getMatrixID() {
        return matrixID;
    }

    public int getType() {
        return type;
    }

    public void setProperties(TaskProperties properties) {
        this.properties = properties;
    }

    public TaskProperties getProperties() {
        return properties;
    }

    public void setType(int type) {
        if ((type == INITIAL && this.type == FINAL) || (this.type == INITIAL && type == FINAL)) {
            this.type = INITIAL_AND_FINAL;
        } else {
            this.type = type;
        }
    }

    public boolean isFinal() {
        return (this.type == FINAL || this.type == INITIAL_AND_FINAL);
    }

    public boolean isInitial() {
        return (this.type == INITIAL || this.type == INITIAL_AND_FINAL);
    }

    /* --------------------- Other methods ---------------------------*/
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Task) {
            final Task otherTask = (Task) arg0;
            return otherTask.id.equals(this.id)
                    && otherTask.matrixID == this.matrixID;
        }
        return super.equals(arg0);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.id);
        hash = 73 * hash + Objects.hashCode(this.matrixID);
        return hash;
    }

    @Override
    public String toString() {
        return id;
        //return super.toString();
    }
}

