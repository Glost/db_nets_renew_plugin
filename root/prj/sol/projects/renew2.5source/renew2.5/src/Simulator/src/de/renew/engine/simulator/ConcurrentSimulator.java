package de.renew.engine.simulator;

public class ConcurrentSimulator extends AbstractConcurrentSimulator {
    public ConcurrentSimulator(boolean wantEventQueueDelay) {
        super(wantEventQueueDelay, true);
    }

    public ConcurrentSimulator() {
        super(true, true);
    }
}