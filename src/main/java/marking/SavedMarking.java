/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marking;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author qnoxo
 */
public class SavedMarking {

    public int indexStartTask;
    public int missingTokens;
    public int enabledActivities;
    public int traceParsedActivities;
    public ArrayList<HashMap<TIntHashSet, Integer>> tokens;
    public int startPlace;
    public int numOfTokens;
    public int endPlace;
    public TIntHashSet possibleEnabledTasks;
    public int patternSize;

    public SavedMarking(int indexStartTask, int missingTokens, int enabledActivities, int traceParsedActivities, ArrayList<HashMap<TIntHashSet, Integer>> tokensInput, int startPlace, int numOfTokens, int endPlace, TIntHashSet possibleEnabledTasks, int patternSize) {
        this.indexStartTask = indexStartTask;
        this.missingTokens = missingTokens;
        this.enabledActivities = enabledActivities;
        this.traceParsedActivities = traceParsedActivities;
        this.startPlace = startPlace;
        this.numOfTokens = numOfTokens;
        this.endPlace = endPlace;
        this.possibleEnabledTasks = new TIntHashSet(possibleEnabledTasks);
        this.tokens = new ArrayList<>();
        for (HashMap<TIntHashSet, Integer> mapper : tokensInput) {
            this.tokens.add(new HashMap<>(mapper));
        }
        this.patternSize=patternSize;
    }
}

