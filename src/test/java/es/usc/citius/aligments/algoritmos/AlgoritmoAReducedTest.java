package es.usc.citius.aligments.algoritmos;

import es.usc.citius.aligments.config.Parametros;
import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.estadisticas.InterfazEstadisticas;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.problem.Traza;
import es.usc.citius.prodigen.domainLogic.workflow.Task.Task;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class AlgoritmoAReducedTest {

    @Before
    public void resetReader() {
        Readers.reset();
        Task.restartcount();
        AlgoritmoAReduced.reset();
    }

    //Test que verifican la correcta creación de todos los estados. Necesario eliminar break del bucle del algoritmo y
    // utilizar una h() igual a 0.
    @Test
    public void problem() throws Exception {
        //Modelo secuencial A y B
        //Simple secuencial Individual
        CMIndividual ind = new CMIndividual(2);

        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task B = new Task("B");
        B.setType(1);
        CMTask cmB = new CMTask(B);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        set3.add(subset3);

        cmB.setInputs(set3);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmB);

        ind.setTasks(tasks);
        ind.print();

        ArrayList<InterfazTraza> traces = new ArrayList<>();
        Traza t = new Traza();
        ArrayList<Integer> tareas = new ArrayList<>();
        tareas.add(0);
        tareas.add(1);
        t.setTareas(tareas);

        traces.add(t);

        Readers miReader = Readers.getReader(traces, ind);
        InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, true);

        Assert.assertEquals(0d, problem.getCoste(), 0);
        Assert.assertEquals(1d, problem.getPrecission(), 0);
        Assert.assertEquals(9, problem.getDiferentStates(), 0);
    }

    @Test
    public void problem2() throws Exception {
        //Modelo secuencial A, B y C
        //Simple secuencial Individual
        CMIndividual ind = new CMIndividual(3);

        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task B = new Task("B");
        CMTask cmB = new CMTask(B);

        Task C = new Task("C");
        C.setType(1);
        CMTask cmC = new CMTask(C);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de outputs
        set = new CMSet();
        //Creamos y añadimos el primer subset
        subset = new TIntHashSet();
        subset.add(C.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmB.setOutputs(set);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        set3.add(subset3);

        cmB.setInputs(set3);

        //Creamos el set de inputs
        set3 = new CMSet();
        //Creamos y añadimos el primer subset
        subset3 = new TIntHashSet();
        subset3.add(B.getMatrixID());
        set3.add(subset3);

        cmC.setInputs(set3);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmB);
        tasks.put(2, cmC);

        ind.setTasks(tasks);
        ind.print();

        ArrayList<InterfazTraza> traces = new ArrayList<>();
        Traza t = new Traza();
        ArrayList<Integer> tareas = new ArrayList<>();
        tareas.add(0);
        tareas.add(1);
        tareas.add(2);
        t.setTareas(tareas);

        traces.add(t);

        Readers miReader = Readers.getReader(traces, ind);
        InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, true);

        Assert.assertEquals(0d, problem.getCoste(), 0);
        Assert.assertEquals(1d, problem.getPrecission(), 0);
        Assert.assertEquals(16, problem.getDiferentStates(), 0);
    }

    @Test
    public void problem3() throws Exception {
        //Modelo secuencial A, B OR C, D
        //Simple secuencial Individual
        CMIndividual ind = new CMIndividual(4);

        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task B = new Task("B");
        CMTask cmB = new CMTask(B);

        Task C = new Task("C");
        CMTask cmC = new CMTask(C);

        Task D = new Task("D");
        D.setType(1);
        CMTask cmD = new CMTask(D);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        subset.add(C.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de outputs
        set = new CMSet();
        //Creamos y añadimos el primer subset
        subset = new TIntHashSet();
        subset.add(D.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmB.setOutputs(set);
        cmC.setOutputs(set);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        set3.add(subset3);

        cmB.setInputs(set3);
        cmC.setInputs(set3);

        //Creamos el set de inputs
        set3 = new CMSet();
        //Creamos y añadimos el primer subset
        subset3 = new TIntHashSet();
        subset3.add(B.getMatrixID());
        subset3.add(C.getMatrixID());
        set3.add(subset3);

        cmD.setInputs(set3);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmB);
        tasks.put(2, cmC);
        tasks.put(3, cmD);

        ind.setTasks(tasks);
        ind.print();

        ArrayList<InterfazTraza> traces = new ArrayList<>();
        Traza t = new Traza();
        ArrayList<Integer> tareas = new ArrayList<>();
        tareas.add(0);
        tareas.add(1);
        tareas.add(3);
        t.setTareas(tareas);

        traces.add(t);

        /*t = new Traza();
        tareas = new ArrayList<>();
        tareas.add(0);
        tareas.add(2);
        tareas.add(3);
        t.setTareas(tareas);

        traces.add(t);*/

        Readers miReader = Readers.getReader(traces, ind);
        InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, true);

        Assert.assertEquals(0d, problem.getCoste(), 0);
        Assert.assertEquals(21, problem.getDiferentStates(), 1);
    }

    @Test
    public void problem4() throws Exception {
        //Modelo secuencial A, B (en bucle) y C
        //Simple secuencial Individual
        CMIndividual ind = new CMIndividual(3);

        Task A = new Task("A");
        A.setType(0);
        CMTask cmA = new CMTask(A);

        Task B = new Task("B");
        CMTask cmB = new CMTask(B);

        Task C = new Task("C");
        C.setType(1);
        CMTask cmC = new CMTask(C);

        //Creamos el set de outputs
        CMSet set = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset = new TIntHashSet();
        subset.add(B.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmA.setOutputs(set);

        //Creamos el set de outputs
        set = new CMSet();
        //Creamos y añadimos el primer subset
        subset = new TIntHashSet();
        subset.add(C.getMatrixID());
        subset.add(B.getMatrixID());
        set.add(subset);

        //Asignamos outputs
        cmB.setOutputs(set);

        //Creamos el set de inputs
        CMSet set3 = new CMSet();
        //Creamos y añadimos el primer subset
        TIntHashSet subset3 = new TIntHashSet();
        subset3.add(A.getMatrixID());
        subset3.add(B.getMatrixID());
        set3.add(subset3);

        cmB.setInputs(set3);

        //Creamos el set de inputs
        set3 = new CMSet();
        //Creamos y añadimos el primer subset
        subset3 = new TIntHashSet();
        subset3.add(B.getMatrixID());
        set3.add(subset3);

        cmC.setInputs(set3);

        TIntObjectHashMap<CMTask> tasks = new TIntObjectHashMap<CMTask>();
        tasks.put(0, cmA);
        tasks.put(1, cmB);
        tasks.put(2, cmC);

        ind.setTasks(tasks);
        ind.print();

        ArrayList<InterfazTraza> traces = new ArrayList<>();
        Traza t = new Traza();
        ArrayList<Integer> tareas = new ArrayList<>();
        tareas.add(0);
        tareas.add(1);
        tareas.add(1);
        tareas.add(2);
        t.setTareas(tareas);

        traces.add(t);

        Readers miReader = Readers.getReader(traces, ind);
        InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, true);

        Assert.assertEquals(0d, problem.getCoste(), 0);
        Assert.assertEquals(25, problem.getDiferentStates(), 0);
    }

    //Test que ejecutan ficheros
    @Test
    public void problem5() throws Exception {
        String PATH = "/home/martin/Documentos/projects/Aligments/TFG/deMedeiros/";

        for (int i=2; i<=10; i++) {
            resetReader();
            Readers miReader = Readers.getReader(PATH + "g" + i + "/grpd_g" + i + "pi300.xes", PATH + "g" + i + "/FHM.hn");
            InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, false);

            resetReader();
            miReader = Readers.getReader(PATH + "g" + i + "/grpd_g" + i + "pi300.xes", PATH + "g" + i + "/FHM.hn");
            ParametrosImpl.setHEURISTIC(Parametros.HEURISTIC_MODEL);
            InterfazEstadisticas problem2 = AlgoritmoAReduced.problem(miReader, false);

            Assert.assertEquals(problem.getCoste(), problem2.getCoste(), 0);
            System.out.println("Modelo G" + i);
            System.out.println("\tTiempo de Cálculo \t Diferent States \t Memoria Consumida");
            System.out.println("\t" + problem.getTiempoCalculo() + " \t" + problem.getDiferentStates() + " \t" + problem.getMemoriaConsumida());
            System.out.println("\t" + problem2.getTiempoCalculo() + " \t" + problem2.getDiferentStates() + " \t" + problem2.getMemoriaConsumida());
        }
    }

    @Test
    public void problem6() throws Exception {
        String PATH = "/home/martin/Documentos/projects/Aligments/TFG/deMedeiros/";
        ParametrosImpl.setHEURISTIC(Parametros.HEURISTIC_MODEL);
        Readers miReader = Readers.getReader(PATH + "g9/grpd_g9pi300.xes", PATH + "g9/FHM.hn");
        InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, true);

        System.out.println();

    }
}