package CMTask;

import Task.Task;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import java.util.Objects;

/**
 *
 * @author QnOx
 */
public class CMTask {

    private CMSet inputs;
    private CMSet outputs;
    private final Task task;

    public CMTask(Task newTask) {
        this.task = newTask;
        outputs = new CMSet();
        inputs = new CMSet();
    }

    public CMTask(CMTask newTask) {
        this.task = newTask.task;
        outputs = new CMSet(newTask.outputs);
        inputs = new CMSet(newTask.inputs);
    }

    public Task getTask() {
        return task;
    }

    public CMSet getInputs() {
        return this.inputs;
    }

    public CMSet getOutputs() {
        return this.outputs;
    }

    public void setInputs(CMSet inputs) {
        this.inputs = new CMSet(inputs);
    }

    public void setOutputs(CMSet outputs) {
        this.outputs = new CMSet(outputs);
    }

    public int getMatrixID() {
        return this.task.getMatrixID();
    }

    
    @Override
    public String toString() {
        return this.task.toString();
    }
    
    @Override
    public boolean equals(Object arg0){
        if (this == arg0) {
            return true;
        }
        if (arg0 == null) {
            return false;
        }
        if (getClass() != arg0.getClass()) {
            return false;
        } else {
            final CMTask otherTask = (CMTask) arg0;
            if (this.task.equals(otherTask.task)){
                return (this.inputs.equals(otherTask.inputs) 
                        && this.outputs.equals(otherTask.outputs));
            }else{
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.inputs);
        hash = 29 * hash + Objects.hashCode(this.outputs);
        hash = 29 * hash + Objects.hashCode(this.task);
        return hash;
    }
    
    public int numberOutputs() {
//        TIntIterator it = this.getOutputs().getUnionSubsets().iterator();
//        
//        while (it.hasNext()) {
//            System.out.println(it.next());
//        }
        
        return this.getOutputs().getUnionSubsets().size();
    }
}

