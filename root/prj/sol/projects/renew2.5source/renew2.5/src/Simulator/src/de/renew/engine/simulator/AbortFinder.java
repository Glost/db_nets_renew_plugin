package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;


public class AbortFinder implements Finder {
    private boolean abortRequested = false;
    private Finder finder;

    public AbortFinder(Finder finder) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.finder = finder;
    }

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        finder.found(searcher);
    }

    public void abortSearch() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        abortRequested = true;
    }

    public boolean isCompleted() {
        return abortRequested || finder.isCompleted();
    }
}