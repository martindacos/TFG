package Problem;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marti
 */
public class EjecTareas {

    private Integer tareaSINCRONO;
    private Integer tareaTRAZA;
    //Posibles tareas a ejecutar en esta instancia de la traza
    private ArrayList<Integer> tareasMODELO;
    private HashMap<Integer, Integer> tareasModeloForzado;
    private Integer tareaModeloForzadoActual;
    //Colección de elementos que vamos a guardar del marcado
    ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    //Elementos para la copia del marcado
    private int startPlace;
    private int numOfTokens;
    private int endPlace;
    private TIntHashSet possibleEnabledTasks;

    private TIntHashSet tareasTokensEntrada;

    public EjecTareas() {
    }

    public ArrayList<Integer> getTareasModelo() {
        return tareasMODELO;
    }

    public Integer getTareaTRAZA() {
        return tareaTRAZA;
    }

    public Integer getTareaSINCRONA() {
        return tareaSINCRONO;
    }

    public void clear() {
        this.tareaSINCRONO = null;
        tareasMODELO = new ArrayList<Integer>();
        tareasModeloForzado = new HashMap<>();
        tareaModeloForzadoActual = 0;
        tareasTokensEntrada = new TIntHashSet();
    }

    public void anadirSincrono(Integer a) {
        tareaSINCRONO = a;
    }

    public void anadirTraza(Integer a) {
        tareaTRAZA = a;
    }

    public void anadirModelo(Integer a) {
        tareasMODELO.add(a);
    }

    public Integer leerTareaModelo() {
        if (tareasMODELO != null && !tareasMODELO.isEmpty()) {
            Integer modelo = tareasMODELO.get(0);
            tareasMODELO.remove(0);
            return modelo;
        } else {
            return null;
        }
    }

    public Integer leerTareaModeloForzado() {
        Integer artificial = null;
        if (tareasModeloForzado != null && !tareasModeloForzado.isEmpty()) {
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : tareasModeloForzado.entrySet()) {
                if (i == tareaModeloForzadoActual) {
                    artificial = entry.getKey();
                }
                i++;
            }
            tareaModeloForzadoActual++;
        }
        return artificial;
    }

    public ArrayList<HashMap<TIntHashSet, Integer>> getTokens() {
        return tokens;
    }

    public ArrayList<HashMap<TIntHashSet, Integer>> cloneTokens() {
        ArrayList<HashMap<TIntHashSet, Integer>> clone = new ArrayList<>();

        for (HashMap<TIntHashSet, Integer> token : tokens) {
            HashMap<TIntHashSet, Integer> tokenClone = new HashMap<>();
            for (TIntHashSet tokenKey : token.keySet()) {
                TIntHashSet tokenKeyClone = new TIntHashSet();
                tokenKeyClone.addAll(tokenKey);
                tokenClone.put(tokenKeyClone, token.get(tokenKey));
            }
            clone.add(tokenClone);
        }

        return clone;
    }

    public void setTokens(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
        this.tokens = tokens;
    }

    public int getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(int startPlace) {
        this.startPlace = startPlace;
    }

    public int getNumOfTokens() {
        return numOfTokens;
    }

    public void setNumOfTokens(int numOfTokens) {
        this.numOfTokens = numOfTokens;
    }

    public int getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(int endPlace) {
        this.endPlace = endPlace;
    }

    public TIntHashSet getPossibleEnabledTasks() {
        return possibleEnabledTasks;
    }

    public void setPossibleEnabledTasks(TIntHashSet possibleEnabledTasks) {
        this.possibleEnabledTasks = possibleEnabledTasks;
    }

//    public void setTareasModeloForzado(HashMap<Integer, Integer> tareasModeloForzado) {
//        this.tareasModeloForzado = tareasModeloForzado;
//    }
    //Función que revisa cuantos tokens son necesarios para ejecutar la tarea forzada
    //Lo guardamos en un HasMap de Tarea y Tokens restantes
    public void anadirTareaForzada(Integer a) {
        if (tareasModeloForzado == null) {
            tareasModeloForzado = new HashMap();
        }
        tareasModeloForzado.put(a, 0);
    }

    //Función que revisa las tareas que tienen algún token en su entrada
    public TIntHashSet tareasTokensEntrada(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
        if (tareasTokensEntrada == null) {
            tareasTokensEntrada = new TIntHashSet();
        }
        if (tokens != null) {
            for (int i = 0; i < tokens.size(); i++) {
                HashMap<TIntHashSet, Integer> tareas = tokens.get(i);
                for (Map.Entry<TIntHashSet, Integer> entry : tareas.entrySet()) {
                    //System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                    //Si tienen algún token
                    if (entry.getValue() > 0) {
                        TIntHashSet subsets = entry.getKey();
                        TIntIterator tasks = subsets.iterator();
                        //Añadimos las tareas a la lista
                        while (tasks.hasNext()) {
                            int id = tasks.next();
                            tareasTokensEntrada.add(id);
                            //System.out.println("Tareas " + id);
                        }
                    }
                }
            }
        }
        return tareasTokensEntrada;
    }

    //Función que revisa las tareas que tienen algún token en su entrada
    //Lo guardamos en un HasMap de Tarea y Tokens restantes
    public Integer tareasTokensRestantes(ArrayList<HashMap<TIntHashSet, Integer>> tokens) {
        if (tareasModeloForzado == null) {
            tareasModeloForzado = new HashMap();
        }
        if (tokens != null && tareasTokensEntrada != null) {
            for (int i = 0; i < tokens.size(); i++) {
                HashMap<TIntHashSet, Integer> tareas = tokens.get(i);
                for (Map.Entry<TIntHashSet, Integer> entry : tareas.entrySet()) {
                    //System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                    //Para las tareas que actualmente no tienen tokens
                    if (entry.getValue() == 0) {
                        TIntHashSet subsets = entry.getKey();
                        TIntIterator tasks = subsets.iterator();
                        while (tasks.hasNext()) {
                            int id = tasks.next();
                            //Revisamos si la tarea se encuentra en la lista
                            if (tareasTokensEntrada.contains(id)) {
                                //Si se encuentra le añadimos un token mas
                                if (tareasModeloForzado.get(id) != null) {
                                    int token = tareasModeloForzado.get(id);
                                    token++;
                                    tareasModeloForzado.put(id, token);
                                } else {
                                    tareasModeloForzado.put(id, 1);
                                }
                            }
                        }
                    }
                }
            }
//            System.out.println("******");
//            for (Map.Entry<Integer, Integer> entry : tareasModeloForzado.entrySet()) {
//                System.out.println("Tarea=" + entry.getKey() + ", Tokens Necesarios=" + entry.getValue());
//            }
//            System.out.println("--------");
        }
        return tareasModeloForzado.size();
    }

    public Integer tokenUsados(int task) {
        if (tareasModeloForzado != null) {
            return tareasModeloForzado.get(task);
        } else {
            return null;
        }
    }

    public boolean isModelTask(Integer t) {
        return tareasMODELO.contains(t);
    }

    public void addTareasTraza(InterfazTraza traza, int posProcesado) {
        if (tareasTokensEntrada == null) {
            tareasTokensEntrada = new TIntHashSet();
        }
        //Añadimos las tareas restantes de la traza para contar el nº de tokens restantes
        for (int i = posProcesado; i < traza.tamTrace(); i++) {
            tareasTokensEntrada.add(traza.leerTarea(i));
        }
    }
}
