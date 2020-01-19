package de.renew.engine.searchqueue;

import de.renew.engine.searcher.Searchable;

import java.util.Enumeration;


public interface SearchQueueData {
    public double getTime();

    public void include(Searchable searchable);

    public void exclude(Searchable searchable);

    public Searchable extract();

    public Enumeration<Searchable> elements();

    public int size();
}