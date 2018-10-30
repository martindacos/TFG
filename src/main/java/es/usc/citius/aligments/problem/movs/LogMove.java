package es.usc.citius.aligments.problem.movs;

public class LogMove {
    private int movedEvent;
    private int activity;

    public LogMove(int movedEvent, int activity) {
        this.movedEvent = movedEvent;
        this.activity = activity;
    }

    public int getMovedEvent() {
        return movedEvent;
    }

    public int getActivity() {
        return activity;
    }
}
