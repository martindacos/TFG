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

        private TIntList invisible;

        protected final StateMoveCoBeFra previousMove;

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
            logMove = NOMOVE;
            modelMove = NOMOVE;
            activity = NOMOVE;
            previousMove = null;
        }

        public StateLikeCoBeFra(ShortShortMultiset marking, ShortShortMultiset parikh, int hashCode, BitMask executed
                , int modelMove, int logMove, int activity, StateMoveCoBeFra previousMove) {
            this.marking = marking;
            this.parikh = parikh;
            this.hashCode = hashCode;
            this.executed = executed;
            this.modelMove = modelMove;
            this.logMove = logMove;
            this.activity = activity;
            this.previousMove = previousMove;
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

        public static ShortShortMultiset updateMarking(AbstractPDelegate<?> delegate, ShortShortMultiset marking, short modelMove) {
            // clone the marking
            short[] in = delegate.getInputOf(modelMove);
            short[] out = delegate.getOutputOf(modelMove);

            for (short place = delegate.numPlaces(); place-- > 0; ) {
                short needed = in[place];
                if (needed != AbstractPDelegate.INHIBITED) {
                    // only adjust the value for non-inhibitor arcs
                    marking.adjustValue(place, (short) -needed);
                }
            }

            for (short place = delegate.numPlaces(); place-- > 0; ) {
                short val = marking.get(place);
                short produced = out[place];
                if (produced < 0) {
                    // combination or reset arc and regular arc (regular arc may be 0)
                    // first get the actual produced tokens
                    produced = (short) (-(produced + 1));
                    // then account for removing all tokens first
                    produced -= val;
                }
                marking.adjustValue(place, produced);
            }
            return marking;
        }

        public StateLikeCoBeFra getNextHead(Delegate<? extends Head, ? extends Tail> d, int modelMove, int logMove, int activity,
                                            StateMoveCoBeFra previousMove) {
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

            return new StateLikeCoBeFra(newMarking, newParikh, PROVIDER.hash(newMarking, newParikh), newExecuted, modelMove, logMove, activity, previousMove);
        }

        public TIntList getSynchronousMoves(Delegate<? extends Head, ? extends Tail> d, int activity, ShortShortMultiset marking) {
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

        public TIntList getModelMoves(Delegate<? extends Head, ? extends Tail> delegate, ShortShortMultiset marking) {
            AbstractPDelegate<?> d = (AbstractPDelegate<?>) delegate;
            TIntIterator iterator = d.getEnabledTransitionsChangingMarking(marking).iterator();
            TIntList model = new TIntArrayList();
            invisible = new TIntArrayList();
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

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            return (o != null) && (o instanceof StateLikeCoBeFra) && (((StateLikeCoBeFra) o).marking.equals(marking))
                    && (((StateLikeCoBeFra) o).parikh.equals(parikh));
        }

        @Override
        public String toString() {
            return "[m:" + marking + "<BR/>p:" + parikh + "]";
        }

        public BitMask getExecuted() {
            return executed;
        }

        public int getModelMove() {
            return modelMove;
        }

        public int getLogMove() {
            return logMove;
        }

        public int getActivity() {
            return activity;
        }

        public StateMoveCoBeFra getPreviousMove() {
            return previousMove;
        }

        public TIntList getInvisible() {
            return invisible;
        }
    }
}
