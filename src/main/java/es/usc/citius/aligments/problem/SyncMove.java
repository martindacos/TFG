package es.usc.citius.aligments.problem;

public class SyncMove {
    private int modelMove;
    private int movedEvent;
    private int activity;

    public SyncMove(int modelMove, int movedEvent, int activity) {
        this.modelMove = modelMove;
        this.movedEvent = movedEvent;
        this.activity = activity;
    }

    public int getActivity() {
        return activity;
    }

    public int getModelMove() {
        return modelMove;
    }

    public int getMovedEvent() {
        return movedEvent;
    }
}
