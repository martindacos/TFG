package es.usc.citius.aligments.salida;

import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState;
import es.usc.citius.aligments.problem.NStateLarge;
import es.usc.citius.aligments.problem.Readers;
import es.usc.citius.hipster.model.AbstractNode;
import es.usc.citius.hipster.model.impl.ADStarNodeImpl;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.prodigen.domainLogic.workflow.Task.Task;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.CMTask.CMTask;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static es.usc.citius.aligments.problem.NState.StateMove.*;
import static org.processmining.plugins.petrinet.replayresult.StepTypes.*;

/**
 * @author marti
 */
public class SalidaTerminalImpl implements InterfazSalida {

    private boolean imprimir = true;
    private Integer countS = 0;
    private Integer countT = 0;
    private Integer countM = 0;
    private Integer countMF = 0;

    public SalidaTerminalImpl(boolean imprimir) {
        this.imprimir = imprimir;
    }

    @Override
    public String estadisticasModelo(CMIndividual ind, double coste, long tiempo, double memoria) {
        String s = "";
        if (imprimir) {
            s = s + "\n\n************** ESTADÍSTICAS DEL MODELO **************";
            int cI = (int) coste;
            s = s + "\nCoste del modelo: " + cI;
            s = s + "\nTiempo total de cálculo = " + tiempo + " ms";
            s = s + "\nFitness del modelo: " + ind.getFitness().getCompleteness();
            s = s + "\nPrecission del modelo: " + ind.getFitness().getPreciseness();
            int mI = (int) memoria;
            s = s + "\nMemoria total consumida = " + mI;
        }

        return s;
    }

    @Override
    public void setVisible(boolean visible) {
        this.imprimir = visible;
    }

    @Override
    public void imprimirModelo(CMIndividual ind) {
        ind.print();
    }

    @Override
    public String ActualizarTrazas(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind) {
        String salida = "";
        if (imprimir) {
            Iterator it2 = nodo.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            salida = salida + "\n***************************";
//            System.out.println(nodosSalida.get(i).path());
            salida = salida + "\n\n---------SALIDA VISUAL----------";
            salida = salida + "\n\tTRAZA\tMODELO";
            while (it2.hasNext()) {
                AbstractNode node;
                if (ad) {
                    node = (WeightedNode) it2.next();
                } else {
                    node = (ADStarNodeImpl) it2.next();
                }
                NState.State s = (NState.State) node.state();
                if (node.action().equals(SINCRONO)) {
                    countS++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO)) {
                    countM++;
                    salida = salida + "\n\t>>\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO_FORZADO)) {
                    countMF++;
                    salida = salida + "\n\t>>'\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else {
                    countT++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t>>";
                }
            }
            salida = salida + "\n\nCoste del alineamiento = " + trace.getScore();
            salida = salida + "\nCoste del alineamiento con repeticiones = " + trace.getScoreRepetido();
            salida = salida + "\nTiempo de cálculo del alineamiento = " + trace.getTiempoC() + " ms";
            salida = salida + "\nMemoria consuminda por el alineamiento = " + trace.getMemoriaC();
        }

