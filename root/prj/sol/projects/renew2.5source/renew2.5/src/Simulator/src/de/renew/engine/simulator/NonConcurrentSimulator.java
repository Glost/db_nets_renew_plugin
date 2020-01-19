package de.renew.engine.simulator;

public class NonConcurrentSimulator extends AbstractConcurrentSimulator {
    public NonConcurrentSimulator(boolean wantEventQueueDelay) {
        super(wantEventQueueDelay, false);
    }

    public NonConcurrentSimulator() {
        super(true, false);
    }
}