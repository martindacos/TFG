package es.usc.citius.aligments.problem;

import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import es.usc.citius.prodigen.domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import es.usc.citius.aligments.algoritmos.AlgoritmoAReducedLarge;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class NStateLarge {

    private NStateLarge() {
    }


    public static final class StateLarge {

        //Posición actual de análisis de la traza
        private int pos;
        //Marcado del modelo para este estado
        private CMMarking marcado;
        //Movimiento ejecutado en este estado
        private NState.StateMove mov;
        //Para identificar a tareas dumming nos skips
        private Integer tarea;
        //Tareas que ya fueron ejecutadas en el modelo
        private TIntHashSet tareasEjecutadasModelo;
        //Última heurística calculada
        private double heuristica;
        //Tokens del modelo
        private ArrayList<HashMap<TIntHashSet, Integer>> tokens;
        //Tareas activas del modelo
        private TIntHashSet possibleEnabledTasks;
        //Contador de movimientos realizados en la traza
        private int trazaMovs;
        //Contador de movimientos sincronos realizados
        private int sincroMovs;
        //Tarea final del modelo
        private int tareaFinalModelo;

        public StateLarge(CMIndividual ind) {
            pos = 0;
            marcado = new CMMarking(ind, new Random(666));
            tarea = null;
            tareasEjecutadasModelo = new TIntHashSet();
            heuristica = Double.MAX_VALUE;
            trazaMovs = 0;
            sincroMovs = 0;
            TIntIterator it = ind.getEndTasks().iterator();
            if (it.hasNext()) {
                tareaFinalModelo = it.next();
            } else {
                tareaFinalModelo = -1;
            }
        }

        public void restartState() {
            tokens = AlgoritmoAReducedLarge.cloneTokens(marcado.getTokens());
            possibleEnabledTasks = new TIntHashSet(marcado.getEnabledElements());
        }

        public StateLarge(StateLarge a) {
            pos = a.getPos();
            //Creamos una copia de las tareas ejecutadas en el modelo
            tareasEjecutadasModelo = new TIntHashSet(a.getTareasEjecutadasModelo());
            heuristica = a.getHeuristica();
            marcado = a.getMarcado();
            tokens = a.getTokens();
            possibleEnabledTasks = a.getPossibleEnabledTasks();
            trazaMovs = a.getTrazaMovs();
            sincroMovs = a.getSincroMovs();
            tareaFinalModelo = a.getTareaFinalModelo();
        }

        public TIntHashSet getTareasEjecutadasModelo() {
            return tareasEjecutadasModelo;
        }

        public int getPos() {
            return pos;
        }

        public int getSincroMovs() {
            return sincroMovs;
        }

        public int getTareaFinalModelo() {
            return tareaFinalModelo;
        }

        public CMMarking getMarcado() {
            return marcado;
        }

        public Integer getTarea() {
            return tarea;
        }

        public double getHeuristica() {
            return heuristica;
        }

        public ArrayList<HashMap<TIntHashSet, Integer>> getTokens() {
            return tokens;
        }

        public void setTokens(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
            this.tokens = tokens;
        }

        public TIntHashSet getPossibleEnabledTasks() {
            return possibleEnabledTasks;
        }

        public void setPossibleEnabledTasks(TIntHashSet possibleEnabledTasks) {
            this.possibleEnabledTasks = possibleEnabledTasks;
        }

        public void setTarea(Integer tarea) {
            this.tarea = tarea;
        }

        public void setMarcado(CMMarking marcado) {
            this.marcado = marcado;
        }

        public void setMov(NState.StateMove mov) {
            if (mov.equals(NState.StateMove.SINCRONO)) {
                sincroMovs++;
            } else {
                trazaMovs++;
            }
            this.mov = mov;
        }

        public void setHeuristica(double heuristica) {
            this.heuristica = heuristica;
        }

        public NState.StateMove getMov() {
            return mov;
        }

        public void avanzarTarea() {
            pos++;
        }

        public TIntHashSet getTareas() {
            return marcado.getEnabledElements();
        }

        public int getTrazaMovs() {
            return trazaMovs;
        }

        public void avanzarMarcado(Integer e) {
            marcado.execute(e);
            tareasEjecutadasModelo.add(e);
        }

        //La tarea final se ha ejecutado y no quedan tareas activas
        public boolean finalModelo() {
            return tareasEjecutadasModelo.contains(this.tareaFinalModelo);
        }

        //Devuelve TRUE cuando quedan tokens en el modelo y FALSE cuando no quedan
        public boolean sinTokens() {
            Integer numTokens = 0;
            ArrayList<HashMap<TIntHashSet, Integer>> tokens = marcado.getTokens();
            for (int i = 0; i < tokens.size(); i++) {
                HashMap<TIntHashSet, Integer> tareas = tokens.get(i);
                for (Map.Entry<TIntHashSet, Integer> entry : tareas.entrySet()) {
                    //System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                    //Si tiene tokens
                    if (entry.getValue() != 0) {
                        //Aumentamos su valor en 1 (da igual el nº de tokens que tenga)
                        numTokens++;
                    }
                }
            }
            //System.out.println(numTokens);
            return numTokens <= 1;
        }

        public boolean finalModelo(CMIndividual ind) {
            //Si el modelo NO tiene tarea final
            if (tareaFinalModelo == -1) {
                //System.out.println(marcado.toString());
                return sinTokens();
            } else {
                return false;
            }
        }

        //Ninguna tarea activa en el modelo
        public boolean Enabled() {
            return marcado.getEnabledElements().size() > 0;
        }

        @Override
        public String toString() {
            return "State{" + "pos=" + pos + ", mov=" + mov + ", tarea=" + tarea + '}';
        }

        public boolean isEjecutedTask(Integer t) {
            return tareasEjecutadasModelo.contains(t);
        }
    }
}
