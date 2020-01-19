package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searchqueue.SearchQueue;


class CheckTimeFinder implements Finder {
    private Finder finder;

    CheckTimeFinder(Finder finder) {
        this.finder = finder;
    }

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Only inform aggregated finder if searchable is usable now.
        if (searcher.getEarliestTime() <= SearchQueue.getTime()) {
            finder.found(searcher);
        }
    }

    public boolean isCompleted() {
        return finder.isCompleted();
    }
}