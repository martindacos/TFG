package es.usc.citius.aligments.estadisticas;

import es.usc.citius.aligments.config.ParametrosImpl;
import es.usc.citius.aligments.problem.InterfazTraza;
import es.usc.citius.aligments.problem.NState;
import es.usc.citius.aligments.problem.NState.State;
import es.usc.citius.aligments.problem.Traza;
import static es.usc.citius.aligments.problem.NState.StateMove.*;
import es.usc.citius.hipster.model.AbstractNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author marti
 */
public class EstadisticasImpl implements InterfazEstadisticas {

    private double totalEventosLog;
    private final ParametrosImpl parametrosImpl;

    private Double costeIndividuo;
    private Double fitness;
    private Double precission;
    private Long tiempoCalculo;
    private Double memoriaConsumida;

    public EstadisticasImpl() {
        this.totalEventosLog = 0d;
        parametrosImpl = ParametrosImpl.getParametrosImpl();
    }

    @Override
    public Double getFitness() {
        return fitness;
    }

    @Override
    public Double getPrecission() {
        return precission;
    }

    @Override
    public Long getTiempoCalculo() {
        return tiempoCalculo;
    }

    @Override
    public Double costeTraza(ArrayList<Traza> t, int pos) {
        return t.get(pos).getScore();
    }

    @Override
    public Double costeIndividuo(ArrayList<InterfazTraza> t) {
        Double aux = 0d;
        if (t != null) {
            for (int i = 0; i < t.size(); i++) {
                //Obtenemos el coste de la traza y su número de repeticiones y lo anadimos
                aux = aux + t.get(i).getScore() * t.get(i).getNumRepeticiones();
            }
        }
        return aux;
    }

    public int menorCamino(ArrayList<AbstractNode> nodosSalida) {
        int menorCamino = 999999999;
        if (nodosSalida != null) {
            for (int i = 0; i < nodosSalida.size(); i++) {
                int aux = 0;
                Iterator it2 = nodosSalida.get(i).path().iterator();
                //La primera iteración corresponde con el Estado Inicial
                it2.next();
                while (it2.hasNext()) {
                    AbstractNode node = (AbstractNode) it2.next();
                    if (node.action().equals(SINCRONO) || node.action().equals(TRAZA)) {
                        aux++;
                    }
                }
                if (aux < menorCamino) {
                    menorCamino = aux;
                }
            }
        }
        return menorCamino;
    }

    @Override
    public Double fitnessNuevo(ArrayList<InterfazTraza> t, ArrayList<AbstractNode> nodosSalida) {
        fitness = 0d;
        costeIndividuo = costeIndividuo(t);
        if (t != null && nodosSalida != null) {
            int menorCamino = this.menorCamino(nodosSalida);
            //Realizamos el sumatorio para todas la trazas del log
            for (int i = 0; i < t.size(); i++) {
                //System.out.println("TotalEventosLog: " + totalEventosLog + " + " + t.get(i).tamTrace() + " *" + t.get(i).getNumRepeticiones());
                this.totalEventosLog = this.totalEventosLog + (t.get(i).tamTrace() * t.get(i).getNumRepeticiones());
            }
            //Obtenemos el fitness
            fitness = 1 - (costeIndividuo / (this.totalEventosLog * parametrosImpl.getC_TRAZA() + (menorCamino * t.size() * parametrosImpl.getC_MODELO())));
            if (fitness < 0) {
                fitness = 0d;
            }
        }
        return fitness;
    }

