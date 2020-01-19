package de.renew.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An immutable set that comprises integer values within a specified range.
 * The range contains at least one value (when lower and upper bound are
 * the same).
 * <p>
 * <b>New in version 2.0 of this class:</b>
 * The elements of the set are integer-valued instances of {@link Value}.
 * In the previous version the set contained pure {@link Integer} objects,
 * which was not useful in the formalism using this set.
 * </p>
 * @author Olaf Kummer
 * @author Michael Duvigneau
 * @version 2.0
 **/
public final class IntegerRangeSet extends AbstractSet<Value> {

    /**
     * The lower bound of this set's integer range.
     **/
    private final int first;

    /**
     * The upper bound of this set's integer range.
     **/
    private final int last;

    /**
     * Creates a new <code>IntegerRangeSet</code> that comprises all
     * integer values from <code>first</code> to <code>last</code>.
     * The bounds themselves are included in the set.
     *
     * @param first  the lower bound of all values in the set.
     * @param last   the upper bound of all values in the set.
     * @exception IllegalArgumentException  if <code>first</code> greater than <code>last</code>.
     **/
    public IntegerRangeSet(int first, int last) {
        if (last < first) {
            throw new IllegalArgumentException("(lower bound)" + first + " > "
                                               + last + " (upper  bound).");
        }
        this.first = first;
        this.last = last;
    }

    /**
     * {@inheritDoc}
     * @return  the size of the integer range including upper and lower
     *          bounds.
     **/
    public int size() {
        return last - first + 1;
    }

    /**
     * {@inheritDoc}
     * @param o element whose presence in this set is to be tested.
     * @return  <code>true</code> if the given <code>o</code> is an instance
     *          of {@link Value} that represents an {@link Integer} value
     *          within the set range.
     **/
    public boolean contains(Object o) {
        if (o instanceof Value) {
            Value v = (Value) o;
            if (v.value instanceof Integer) {
                int i = v.intValue();
                return first <= i && i <= last;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @return  an immutable <code>Iterator</code> that returns all integer
     *          values in the range from lower bound to upper bound,
     *          starting with the lower bound.
     **/
    public Iterator<Value> iterator() {
        return new Iterator<Value>() {
                private int next = first;
                private boolean hasNext = true;

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public boolean hasNext() {
                    return hasNext;
                }

                public Value next() {
                    if (!hasNext) {
                        throw new NoSuchElementException();
                    }
                    hasNext = (next != last);
                    return new Value(new Integer(next++));
                }
            };
    }
}