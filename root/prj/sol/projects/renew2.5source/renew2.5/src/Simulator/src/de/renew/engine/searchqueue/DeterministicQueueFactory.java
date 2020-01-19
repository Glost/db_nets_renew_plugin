package de.renew.engine.searchqueue;

import de.renew.engine.simulator.SimulationThreadPool;


public class DeterministicQueueFactory implements SearchQueueFactory {
    public SearchQueueData makeQueue(double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new DeterministicSearchQueue(time);
    }
}