        return salida;
    }

    @Override
    public String ActualizarTrazasOld(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind) {
        String salida = "";
        if (imprimir) {
            Iterator it2 = nodo.path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            it2.next();
            salida = salida + "\n***************************";
//            System.out.println(nodosSalida.get(i).path());
            salida = salida + "\n\n---------SALIDA VISUAL----------";
            salida = salida + "\n\tTRAZA\tMODELO";
            while (it2.hasNext()) {
                AbstractNode node;
                if (ad) {
                    node = (WeightedNode) it2.next();
                } else {
                    node = (ADStarNodeImpl) it2.next();
                }
                NStateLarge.StateLarge s = (NStateLarge.StateLarge) node.state();
                if (node.action().equals(SINCRONO)) {
                    countS++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO)) {
                    countM++;
                    salida = salida + "\n\t>>\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else if (node.action().equals(MODELO_FORZADO)) {
                    countMF++;
                    salida = salida + "\n\t>>'\t" + ind.getTask(s.getTarea()).getTask().getId();
                } else {
                    countT++;
                    salida = salida + "\n\t" + ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId() + "\t>>";
                }
            }
            salida = salida + "\n\nCoste del alineamiento = " + trace.getScore();
            salida = salida + "\nCoste del alineamiento con repeticiones = " + trace.getScoreRepetido();
            salida = salida + "\nTiempo de cálculo del alineamiento = " + trace.getTiempoC() + " ms";
            salida = salida + "\nMemoria consuminda por el alineamiento = " + trace.getMemoriaC();
        }

        return salida;
    }

    @Override
    public void ActualizarTrazasReduced(InterfazTraza trace, AbstractNode nodo, boolean ad, CMIndividual ind) {
        String salida = "";
        Iterator it2 = nodo.path().iterator();
        //La primera iteración corresponde con el Estado Inicial, que no imprimimos
        it2.next();
        salida = salida + "\n***************************";
        //System.out.println(nodosSalida.get(i).path());
        salida = salida + "\n\n---------SALIDA VISUAL----------";
        salida = salida + "\n\tTRAZA\tMODELO";
        while (it2.hasNext()) {
            AbstractNode node;
            if (ad) {
                node = (WeightedNode) it2.next();
            } else {
                node = (ADStarNodeImpl) it2.next();
            }
            NState.State s = (NState.State) node.state();
            trace.addStep((NState.StateMove) node.action());
            String idTask = "";
            if (trace.leerTarea(s.getPos() - 1) != null) {
                idTask = ind.getTask(trace.leerTarea(s.getPos() - 1)).getTask().getId();
            }
            String idTaskModelo = ind.getTask(s.getTarea()).getTask().getId();
            if (node.action().equals(SINCRONO)) {
                countS++;
                trace.addStepTask(idTask);
                salida = salida + "\n\t" + idTask + "\t" + idTask;
            } else if (node.action().equals(MODELO)) {
                countM++;
                trace.addStepTask(idTaskModelo);
                salida = salida + "\n\t>>\t" + idTaskModelo;
            } else if (node.action().equals(MODELO_FORZADO)) {
                countMF++;
                trace.addStepTask(idTaskModelo);
                salida = salida + "\n\t>>'\t" + idTaskModelo;
            } else {
                countT++;
                trace.addStepTask(idTask);
                salida = salida + "\n\t" + idTask + "\t>>";
            }
        }
        salida = salida + "\n\nCoste del alineamiento = " + trace.getScore();
        salida = salida + "\nCoste del alineamiento con repeticiones = " + trace.getScoreRepetido();

        if (imprimir) System.out.println(salida);
    }

    @Override
    public void printTrace(InterfazTraza trace) {
        String salida = "";
        salida = salida + "\n***************************";
        salida = salida + "\n\n---------SALIDA VISUAL----------";
        salida = salida + "\n\tTRAZA\tMODELO";
        List<NState.StateMove> steps = trace.getSteps();
        List<String> stepsTasks = trace.getStepsTasks();
        int j = 0;
        for (int i = 0; i < steps.size(); i++) {
            NState.StateMove step = steps.get(i);
            String stepTask = stepsTasks.get(i);
            stepTask = stepTask.replace(":complete", "");
            if (step.equals(SINCRONO)) {
                j++;
                salida = salida + "\n\t" + stepTask + "\t" + stepTask;
            } else if (step.equals(MODELO)) {
                salida = salida + "\n\t>>\t" + stepTask;
            } else {
                j++;
                salida = salida + "\n\t" + stepTask + "\t>>";
            }
        }
        salida = salida + "\n\nCoste del alineamiento = " + trace.getScore();
        salida = salida + "\nCoste del alineamiento con repeticiones = " + trace.getScoreRepetido();

        System.out.println(salida);
    }

    @Override
    public void printCobefra(SyncReplayResult result) {
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

    @Override
    public void printCobefra(PNRepResult pnRepResult) {
        Iterator<SyncReplayResult> iterator = pnRepResult.iterator();
        while (iterator.hasNext()) {
            SyncReplayResult next = iterator.next();
            printCobefra(next);
        }
    }

    public void compareResults(PNRepResult pnRepResult, Readers miReader) {
        Iterator<SyncReplayResult> iterator = pnRepResult.iterator();
        Integer count = 0;
        Integer queued_States = 0;
        pnRepResult.size();
        while (iterator.hasNext()) {
            SyncReplayResult next = iterator.next();
            queued_States += next.getInfo().get("Queued States").intValue();
            List<StepTypes> stepsCobefra = next.getStepTypes();
            SortedSet<Integer> traceIndex = next.getTraceIndex();
            try {
                InterfazTraza trace = null;
                Iterator<Integer> iteratorTraceIndex = traceIndex.iterator();
                while (trace == null && iteratorTraceIndex.hasNext()) {
                    trace = miReader.getTrace(iteratorTraceIndex.next());
                }
                if (trace == null) {
                    System.out.println("TRAZA NO ENCONTRADA : " + traceIndex);
                } else if (!trace.compareSteps(stepsCobefra)) {
                    printCobefra(next);
                    printTrace(trace);
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.print(count + "," + pnRepResult.size() + "," + queued_States);
    }

    @Override
    public String getStatMovs() {
        String s = "";
        s = s + "\nSINCRONO :" + countS;
        s = s + "\nTRAZA :" + countT;
        s = s + "\nMODELO :" + countM;
        s = s + "\nMODELO FORZADO :" + countMF;
        return s;
    }

    @Override
    public void setTotalTrazas(int size) {
        if (imprimir) {
            System.out.println("El número total de trazas es de " + size);
        }
    }

}
