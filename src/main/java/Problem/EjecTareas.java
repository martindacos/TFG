package Problem;

import domainLogic.workflow.algorithms.geneticMining.CMTask.CMSet;
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

    private Integer tareaOK;
    private Integer tareaINSERT;
    //Posibles tareas a ejecutar en esta instancia de la traza
    private ArrayList<Integer> tareasSkip;
    private HashMap<Integer, Integer> tareasArtificiales;
    private Integer tareaArtificialActual;

    //Colección de elementos que vamos a guardar del marcado
    ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    //Elementos para la copia del marcado
    private int startPlace;
    private int numOfTokens;
    private int endPlace;
    private TIntHashSet possibleEnabledTasks;

    TIntHashSet tareasTokensEntrada;

    public EjecTareas() {
    }

    public ArrayList<Integer> getTareasSkip() {
        return tareasSkip;
    }

    public Integer getTareaINSERT() {
        return tareaINSERT;
    }

    public Integer getTareaOK() {
        return tareaOK;
    }

    public void clear() {
        this.tareaOK = null;
        tareasSkip = new ArrayList<Integer>();
        tareasArtificiales = new HashMap<>();
        tareaArtificialActual = 0;
    }

    public void anadirOk(Integer a) {
        tareaOK = a;
    }

    public void anadirInsert(Integer a) {
        tareaINSERT = a;
    }

    public void anadirSkip(Integer a) {
        tareasSkip.add(a);
    }

    public Integer leerTareaSkip() {
        if (tareasSkip != null && !tareasSkip.isEmpty()) {
            Integer skip = tareasSkip.get(0);
            tareasSkip.remove(0);
            return skip;
        } else {
            return null;
        }
    }

    public Integer leerTareaArtificial() {
        Integer artificial = null;
        if (tareasArtificiales != null && !tareasArtificiales.isEmpty()) {
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : tareasArtificiales.entrySet()) {
                if (i == tareaArtificialActual) {
                    artificial = entry.getKey();
                }
                i++;
            }
            tareaArtificialActual++;
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

    //Función que revisa cuantos tokens son necesarios para ejecutar la tarea forzada
    //Lo guardamos en un HasMap de Tarea y Tokens restantes
    public void anadirTareaForzada(Integer a) {
        if (tareasArtificiales == null) {
            tareasArtificiales = new HashMap();
        }
        tareasArtificiales.put(a, 0);
    }

    //Función que revisa las tareas que tienen algún token en su entrada
    public TIntHashSet tareasTokensEntrada() {
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
    public Integer tareasTokensRestantes() {
        if (tareasArtificiales == null) {
            tareasArtificiales = new HashMap();
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
                                if (tareasArtificiales.get(id) != null) {
                                    int token = tareasArtificiales.get(id);
                                    token++;
                                    tareasArtificiales.put(id, token);
                                } else {
                                    tareasArtificiales.put(id, 1);
                                }
                            }
                        }
                    }
                }
            }
//            System.out.println("******");
//            for (Map.Entry<Integer, Integer> entry : tareasArtificiales.entrySet()) {
//                System.out.println("Tarea=" + entry.getKey() + ", Tokens Necesarios=" + entry.getValue());
//            }
//            System.out.println("--------");
        }
        return tareasArtificiales.size();
    }

    public Integer tokenUsados(int task) {
        if (tareasArtificiales != null) {
            return tareasArtificiales.get(task);
        } else {
            return null;
        }
    }

    public void setTareasArtificiales(HashMap<Integer, Integer> tareasArtificiales) {
        this.tareasArtificiales = tareasArtificiales;
    }
}
