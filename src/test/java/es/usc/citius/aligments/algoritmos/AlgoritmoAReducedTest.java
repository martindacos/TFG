package es.usc.citius.aligments.algoritmos;

import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import es.usc.citius.aligments.config.Parametros;
import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.estadisticas.InterfazEstadisticas;
import es.usc.citius.aligments.mains.AligmentsWithCoBeFraMarking;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.aligments.problem.Traza;
import es.usc.citius.aligments.salida.InterfazSalida;
import es.usc.citius.aligments.salida.SalidaTerminalImpl;
import es.usc.citius.aligments.utils.IndividualToPNML;
import es.usc.citius.aligments.utils.Timer;
import es.usc.citius.prodigen.domainLogic.exceptions.*;
import es.usc.citius.prodigen.domainLogic.workflow.Task.Task;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.writer.IndividualWriterInterface;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.writer.IndividualWriterPNMLNew;
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
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import static org.processmining.plugins.petrinet.replayresult.StepTypes.L;
import static org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD;
import static org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL;

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

    //Test que comparan la ejecucion con CoBeFra en Medeiros
    @Test
    public void testPLG() throws Exception {
        List<String> logsPaths = new ArrayList<>();
        List<String> modelsPaths = new ArrayList<>();
        logsPaths.add("/home/martin/Descargas/PLG_Logs/test/log.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/test/test");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/test/diagram.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/test/diagram");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/4_Actividades/5.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/4_Actividades/Individual");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/1000.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/1000_N.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/5000.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/5000_N.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/1000_BN.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/28_Actividades/model");

        logsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/100.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/1000.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/1000_N.xes");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/2000_BN.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/Individual");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/Individual");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/Individual");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/49_Actividades/Individual");
        logsPaths.add("/home/martin/Descargas/PLG_Logs/123_Actividades/5000BN.xes");
        modelsPaths.add("/home/martin/Descargas/PLG_Logs/123_Actividades/Individual");
        runAligments(logsPaths, modelsPaths);
    }

    private void runAligments(List<String> logsPaths, List<String> modelsPaths) throws Exception {
        InterfazSalida salida = new SalidaTerminalImpl(false);
        PrintWriter pw = new PrintWriter(new File("/home/martin/Descargas/PLG_Logs/results.csv"));
        StringBuilder sb = new StringBuilder();
        String HEAD = "Aligments Distintos,Trazas Distintas,Estados Co,Estados Visitados Co,Estados,Estados Visitados," +
                "Fitness Co,Fitness,Time Co,Time\n";
        sb.append(HEAD);
        System.out.print(HEAD);

        for (int i = 0; i < logsPaths.size(); i++) {
            String logPath = logsPaths.get(i);
            String modelPath = modelsPaths.get(i);

            System.out.println(logPath);

            //Execute 5 times to get the average
            Timer total = new Timer();
            List<Long> times = new ArrayList<>();
            List<Long> times_cobefra = new ArrayList<>();
            List<Long> times_mine = new ArrayList<>();
            InterfazEstadisticas problem = null;
            AryaFitness cobefra = null;
            PNRepResultImpl aligmentsWithCobefraMarking = null;
            Readers miReader;
            for (int j = 0; j < 1; j++) {
                resetReader();
                total.start();
                //miReader = Readers.getReader(logPath, modelPath + ".hn");
                //IndividualToPNML writer = new IndividualToPNML();
                //writer.write("/home/martin/Descargas/PLG_Logs/test/testpnml.pnml", miReader.getInd());
                ParametrosImpl.setHEURISTIC(Parametros.HEURISTIC_MODEL);
                //problem = AlgoritmoAReduced.problem(miReader, false);
                total.stop();
                times.add(total.getElapsedTime());

                //AligmentBasedFitness.calculate(logPath, modelPath + ".pnml");

                total.start();
                //cobefra = AligmentBasedFitness.calculate(logPath, modelPath + ".pnml");
                AlignmentBasedPrecision.aryaPrecision(logPath, modelPath + ".pnml");
                total.stop();
                times_cobefra.add(total.getElapsedTime());

                total.start();
//                aligmentsWithCobefraMarking = AligmentsWithCoBeFraMarking.calculate(logPath, modelPath + ".pnml");
                total.stop();
                times_mine.add(total.getElapsedTime());
            }

            //salida.printCobefra(cobefra.getPNRepResult());
            //System.out.println("*******************");
            //salida.printCobefra(aligmentsWithCobefraMarking);
            //String printComparation2 = salida.compareResults(cobefra.getPNRepResult(), miReader);

            //Info With All Metrics
//            Map<String, Object> info = aligmentsWithCobefraMarking.getInfo();
//            String printComparation = compareResults(cobefra.getPNRepResult(), aligmentsWithCobefraMarking);
            //String printComparation = salida.compareResults(aligmentsWithCobefraMarking, miReader);
            long average = averageLongs(times);
            long averageCobefra = averageLongs(times_cobefra);
            long averageMine = averageLongs(times_mine);
//            System.out.println(total.toSeconds(averageCobefra) + "," + total.toSeconds(average) + " ," + total.toSeconds(averageMine));
//            System.out.print(printComparation + "," + cobefra.getResult() + "," + info.get("Trace Fitness"));
            /*System.out.print(printComparation + "," + problem.getDiferentStates() + "," + problem.getVisitedStates() + "," + cobefra.getResult() + "," + problem.getFitness() + "," + total.toSeconds(averageCobefra) + "," +
                    total.toSeconds(average));
            sb.append(printComparation + "," + problem.getDiferentStates() + "," + problem.getVisitedStates() + "," + cobefra.getResult() + "," + problem.getFitness() + "," + total.toSeconds(averageCobefra) + "," +
                    total.toSeconds(average) + "\n");*/
            System.out.println();
            System.out.println();
        }

        pw.write(sb.toString());
        pw.close();
    }

    private long averageLongs(List<Long> numbers) {
        Long total = 0l;
        for (Long l : numbers) {
            total += l;
        }
        long l = total / numbers.size();
        return l;
    }

    public String compareResults(PNRepResult cobefra, PNRepResult mine) {
        Iterator<SyncReplayResult> cobefraIterator = cobefra.iterator();
        Integer queued_States = 0;
        Integer num_States = 0;
        Integer traversed_Arcs = 0;
        Integer queued_States2 = 0;
        Integer num_States2 = 0;
        Integer traversed_Arcs2 = 0;
        Integer count = 0;
        while (cobefraIterator.hasNext()) {
            SyncReplayResult nextCobefra = cobefraIterator.next();
            queued_States += nextCobefra.getInfo().get("Queued States").intValue();
            num_States += nextCobefra.getInfo().get("Num. States").intValue();
            traversed_Arcs += nextCobefra.getInfo().get("Traversed Arcs").intValue();

            Iterator<SyncReplayResult> mineIterator = mine.iterator();
            while (mineIterator.hasNext()) {
                SyncReplayResult nextMine = mineIterator.next();

                if (nextMine.getTraceIndex().equals(nextCobefra.getTraceIndex())) {
                    queued_States2 += nextMine.getInfo().get("Queued States").intValue();
                    num_States2 += nextMine.getInfo().get("Num. States").intValue();
                    traversed_Arcs2 += nextMine.getInfo().get("Traversed Arcs").intValue();

                    if (!nextCobefra.getInfo().get(PNRepResult.TRACEFITNESS).equals(nextMine.getInfo().get(PNRepResult.TRACEFITNESS)) ||
                            !nextCobefra.getInfo().get(PNRepResult.RAWFITNESSCOST).equals(nextMine.getInfo().get(PNRepResult.RAWFITNESSCOST)) ||
                            !nextCobefra.getInfo().get(PNRepResult.MAXFITNESSCOST).equals(nextMine.getInfo().get(PNRepResult.MAXFITNESSCOST)) ||
                            !nextCobefra.getInfo().get(PNRepResult.MAXMOVELOGCOST).equals(nextMine.getInfo().get(PNRepResult.MAXMOVELOGCOST)) ||
                            !nextCobefra.getInfo().get(PNRepResult.MOVELOGFITNESS).equals(nextMine.getInfo().get(PNRepResult.MOVELOGFITNESS)) ||
                            !nextCobefra.getInfo().get(PNRepResult.MOVEMODELFITNESS).equals(nextMine.getInfo().get(PNRepResult.MOVEMODELFITNESS)) ||
                            !nextCobefra.getInfo().get(PNRepResult.ORIGTRACELENGTH).equals(nextMine.getInfo().get(PNRepResult.ORIGTRACELENGTH))) {
                            //!nextCobefra.getInfo().get(PNRepResult.NUMSTATEGENERATED).equals(nextMine.getInfo().get(PNRepResult.NUMSTATEGENERATED))) {
                        printSyncReplayResult(nextCobefra);
                        printSyncReplayResult(nextMine);
                        count++;
                    }
                    break;
                }
            }
        }

        return count + "," + cobefra.size() + "," + queued_States + "," + num_States + "," + traversed_Arcs + "," + queued_States2 + "," + num_States2 + "," + traversed_Arcs2;
    }

    private void printSyncReplayResult(SyncReplayResult result) {
        String salida = "";
        salida = salida + "\n***************************";
        salida = salida + "\n\n---------SALIDA VISUAL----------";
        salida = salida + "\n\tTRAZA\tMODELO";
        List<Object> nodeInstance = result.getNodeInstance();
        List<StepTypes> steps = result.getStepTypes();
        for (int i = 0; i < nodeInstance.size(); i++) {
            Object node = nodeInstance.get(i);
            StepTypes step = steps.get(i);
            if (step.equals(LMGOOD)) {
                salida = salida + "\n\t" + node + "\t" + node;
            } else if (step.equals(L)) {
                salida = salida + "\n\t" + node + "\t>>";
            } else if (step.equals(MREAL)) {
                salida = salida + "\n\t>>\t" + node;
            }
        }

        System.out.println(salida);
    }
}