    //Calculamos el número de tareas que tienen el mismo contexto que el prefijo
    public Integer tareasPrefijo(ArrayList<InterfazTraza> t, ArrayList<Integer> prefijo) {
        HashSet<Integer> actividades = new HashSet<Integer>();
        if (t != null && prefijo != null) {
            //Para todas la trazas del log
            for (int i = 0; i < t.size(); i++) {
                //En un principio ambos contextos son iguales
                boolean iguales = true;
                int j = 0;
                //Mientras no acabemos las tareas de la traza o del prefijo
                while (j < t.get(i).tamTrace() && j < prefijo.size()) {
                    //Comparamos si ambas tareas son distintas
                    if (t.get(i).leerTarea(j).intValue() != prefijo.get(j).intValue()) {
                        //Si son distintas no tienen el mismo contexto
                        iguales = false;
                    }
                    j++;
                }
                //Si son iguales y existe la siguientes tarea de la traza
                if (iguales && j < t.get(i).tamTrace()) {
                    //Añadimos la tarea sólo en caso de que no se encuentre añadida
                    actividades.add(t.get(i).leerTarea(j));
                }
            }
        }
        //System.out.println("ActividadesMismoContexto: " +actividades);
        return actividades.size();
    }

    @Override
    public Double precission(ArrayList<InterfazTraza> t, ArrayList<AbstractNode> nodosSalida) {
        precission = 0d;
        if (t != null && nodosSalida != null) {
            ArrayList<ArrayList<State>> tareasActivasEstado = tareasActivasEstado(nodosSalida);
            Double subPrecission = 0d;
            //Para todas las trazas del log
            for (int i = 0; i < t.size(); i++) {
                //Array para guardar las tareas del prefijo
                ArrayList<Integer> prefijo = new ArrayList<Integer>();
                //Para todas las tareas de la traza
                for (int j = 0; j < t.get(i).tamTrace(); j++) {
                    //Añadimos al prefijo la tarea anterior a la procesada
                    if (j > 0) {
                        prefijo.add(t.get(i).leerTarea(j - 1));
                    }
                    //Calculamos el contexto del prefijo
                    double enL = this.tareasPrefijo(t, prefijo);
                    //System.out.println("Subprecision = "+ subPrecission + " + " + t.get(i).getNumRepeticiones() +" * ("+ enL + " / " + tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size() +" )");
                    //Realizamos el sumatorio controlando que el número de tareas activas sea mayor que 1
                    if (tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size() > 0) {
                        int divisor = tareasActivasEstado.get(i).get(j).getMarcado().getEnabledElements().size();
//                        if (enL > 1) {
//                            enL = 1;
//                        }
//                        if (divisor > 1) {
//                            divisor = 1;
//                        }
                        subPrecission = subPrecission + t.get(i).getNumRepeticiones() * (enL / divisor);
                    }
                }
                //System.out.println();
            }
            //Obtenemos la precisión
            if (totalEventosLog > 0 && subPrecission > 0) {
                precission = 1 / this.totalEventosLog * subPrecission;
            }
            //System.out.println("Precision = 1 / " + totalEventosLog + " * " + subPrecission);
        }
        return precission;
    }

    //Cálculo de las tareas activas en cada estado   
    public ArrayList<ArrayList<State>> tareasActivasEstado(ArrayList<AbstractNode> nodosSalida) {
        ArrayList<ArrayList<State>> tareasActivasEstado = new ArrayList<ArrayList<State>>();
        for (int i = 0; i < nodosSalida.size(); i++) {
            ArrayList<NState.State> tareasEstadoTraza = new ArrayList<NState.State>();
            Iterator it2 = nodosSalida.get(i).path().iterator();
            //La primera iteración corresponde con el Estado Inicial, que no imprimimos
            AbstractNode node2 = (AbstractNode) it2.next();
            NState.State s2 = (NState.State) node2.state();
            tareasEstadoTraza.add(s2);
            while (it2.hasNext()) {
                AbstractNode node = (AbstractNode) it2.next();
                NState.State s = (NState.State) node.state();
                tareasEstadoTraza.add(s);
            }
            tareasActivasEstado.add(tareasEstadoTraza);
        }
        return tareasActivasEstado;
    }

    @Override
    public Double getCoste() {
        return this.costeIndividuo;
    }

    @Override
    public void setTiempoCalculo(Long tiempo) {
        tiempoCalculo = tiempo;
    }

    @Override
    public void setMemoriaConsumida(double memoria) {
        memoriaConsumida = memoria;
    }

    @Override
    public double getMemoriaConsumida() {
        return this.memoriaConsumida;
    }
}
