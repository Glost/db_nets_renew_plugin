package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;


class EnablednessFinder implements Finder {
    private boolean bindingFound = false;

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        bindingFound = true;
    }

    // Return true if no other bindings should be tried.
    // After returning true once, the method must return true forever.
    public boolean isCompleted() {
        return bindingFound;
    }

    // Return whether a binding was found so far.
    // This might behave different from isCompleted,
    // if we decide to make the search interruptable.
    public boolean isEnabled() {
        return bindingFound;
    }
}