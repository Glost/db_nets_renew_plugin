package de.uni_hamburg.fs;

import collections.CollectionEnumeration;

import java.util.NoSuchElementException;


public class EmptyEnumeration implements CollectionEnumeration {
    public final static EmptyEnumeration INSTANCE = new EmptyEnumeration();

    public EmptyEnumeration() {
    }

    public boolean hasMoreElements() {
        return false;
    }

    public Object nextElement() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    public boolean corrupted() {
        return false;
    }

    public int numberOfRemainingElements() {
        return 0;
    }
}