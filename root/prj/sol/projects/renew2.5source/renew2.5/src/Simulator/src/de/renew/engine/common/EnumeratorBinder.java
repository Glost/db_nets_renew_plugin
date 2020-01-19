package de.renew.engine.common;

import de.renew.engine.searcher.TriggerableCollection;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.unify.Variable;

import java.util.Collection;


public class EnumeratorBinder extends AssignBinder {
    private EnumeratorOccurrence enumerator;

    EnumeratorBinder(EnumeratorOccurrence enumerator, boolean checkBound,
                     Variable variable) {
        super(variable, checkBound);
        this.enumerator = enumerator;
    }

    public final void lock() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    public final void unlock() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    public Collection<?extends Object> getCandidates(Object pattern) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return enumerator.getCandidates(pattern);
    }

    public TriggerableCollection getTriggerables() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return null;
    }
}