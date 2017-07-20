package Problem;

import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class NState {

    private NState() {
    }

    //Posibles movimientos del alineamiento
    public enum StateMove {
        SINCRONO, MODELO, TRAZA, MODELO_FORZADO
    }

    public static final class State {

        //Posición actual de análisis de la traza
        private int pos;
        //Marcada del modelo para este estado
        private CMMarking marcado;
        //Movimiento ejecutado en este estado
        private StateMove mov;
        //Para identificar a tareas dumming nos skips
        private Integer tarea;
        //Tareas que ya fueron ejecutadas en el modelo
        private TIntHashSet tareasEjecutadasModelo;

        public State(CMIndividual ind) {
            pos = 0;
            marcado = new CMMarking(ind, new Random(666));
            tarea = null;
            tareasEjecutadasModelo = new TIntHashSet();
        }

        public State(State a) {
            pos = a.getPos();
            //Creamos una copia de las tareas ejecutadas en el modelo
            tareasEjecutadasModelo = new TIntHashSet(a.getTareasEjecutadasModelo());
        }

        public TIntHashSet getTareasEjecutadasModelo() {
            return tareasEjecutadasModelo;
        }

        public int getPos() {
            return pos;
        }

        public CMMarking getMarcado() {
            return marcado;
        }

        public Integer getTarea() {
            return tarea;
        }

        public void setTarea(Integer tarea) {
            this.tarea = tarea;
        }

        public void setMarcado(CMMarking marcado) {
            this.marcado = marcado;
        }

        public void setMov(StateMove mov) {
            this.mov = mov;
        }

        public StateMove getMov() {
            return mov;
        }

        public void avanzarTarea() {
            pos++;
        }

        public TIntHashSet getTareas() {
            return marcado.getEnabledElements();
        }

        public void avanzarMarcado(Integer e) {
            marcado.execute(e);
            tareasEjecutadasModelo.add(e);
        }

        //La tarea final se ha ejecutado y no quedan tareas activas
        public boolean finalModelo() {
            return marcado.isEndPlaceEnabled();
        }

        public boolean sinTokens() {
            Integer sinTokens = 0;
            ArrayList<HashMap<TIntHashSet, Integer>> tokens = marcado.getTokens();
            for (int i = 0; i < tokens.size(); i++) {
                HashMap<TIntHashSet, Integer> tareas = tokens.get(i);
                for (Map.Entry<TIntHashSet, Integer> entry : tareas.entrySet()) {
                    //System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                    //Si no tienen tokens
                    if (entry.getValue() != 0) {
                        sinTokens++;
                    }
                }
            }
            System.out.println(sinTokens);
            return sinTokens <= 1;
        }

        public boolean finalModelo(CMIndividual ind) {
            //Si el modelo NO tiene tarea final
            if (ind.getEndTasks().size() == 0) {
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
