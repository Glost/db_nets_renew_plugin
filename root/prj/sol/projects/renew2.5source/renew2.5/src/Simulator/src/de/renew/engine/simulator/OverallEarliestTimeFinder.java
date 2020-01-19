package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searchqueue.SearchQueue;


class OverallEarliestTimeFinder implements Finder {
    private Finder finder;
    private double overallEarliestTime = Double.POSITIVE_INFINITY;

    OverallEarliestTimeFinder(Finder finder) {
        this.finder = finder;
    }

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // We must prepare a reinsertion of this transition
        // into the list of activated elements, regardless whether this
        // transition instance is actually selected by the binder.
        double possibleTime = searcher.getEarliestTime();
        double currentTime = SearchQueue.getTime();
        if (possibleTime < currentTime) {
            possibleTime = currentTime;
        }


        // Keep track of the earliest time stamp found.
        // In theory, no searchable should be checked twice.
        if (possibleTime < overallEarliestTime) {
            overallEarliestTime = possibleTime;
        }

        // Only inform aggregated finder if the searchable is usable now.
        if (possibleTime == currentTime) {
            finder.found(searcher);
        }
    }

    public boolean isCompleted() {
        return finder.isCompleted();
    }

    public double getOverallEarliestTime() {
        return overallEarliestTime;
    }

    /**
     * Insert the given searchable into the {@link SearchQueue}
     * at that point of time that is currently determined
     * as the earliest overall firing time by this finder.
     *
     * @param searchable the searchable to insert
     */
    public void insertIntoSearchQueue(Searchable searchable) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (overallEarliestTime < Double.POSITIVE_INFINITY) {
            SearchQueue.include(searchable, overallEarliestTime);
        }
    }
}