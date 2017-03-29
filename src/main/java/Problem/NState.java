package Problem;

import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
import domainLogic.workflow.algorithms.geneticMining.individual.CMIndividual;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public final class NState {

    private NState() {
    }

    //Posibles movimientos del alineamiento
    public enum StateMove {
        OK, SKIP, INSERT, TOTALSKIP
    }

    public static final class State {
        //Posición actual de análisis de la traza
        private int pos;
        //Marcada del modelo para esta traza
        private CMMarking marcado;
        //MOvimiento ejecutado en este estado
        private StateMove mov;
        //Para identificar a tareas dumming nos skips
        private Integer tarea;
        
        public State(CMIndividual ind) {
            pos = 0;
            marcado = new CMMarking(ind, new Random(666));
        }

        public State(State a) {
            pos = a.getPos();
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
        
        public void avanzarTarea() {
            pos++;
        }
        
        public TIntHashSet getTareas() {
            return marcado.getEnabledElements();
        }
                
        public void avanzarMarcado(Integer e) {
            marcado.execute(e);
        }
          
        //La tarea final se ha ejecutado y no quedan tareas activas
        public boolean finalModelo() {
            return marcado.isEndPlaceEnabled();
        }
        
        public boolean noEnabled() {
            return marcado.getEnabledElements().size() == 0;
        }
     
        public ArrayList<Integer> getTaskWithTokens() {
            ArrayList<Integer> tareas = new ArrayList<Integer>();
            ArrayList<HashMap<TIntHashSet, Integer>> tokens = marcado.getTokens();
            for (HashMap<TIntHashSet, Integer> token : tokens) {              
                for (TIntHashSet tokenKey : token.keySet()) {
                    if (token.get(tokenKey) > 0) {
                        //System.out.println("Tareas con algún token");
                        TIntIterator it = tokenKey.iterator();
                        while (it.hasNext()) {
                            //System.out.println(it.next());
                            tareas.add(it.next());
                        }
                    }
                }
            }
            return tareas;
        }
//        @Override
//        public int hashCode() {
//            int hash = 5;
//            hash = 43 * hash + this.pos;
//            hash = 43 * hash + Objects.hashCode(this.mov);
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            final State other = (State) obj;
//            System.out.println("Comparación "  + this.mov +" "+ other.mov + " " +this.pos +" "+ other.pos + " " +this.tarea +" "+ other.tarea);
//            return this.pos==other.pos && this.mov == other.mov && (this.tarea == null ? other.tarea == null : this.tarea.equals(other.tarea));
//        }

//        @Override
//        public int hashCode() {
//            int hash = 3;
//            hash = 37 * hash + this.pos;
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            final State other = (State) obj;
//            if (this.pos != other.pos) {
//                return false;
//            }
//            if (this.marcado.getNumberTokens() != other.marcado.getNumberTokens()) {
//                return false;
//            }
//            
//            TIntHashSet estasTareas = this.marcado.getEnabledElements();
//            TIntHashSet otrasTareas = this.marcado.getEnabledElements();
//            TIntIterator tasks = estasTareas.iterator();
//            while (tasks.hasNext()) {
//                int id = tasks.next();
//                System.out.println(id);
//                if (!otrasTareas.contains(id)) {
//                    return false;
//                }
//            }
//            return true;
//        }

        
    }
}
