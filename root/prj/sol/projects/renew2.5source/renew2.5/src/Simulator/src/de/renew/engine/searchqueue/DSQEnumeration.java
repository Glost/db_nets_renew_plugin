package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;

import java.util.Enumeration;
import java.util.NoSuchElementException;


class DSQEnumeration implements Enumeration<Searchable> {
    private DSQListNode current;
    private DSQListNode start;

    DSQEnumeration(DSQListNode start) {
        current = start;
        this.start = start;
    }

    public boolean hasMoreElements() {
        return current != null;
    }

    public synchronized Searchable nextElement() throws NoSuchElementException {
        if (current == null) {
            throw new NoSuchElementException();
        }
        Searchable result = current.elem;
        current = current.next;
        if (current == start) {
            current = null;
        }
        return result;
    }
}