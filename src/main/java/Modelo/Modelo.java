package Modelo;

import CMTask.CMTask;
import java.util.HashMap;

/**
 *
 * @author Martin
 */
public class Modelo {
    //Posición provisional de las tareas del modelo
    //La ponemos en la traza
    //int posM;
    //Almacenamos las tareas y el id de la matriz de la tarea principal
    HashMap<Integer, CMTask> tareas;

    public Modelo() {
        tareas = new HashMap<>();
        //posM = 0;
    }

//    public int getPosM() {
//        return posM;
//    }

    public void put(int id, CMTask a) {
        tareas.put(id, a);
    }
    
//    public void aPosM(){
//        posM++;
//    }
    
    public CMTask getCM(int posM){
        return tareas.get(posM);
    }
    
    public CMTask getCMAnterior(int posM) {
        //System.out.println("posM " +posM);
        return tareas.get(0);
    }
}
