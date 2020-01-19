package de.renew.util;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class EmptyIterator implements Iterator<Object> {
    public final static EmptyIterator INSTANCE = new EmptyIterator();

    public EmptyIterator() {
    }

    public boolean hasNext() {
        return false;
    }

    public Object next() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}