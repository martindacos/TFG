package CMTask;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

/**
 * @author QnOx
 */
public class CMSet implements List<TIntHashSet> {

    /*
     * need a lot of random access, so, AbstracSet it is not an option.
     */
    private final List<TIntHashSet> set;

    public CMSet() {
        this.set = new ArrayList<>();
    }

    public CMSet(CMSet set) {
        this();
        deepCopy(set);
    }

    private void deepCopy(List<TIntHashSet> set) {
        for (TIntHashSet subset : set) {
            add(new TIntHashSet(subset));
        }
    }

    @Override
    public TIntHashSet get(int index) {
        return this.set.get(index);
    }

    public boolean contains(Integer element) {
        for (TIntHashSet subset : set) {
            if (subset.contains(element)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(TIntHashSet subset) {
        if (subset != null && !subset.isEmpty() && !this.set.contains(subset)) {
            return this.set.add(subset);
        }
        return false;
    }

    @Override
    public int size() {
        return this.set.size();
    }

    public int subsetsSize() {
        int totalSize = 0;
        for (TIntHashSet subset : set) {
            totalSize += subset.size();
        }
        return totalSize;
    }

    @Override
    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    @Override
    public boolean remove(Object set) {
        return this.set.remove(set);
    }

    @Override
    public void clear() {
        this.set.clear();
    }

    @Override
    public String toString() {
        return this.set.toString();
    }

    public TIntHashSet getUnionSubsets() {
        TIntHashSet tasksInputsSet = new TIntHashSet();
        for (TIntHashSet subset : this.set) {
            tasksInputsSet.addAll(subset);
        }
        return tasksInputsSet;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.set);
        return hash;
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<TIntHashSet> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return toArray(a);
    }

    public TIntList getAll() {
        TIntList all = new TIntArrayList();
        for (TIntHashSet subset : set) {
            all.addAll(subset);
        }
        return all;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TIntHashSet> c) {
        for (TIntHashSet subset : c) {
            add(subset);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends TIntHashSet> c) {
        return set.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public TIntHashSet set(int index, TIntHashSet element) {
        return set.set(index, element);
    }

    @Override
    public void add(int index, TIntHashSet element) {
        this.set.add(index, element);
    }

    @Override
    public TIntHashSet remove(int index) {
        return set.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return set.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return set.lastIndexOf(o);
    }

    @Override
    public ListIterator<TIntHashSet> listIterator() {
        return set.listIterator();
    }

    @Override
    public ListIterator<TIntHashSet> listIterator(int index) {
        return set.listIterator();
    }

    @Override
    public List<TIntHashSet> subList(int fromIndex, int toIndex) {
        return set.subList(fromIndex, toIndex);
    }

    /*
     * performance:
     * containsAll=nope, it compares ALL
     * removeAll=nope +1 for creating a new subset
     * this method= when it fails, stop. Worst case scenario= o(n), like containsAll
     */
    @Override
    public boolean equals(Object arg0) {
        if (this == arg0) {
            return true;
        }
        if (arg0 == null) {
            return false;
        }
        if (getClass() != arg0.getClass()) {
            return false;
        } else {
            final CMSet otherSet = (CMSet) arg0;
            if (this.set.size() != otherSet.set.size()) {
                return false;
            } else {
                for (TIntHashSet currentSubset : set) {
                    if (!otherSet.set.contains(currentSubset)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

