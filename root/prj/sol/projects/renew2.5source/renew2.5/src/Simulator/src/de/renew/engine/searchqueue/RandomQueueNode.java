package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;


class RandomQueueNode {
    int pos;
    final Searchable searchable;

    RandomQueueNode(int pos, Searchable searchable) {
        this.pos = pos;
        this.searchable = searchable;
    }
}