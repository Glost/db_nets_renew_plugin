package de.renew.unify;

import java.util.Iterator;


public class ListIterator implements Iterator<Object> {
    private Object current;

    public ListIterator(List list) {
        current = list;
    }

    private static boolean hasMoreElements(Object list) {
        return list instanceof List && !((List) list).isNull();
    }

    public boolean hasNext() {
        return hasMoreElements(current);
    }

    public Object next() {
        List list = (List) current;


        // has to be a non-empty List, otherwise hasMoreElements()==false
        current = list.tail();
        return list.head();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Object getOpenTail() {
        if (hasNext()) {
            throw new IllegalStateException("getOpenTail() called before ListEnumeration was over.");
        }
        if (current instanceof List) {
            return null;
        } else {
            return current;
        }
    }
}