package es.usc.citius.aligments.problem.movs;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;

import java.util.ArrayList;
import java.util.List;

public class PossibleMovements {

    //Ids of possible movements
    private List<SyncMove> synchronous;
    private List<LogMove> log;
    private List<Integer> model;
    private List<Integer> invisible;

    public PossibleMovements() {
        this.model = new ArrayList<>();
        this.invisible = new ArrayList<>();
        this.synchronous = new ArrayList<>();
        this.log = new ArrayList<>();
    }

    public void setModel(TIntList model) {
        if (model != null) {
            TIntIterator modelIterator = model.iterator();
            while (modelIterator.hasNext()) {
                this.model.add(modelIterator.next());
            }
        }
    }

    public void setInvisible(TIntList invisible) {
        if (invisible != null) {
            TIntIterator invisibleIterator = invisible.iterator();
            while (invisibleIterator.hasNext()) {
                this.invisible.add(invisibleIterator.next());
            }
        }
    }

    public void addLogMovement(LogMove mov) {
        log.add(mov);
    }

    public LogMove getAndDeleteLogMovement() {
        LogMove mov = log.get(0);
        log.remove(0);
        return mov;
    }

    public int getAndDeleteModelMovement() {
        int mov = model.get(0);
        model.remove(0);
        return mov;
    }

    public int getAndDeleteInvisibleMovement() {
        int mov = invisible.get(0);
        invisible.remove(0);
        return mov;
    }

    public void addSyncMovement(SyncMove mov) {
        synchronous.add(mov);
    }

    public SyncMove getAndDeleteSyncMovement() {
        SyncMove mov = synchronous.get(0);
        synchronous.remove(0);
        return mov;
    }
}
