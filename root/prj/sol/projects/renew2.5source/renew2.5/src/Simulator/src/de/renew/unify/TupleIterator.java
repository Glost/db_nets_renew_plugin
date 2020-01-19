package de.renew.unify;

import java.util.Iterator;


public class TupleIterator implements Iterator<Object> {
    private Tuple tuple;
    private int index = 0;

    public TupleIterator(Tuple tuple) {
        this.tuple = tuple;
    }

    public boolean hasNext() {
        return index < tuple.getArity();
    }

    public Object next() {
        return tuple.getComponent(index++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}