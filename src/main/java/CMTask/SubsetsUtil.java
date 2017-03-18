package CMTask;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author qnoxo
 */
public class SubsetsUtil {

    public static Integer getPos(TIntHashSet subset, Random rnd) {
        final int pos = rnd.nextInt(subset.size());
        TIntIterator iter = subset.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Integer a =  iter.next();
            if (i == pos) {
                return a;
            }
            i++;
        }
        return -1;
    }
    public static Integer getPos(TIntHashSet subset, int pos) {
        TIntIterator iter = subset.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Integer a =  iter.next();
            if (i == pos) {
                return a;
            }
            i++;
        }
        return -1;
    }

    public static int removePos(TIntHashSet subset, Random rnd) {
        final int pos = rnd.nextInt(subset.size());
        TIntIterator iter = subset.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final int elem = iter.next();
            if (i == pos) {
                iter.remove();
                return elem;
            }
            i++;
        }
        return -1;
    }

    public static int removePos(TIntHashSet subset, int pos) {
        TIntIterator iter = subset.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final int elem = iter.next();
            if (i == pos) {
                iter.remove();
                return elem;
            }
            i++;
        }
        return -1;
    }

    public static TIntHashSet getIntersection(TIntHashSet subset, TIntHashSet otherSubset) {
        TIntHashSet intersection = new TIntHashSet(otherSubset);
        intersection.retainAll(subset);
        return intersection;
    }
}

