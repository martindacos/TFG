package Problem;

import domainLogic.workflow.Task.Task;
import domainLogic.workflow.algorithms.geneticMining.fitness.parser.marking.CMMarking;
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
    private ArrayList<Integer> execute;
    private Integer skip;
    //Colección de elementos que vamos a guardar del marcado
    ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    private int startPlace;
    private int numOfTokens;
    private int endPlace;
    private TIntHashSet possibleEnabledTasks;

    public EjecTareas() {
    }

    public ArrayList<Integer> getExecute() {
        return execute;
    }

    public Integer getSkip() {
        return skip;
    }

    public Integer getTareaINSERT() {
        return tareaINSERT;
    }

    public Integer getTareaOK() {
        return tareaOK;
    }

    public void clear() {
        this.tareaOK = null;
        execute = new ArrayList<Integer>();
        this.skip = null;
    }

    public void anadirOk(Integer a) {
        tareaOK = a;
    }

    public void anadirINSERT(Integer a) {
        tareaINSERT = a;
    }
        
    public void anadirExecute(Integer a) {
        execute.add(a);
    }
    
    public Integer leerTareaExecute() {
        Integer skip = execute.get(0);
        //System.out.println("leerTareaExecute " + skip.getId() +" "+ skip.getMatrixID());
        execute.remove(0);
        return skip;
    }

    public ArrayList<HashMap<TIntHashSet, Integer>> getTokens() {
        System.out.println("GET: " + tokens);
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
        //this.tokens = (ArrayList<HashMap<TIntHashSet, Integer>>) tokens.clone();
        this.tokens = tokens;
        System.out.println("SET: " + this.tokens);
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
