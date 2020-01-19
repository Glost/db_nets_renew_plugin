package de.renew.net;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.FiringEvent;


public class FiringStartExecutable implements LateExecutable {
    private FiringEvent event;

    FiringStartExecutable(FiringEvent event) {
        this.event = event;
    }

    public int phase() {
        return START;
    }

    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        event.getTransitionInstance().firingStarted(event);
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // This should not happen, but ignore the exception.
        execute(stepIdentifier);
    }
}