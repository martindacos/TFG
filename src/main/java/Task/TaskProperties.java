package Task;

import java.util.HashMap;

/**
 * Outras propiedades de unha tarefa que dependen do log concreto
 * que se estea a tratar.
 * @author Vane
 *
 */
public interface TaskProperties {
	/* ----------------------- Constants -----------------------------*/
	/* ----------------------- Atributes -----------------------------*/
	/* --------------------- Constructors ----------------------------*/
	/* ------------------ Getters and setters-------------------------*/
	/* --------------------- Other methods ---------------------------*/
    
	public HashMap<String, Object> getPropertiesAsHashMap();
}

