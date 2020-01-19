package de.renew.engine.searchqueue;

import de.renew.engine.simulator.SimulationThreadPool;


public class RandomQueueFactory implements SearchQueueFactory {
    public SearchQueueData makeQueue(double time) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new RandomSearchQueue(time);
    }
}