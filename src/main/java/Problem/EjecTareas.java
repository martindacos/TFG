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

    private Task tareaOK;
    private Task tareaINSERT;
    //Posibles tareas a ejecutar en esta instancia de la traza
    private ArrayList<Task> execute;
    private Task skip;
    //Colección de elementos que vamos a guardar del marcado
    ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    private int startPlace;
    private int numOfTokens;
    private int endPlace;
    private TIntHashSet possibleEnabledTasks;

    public EjecTareas() {
    }

    public ArrayList<Task> getExecute() {
        return execute;
    }

    public Task getSkip() {
        return skip;
    }

    public Task getTareaINSERT() {
        return tareaINSERT;
    }

    public Task getTareaOK() {
        return tareaOK;
    }

    public void clear() {
        this.tareaOK = null;
        execute = new ArrayList<Task>();
        this.skip = null;
    }

    public void anadirOk(Task a) {
        Task nueva = new Task(a);
        tareaOK = nueva;
    }

    public void anadirINSERT(Task a) {
        Task nueva = new Task(a);
        tareaINSERT = nueva;
    }
        
    public void anadirExecute(Task a) {
        Task nueva = new Task(a);
        execute.add(nueva);
    }
    
    public Task leerTareaExecute() {
        skip = new Task(execute.get(0));
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
