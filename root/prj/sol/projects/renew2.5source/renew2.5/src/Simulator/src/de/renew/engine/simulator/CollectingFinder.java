package de.renew.engine.simulator;

import de.renew.engine.searcher.Finder;
import de.renew.engine.searcher.Searcher;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A collecting finder lists all bindings that
 * are found by a searcher.
 */
class CollectingFinder implements Finder {
    private Collection<Binding> bindings = new ArrayList<Binding>();

    public void found(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        bindings.add(new Binding(searcher));
    }

    // I will never be satisfied with the bindings that I have collected.
    public boolean isCompleted() {
        return false;
    }

    public Collection<Binding> bindings() {
        return bindings;
    }
}