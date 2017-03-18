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
public class CombinationsUsage {
    
    
    private final HashMap<TIntHashSet,Integer> arcInputUsage;
    private final HashMap<TIntHashSet,Integer> arcOutputUsage;

    public CombinationsUsage() {
        this.arcInputUsage = new HashMap<>();
        this.arcOutputUsage = new HashMap<>();
    }
    public CombinationsUsage(CombinationsUsage arcUsage) {
        this.arcInputUsage = new HashMap<>();
        for (Map.Entry<TIntHashSet,Integer> entry : arcUsage.arcInputUsage.entrySet()) {
            this.arcInputUsage.put(new TIntHashSet(entry.getKey()),entry.getValue());
        }
        this.arcOutputUsage = new HashMap<>();
        for (Map.Entry<TIntHashSet,Integer> entry : arcUsage.arcOutputUsage.entrySet()) {
            this.arcOutputUsage.put(new TIntHashSet(entry.getKey()),entry.getValue());
        }
    }
    public HashMap<TIntHashSet, Integer> getCombInputUsage() {
        return arcInputUsage;
    }

    public HashMap<TIntHashSet, Integer> getCombOutputUsage() {
        return arcOutputUsage;
    }
    public void increaseInputArc(TIntHashSet comb, Integer valueIncrease){
        Integer value = arcInputUsage.get(comb);
        if (value == null){
            value=0;
        }
        final int total = value+valueIncrease;
        arcInputUsage.put(comb, total);
    }
    
    public void increaseOutputArc(TIntHashSet comb, Integer valueIncrease){
        Integer value = arcOutputUsage.get(comb);
        if (value == null){
            value=0;
        }
        final int total = value+valueIncrease;
        arcOutputUsage.put(comb, total);
    }
    
    public void plus(CombinationsUsage combsUsage, int numRep){
        for (Map.Entry<TIntHashSet,Integer> entry : combsUsage.arcInputUsage.entrySet()) {
            final int total = entry.getValue()*numRep;
            increaseInputArc(entry.getKey(),total);
        }
        for (Map.Entry<TIntHashSet,Integer> entry : combsUsage.arcOutputUsage.entrySet()) {
            final int total = entry.getValue()*numRep;
            increaseOutputArc(entry.getKey(),total);
        }
    }
    
    public boolean isInputUsed (TIntHashSet subset){
        if (this.arcInputUsage.get(subset)!=null){
            return true;
        }else {
            return false;
        }
    }
    public boolean isOutputUsed (TIntHashSet subset){
        if (this.arcOutputUsage.get(subset)!=null){
            return true;
        }else {
            return false;
        }
    }
}

