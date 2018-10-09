package es.usc.citius.aligments.algoritmos;

import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import es.usc.citius.aligments.config.Parametros;
import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.estadisticas.InterfazEstadisticas;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.problem.Traza;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;
import es.usc.citius.aligments.utils.Timer;
import es.usc.citius.prodigen.domainLogic.exceptions.*;
import es.usc.citius.prodigen.domainLogic.workflow.Task.Task;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.processmining.cobefra.AligmentBasedFitness;
import org.processmining.cobefra.AlignmentBasedPrecision;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

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

        for (int i = 2; i <= 10; i++) {
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

    //Test que comparan la ejecucion con CoBeFra en Medeiros
    @Test
    public void testMedeiros() throws Exception {
        String PATH = "/home/martin/Documentos/projects/Aligments/TFG/deMedeiros/";
        InterfazSalida salida = new SalidaTerminalImpl(false);

        for (int i = 10; i <= 10; i++) {
            resetReader();
            Timer total = new Timer();
            total.start();
            Readers miReader = Readers.getReader(PATH + "g" + i + "/grpd_g" + i + "pi300.xes", PATH + "g" + i + "/FHM.hn");
            //Readers miReader = Readers.getReader(PATH + "Cobefra/Test.xes", PATH + "Cobefra/Test.hn");
            ParametrosImpl.setHEURISTIC(Parametros.HEURISTIC_MODEL);
            InterfazEstadisticas problem = AlgoritmoAReduced.problem(miReader, false);
            total.stop();

            System.out.println("Modelo G" + i);
            System.out.println("Fitness : " + problem.getFitness());
            AryaFitness cobefra = AligmentBasedFitness.calculate(PATH + "g" + i + "/grpd_g" + i + "pi300.xes", PATH + "g" + i + "/FHM.pnml");
            //salida.printCobefra(cobefra.getPNRepResult());

            salida.compareResults(cobefra.getPNRepResult(), miReader);
            //System.out.println("\tTiempo de Cálculo \t Diferent States \t Memoria Consumida");
            //System.out.println("\t" + problem.getTiempoCalculo() + " \t" + problem.getDiferentStates() + " \t" + problem.getMemoriaConsumida());
            System.out.println();
            System.out.println();
        }
    }

    //Test que comparan la ejecucion con CoBeFra
    @Test
    public void problem8() throws Exception {
        String PATH = "/home/martin/Documentos/projects/Aligments/TFG/deMedeiros/";
        InterfazSalida salida = new SalidaTerminalImpl(true);

        AryaFitness cobefra = AligmentBasedFitness.calculate(PATH + "g9/grpd_g9pi300.xes", PATH + "g9/PROM.pnml");
        salida.printCobefra(cobefra.getPNRepResult());

        AlignmentBasedPrecision.calculate(PATH + "g9/grpd_g9pi300.xes", PATH + "g9/PROM.pnml");
    }

    //Test que comparan la ejecucion con CoBeFra en Medeiros
    @Test
    public void testPLG() throws Exception {
        List<String> logsPaths = new ArrayList<>();
        List<String> modelsPaths = new ArrayList<>();
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/1000.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/1000_N.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/5000.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/5000_N.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        runAligments(logsPaths, modelsPaths);
    }

    private void runAligments(List<String> logsPaths, List<String> modelsPaths) throws Exception {
        InterfazSalida salida = new SalidaTerminalImpl(false);
        System.out.println("Aligments Distintos,Trazas Distintas,Estados Co,Estados," +
                "Fitness Co,Fitness,Time Co,Time");

        for (int i = 0; i < logsPaths.size(); i++) {
            String logPath = logsPaths.get(i);
            String modelPath = modelsPaths.get(i);

            System.out.println(logPath);

            //Execute 5 times to get the average
            Timer total = new Timer();
            List<Long> times = new ArrayList<>();
            List<Long> times_cobefra = new ArrayList<>();
            InterfazEstadisticas problem = null;
            AryaFitness cobefra = null;
            Readers miReader = null;
            for (int j = 0; j < 5; j++) {
                resetReader();
                total.start();
                miReader = Readers.getReader(logPath, modelPath + ".hn");
                ParametrosImpl.setHEURISTIC(Parametros.HEURISTIC_MODEL);
                problem = AlgoritmoAReduced.problem(miReader, false);
                total.stop();
                times.add(total.getElapsedTime());

                total.start();
                cobefra = AligmentBasedFitness.calculate(logPath, modelPath + ".pnml");
                total.stop();
                times_cobefra.add(total.getElapsedTime());
            }

            salida.compareResults(cobefra.getPNRepResult(), miReader);
            long average = averageLongs(times);
            long averageCobefra = averageLongs(times_cobefra);
            System.out.print("," + problem.getDiferentStates() + "," + cobefra.getResult() + "," + problem.getFitness() + "," + total.toSeconds(averageCobefra) + "," +
                    total.toSeconds(average));
            System.out.println();
            System.out.println();
        }
    }

    private long averageLongs(List<Long> numbers) {
        Long total = 0l;
        for (Long l : numbers) {
            total += l;
        }
        long l = total / numbers.size();
        return l;
    }
}