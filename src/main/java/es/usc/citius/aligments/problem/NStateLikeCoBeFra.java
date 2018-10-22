package es.usc.citius.aligments.problem;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TIntArrayList;
import nl.tue.astar.*;
import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.compressor.BitMask;
import nl.tue.storage.hashing.HashCodeProvider;
import nl.tue.storage.hashing.impl.MurMur3HashCodeProvider;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.impl.AbstractPDelegate;

import java.util.ArrayList;
import java.util.List;

import static nl.tue.astar.AStarThread.NOMOVE;

public final class NStateLikeCoBeFra {

    private NStateLikeCoBeFra() {
    }

    public enum StateMoveCoBeFra {
        SYNC, MODEl, LOG, INVISIBLE
    }

    public static final class StateLikeCoBeFra {

        //CoBeFra PHead copy
        public static HashCodeProvider PROVIDER = new MurMur3HashCodeProvider();
        // The maximum size of this.super:             16
        protected final ShortShortMultiset marking; //    8 + 2 * numplaces + 48
        protected final ShortShortMultiset parikh; //     8 + 2 * numactivities + 48
        protected int hashCode; //                  4

        //CoBeFra PRecord copy
        protected final int logMove; //         4 bytes
        protected final int modelMove; //       4 bytes
        protected final int activity; //       4 bytes
        BitMask executed;

        //Ids of possible movements
        List<SyncMove> synchronous;
        List<LogMove> log;
        TIntList model;
        TIntList invisible;


        public StateLikeCoBeFra(AbstractPDelegate<?> delegate, Marking m, XTrace t) {
            marking = new ShortShortMultiset(delegate.numPlaces());
            parikh = new ShortShortMultiset(delegate.numEventClasses());

            for (Place p : m.baseSet()) {
                short i = delegate.getIndexOf(p);
                marking.put(i, m.occurrences(p).shortValue());
            }
            for (XEvent e : t) {
                XEventClass c = delegate.getClassOf(e);
                if (c != null) {
                    short key = delegate.getIndexOf(c);
                    if (key >= 0) {
                        parikh.adjustValue(key, (short) 1);
                    }
                }
            }
            hashCode = PROVIDER.hash(marking, parikh);
            executed = new BitMask(t.size());
            log = new ArrayList<>();
            synchronous = new ArrayList<>();
            model = new TIntArrayList();
            invisible = new TIntArrayList();
            logMove = NOMOVE;
            modelMove = NOMOVE;
            activity = NOMOVE;
        }

        public StateLikeCoBeFra(ShortShortMultiset marking, ShortShortMultiset parikh, int hashCode, BitMask executed
                , int modelMove, int logMove, int activity) {
            this.marking = marking;
            this.parikh = parikh;
            this.hashCode = hashCode;
            this.executed = executed;
            this.log = new ArrayList<>();
            this.synchronous = new ArrayList<>();
            this.model = new TIntArrayList();
            this.invisible = new TIntArrayList();
            this.modelMove = modelMove;
            this.logMove = logMove;
            this.activity = activity;
        }

        protected ShortShortMultiset cloneAndUpdateMarking(AbstractPDelegate<?> delegate, ShortShortMultiset marking,
                                                           short modelMove) {
            ShortShortMultiset newMarking = marking.clone();
            // clone the marking
            short[] in = delegate.getInputOf(modelMove);
            short[] out = delegate.getOutputOf(modelMove);

            for (short place = delegate.numPlaces(); place-- > 0; ) {
                short needed = in[place];
                if (needed != AbstractPDelegate.INHIBITED) {
                    // only adjust the value for non-inhibitor arcs
                    newMarking.adjustValue(place, (short) -needed);
                }
            }

            for (short place = delegate.numPlaces(); place-- > 0; ) {
                short val = newMarking.get(place);
                short produced = out[place];
                if (produced < 0) {
                    // combination or reset arc and regular arc (regular arc may be 0)
                    // first get the actual produced tokens
                    produced = (short) (-(produced + 1));
                    // then account for removing all tokens first
                    produced -= val;
                }
                newMarking.adjustValue(place, produced);
            }
            return newMarking;
        }

        public StateLikeCoBeFra getNextHead(Delegate<? extends Head, ? extends Tail> d, int modelMove, int logMove, int activity) {
            AbstractPDelegate<?> delegate = (AbstractPDelegate<?>) d;

            final ShortShortMultiset newMarking;
            BitMask newExecuted;

            if (modelMove != AStarThread.NOMOVE) {
                newMarking = cloneAndUpdateMarking(delegate, marking, (short) modelMove);
            } else {
                newMarking = marking;
            }

            final ShortShortMultiset newParikh;
            if (logMove != AStarThread.NOMOVE) {
                newParikh = parikh.clone();
                newParikh.adjustValue((short) activity, (short) -1);
                newExecuted = executed.clone();
                newExecuted.set(logMove, true);
            } else {
                newParikh = parikh;
                newExecuted = executed;
            }

            return new StateLikeCoBeFra(newMarking, newParikh, PROVIDER.hash(newMarking, newParikh), newExecuted, modelMove, logMove, activity);
        }

        public TIntList getSynchronousMoves(Delegate<? extends Head, ? extends Tail> d, TIntList enabled, int activity) {
            final AbstractPDelegate<?> delegate = (AbstractPDelegate<?>) d;

            // only consider transitions mapped to activity
            TIntList syncronous = new TIntArrayList();
            TShortList trans = delegate.getTransitions((short) activity);
            TShortIterator it = trans.iterator();
            while (it.hasNext()) {
                int i = it.next();
                if (delegate.isEnabled(i, marking)) {
                    syncronous.add(i);
                }
            }

            return syncronous;
        }

        public TIntList getModelMoves(Delegate<? extends Head, ? extends Tail> delegate) {
            AbstractPDelegate<?> d = (AbstractPDelegate<?>) delegate;
            TIntIterator iterator = d.getEnabledTransitionsChangingMarking(marking).iterator();
            while (iterator.hasNext()) {
                int next = iterator.next();
                Transition t = d.getTransition((short) next);
                if (t.isInvisible()) {
                    invisible.add(next);
                } else {
                    model.add(next);
                }
            }
            return model;
        }

        public boolean isFinal(Delegate<? extends Head, ? extends Tail> d) {
            AbstractPDelegate<?> delegate = (AbstractPDelegate<?>) d;
            return parikh.isEmpty() && delegate.isFinal(marking);
        }

        public ShortShortMultiset getMarking() {
            return marking;
        }

        public ShortShortMultiset getParikhVector() {
            return parikh;
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            return (o != null) && (o instanceof StateLikeCoBeFra) && (((StateLikeCoBeFra) o).marking.equals(marking))
                    && (((StateLikeCoBeFra) o).parikh.equals(parikh));
        }

        public String toString() {
            return "[m:" + marking + "<BR/>p:" + parikh + "]";
        }

        public BitMask getExecuted() {
            return executed;
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

        public int getModelMove() {
            return modelMove;
        }

        public int getLogMove() {
            return logMove;
        }

        public int invisibleSize() {return invisible.size();}

        public int modelSize() {return model.size();}

        public int getActivity() {
            return activity;
        }
    }
}
