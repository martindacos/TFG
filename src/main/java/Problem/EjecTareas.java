package Problem;

import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author marti
 */
public class EjecTareas {

    private Integer tareaOK;
    private Integer tareaINSERT;
    //Posibles tareas a ejecutar en esta instancia de la traza
    private ArrayList<Integer> tareasSkip;
    //Colección de elementos que vamos a guardar del marcado
    ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    //Elementos para la copia del marcado
    private int startPlace;
    private int numOfTokens;
    private int endPlace;
    private TIntHashSet possibleEnabledTasks;

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
        Integer skip = tareasSkip.get(0);
        tareasSkip.remove(0);
        return skip;
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
   
}
