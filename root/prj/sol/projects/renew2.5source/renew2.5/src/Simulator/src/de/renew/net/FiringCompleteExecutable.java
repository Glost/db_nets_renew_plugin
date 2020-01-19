package de.renew.net;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.event.FiringEvent;


public class FiringCompleteExecutable implements LateExecutable {
    private FiringEvent event;

    FiringCompleteExecutable(FiringEvent event) {
        this.event = event;
    }

    public int phase() {
        return END;
    }

    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        event.getTransitionInstance().firingComplete(event);
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Proceed as normal.
        execute(stepIdentifier);
    }
}