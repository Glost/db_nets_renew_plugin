package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searchqueue.SearchQueue;

import java.util.Enumeration;
import java.util.Hashtable;


class DelayingInsertionFinder implements Finder {
    private Finder finder;
    private Searchable searchable;
    private Hashtable<Searchable, Double> times = new Hashtable<Searchable, Double>();

    DelayingInsertionFinder(Finder finder) {
        this.finder = finder;
    }

    void setSearchable(Searchable searchable) {
        this.searchable = searchable;
    }

    private double getTimeFor(Searchable searchable) {
        return (times.get(searchable)).doubleValue();
    }

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // We must prepare a reinsertion of this transition
        // into the list of activated elements, regardless whether this
        // transition instance is actually selected by the binder.
        double time = searcher.getEarliestTime();


        // Keep track of the earliest time stamp found.
        // In theory, no searchable should be checked twice.
        double oldTime;
        if (times.containsKey(searchable)) {
            oldTime = getTimeFor(searchable);
        } else {
            oldTime = Double.POSITIVE_INFINITY;
        }
        if (time < oldTime) {
            // Make suggested search time earlier.
            times.put(searchable, new Double(time));
        }

        // Only inform aggregated finder if searchable is usable now.
        if (time <= SearchQueue.getTime()) {
            finder.found(searcher);
        }
    }

    public boolean isCompleted() {
        return finder.isCompleted();
    }

    public void flushIntoSearchQueue() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Enumeration<Searchable> enumeration = times.keys();
        while (enumeration.hasMoreElements()) {
            Searchable searchable = enumeration.nextElement();
            double time = getTimeFor(searchable);
            SearchQueue.include(searchable, time);
        }
    }
}