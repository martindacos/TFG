package Modelo;

import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

/**
 *
 * @author Martin
 */
public class Modelo {
    private CMIndividual ind;
    private static Modelo miModelo;

    public static Modelo getModelo() {
        if (miModelo == null) {
            miModelo = new Modelo();
        }
        return miModelo;
    }
    
    public static Modelo getModelo(CMIndividual ind) {
        if (miModelo == null) {
            miModelo = new Modelo(ind);
        }
        return miModelo;
    }
        
    private Modelo() {
        //Modelo fijo de prueba
        ind = new CMIndividual(5);
        
        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task D = new Task("D");
        CMTask cmD = new CMTask(D);

        Task B = new Task("B");
        CMTask cmB = new CMTask(B);
        Task C = new Task("C");
        CMTask cmC = new CMTask(C);

        Task E = new Task("E");
        E.setType(1);
        CMTask cmE = new CMTask(E);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(D.getMatrixID());
        //subset.add(B.getMatrixID());
        set.add(subset);
        //Creamos y añadimos el segundo subset
        subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        subset.add(C.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de outputs
        CMSet set2 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset2 = new TIntHashSet();
        subset2.add(E.getMatrixID());
        set2.add(subset2);

        //Asignamos outputs
        cmD.setOutputs(set2);
        cmB.setOutputs(set2);
        cmC.setOutputs(set2);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        set3.add(subset3);

        cmD.setInputs(set3);
        cmB.setInputs(set3);
        cmC.setInputs(set3);

        //Creamos el set de inputs
        CMSet set4 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset4 = new TIntHashSet();
        subset4.add(D.getMatrixID());
        //subset4.add(B.getMatrixID());
        set4.add(subset4);
        //Creamos y añadimos el segundo subset
        subset4 = new TIntHashSet();
        subset4.add(B.getMatrixID());
        subset4.add(C.getMatrixID());
        set4.add(subset4);

        //Asignamos inputs
        cmE.setInputs(set4);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmD);
        tasks.put(2, cmB);
        tasks.put(3, cmC);
        tasks.put(4, cmE);

        ind.setTasks(tasks);
    }
    
    private Modelo(CMIndividual ind) {
        this.ind = ind;
    }

    public CMIndividual getInd() {
        return ind;
    }
}
