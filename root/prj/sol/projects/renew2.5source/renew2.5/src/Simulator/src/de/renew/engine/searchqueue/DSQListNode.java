package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;


class DSQListNode {
    DSQListNode prev;
    DSQListNode next;
    Searchable elem;

    DSQListNode(Searchable elem) {
        prev = this;
        next = this;
        this.elem = elem;
    }
}