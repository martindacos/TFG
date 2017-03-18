/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Individual.properties;

import gnu.trove.set.hash.TIntHashSet;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author qnoxo
 */
public class IndividualCombUsage {

    private HashMap<Integer,CombinationsUsage> combinationsUsage;

    public IndividualCombUsage(int numOfTasks) {
        this.combinationsUsage = new HashMap<>(numOfTasks);
    }

    public IndividualCombUsage(IndividualCombUsage metric) {
        deepCoopy(metric);
    }
    
    private void deepCoopy(IndividualCombUsage metric){
        this.combinationsUsage = new HashMap<>(metric.combinationsUsage.size());
        for (Map.Entry<Integer, CombinationsUsage> entry : metric.combinationsUsage.entrySet()) {
            this.combinationsUsage.put(entry.getKey(),new CombinationsUsage(entry.getValue()));
        }
    }

    public CombinationsUsage get(int elem){
        return this.combinationsUsage.get(elem);
    }

    public HashMap<Integer,CombinationsUsage> getCombinationsUsage() {
        return combinationsUsage;
    }

    public void setCombinationsUsage(HashMap<Integer, CombinationsUsage> combinationsUsage) {
        this.combinationsUsage = combinationsUsage;
    }
    public void increaseOutputCombUsage(int task, TIntHashSet combination, int amount){
        if (!combination.isEmpty()){
            CombinationsUsage combinationUsage = this.combinationsUsage.get(task);
            if (combinationUsage==null){
                combinationUsage = new CombinationsUsage();
            }
            combinationUsage.increaseOutputArc(combination, amount);
            combinationsUsage.put(task, combinationUsage);
        }
    }
    public void increaseInputCombUsage(int task, TIntHashSet combination, int amount){
        if (!combination.isEmpty()){
            CombinationsUsage combinationUsage = this.combinationsUsage.get(task);
            if (combinationUsage==null){
                combinationUsage = new CombinationsUsage();
            }
            combinationUsage.increaseInputArc(combination, amount);
            combinationsUsage.put(task, combinationUsage);
        }
    }
    
    public void printCombinationsUsage(){
        final int numOfTasks = combinationsUsage.size();
        System.out.println("******* COMBINATIONS USAGE *******");
        for (int indexTask = 0; indexTask < numOfTasks; indexTask++) {
            CombinationsUsage combinationUsage = combinationsUsage.get(indexTask);
            if (combinationUsage!=null){
                for (Map.Entry<TIntHashSet, Integer> entry : combinationUsage.getCombOutputUsage().entrySet()) {
                    TIntHashSet comb = entry.getKey();
                    Integer numRep = entry.getValue();
                    System.out.println(">       "+indexTask+" => "+ comb +" = "+numRep);
                }
                for (Map.Entry<TIntHashSet, Integer> entry : combinationUsage.getCombInputUsage().entrySet()) {
                    TIntHashSet comb = entry.getKey();
                    Integer numRep = entry.getValue();
                    System.out.println(">       "+comb+" => "+ indexTask +" = "+numRep);
                }
            }
        }
    }
    
    
}

