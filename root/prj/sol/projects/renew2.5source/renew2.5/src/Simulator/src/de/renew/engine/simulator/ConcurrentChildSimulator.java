package de.renew.engine.simulator;

public class ConcurrentChildSimulator extends AbstractConcurrentChildSimulator {
    public ConcurrentChildSimulator(boolean wantEventQueueDelay,
                                    Simulator parentSimulator) {
        super(wantEventQueueDelay, true, parentSimulator);
    }

    public ConcurrentChildSimulator(Simulator parentSimulator) {
        super(true, true, parentSimulator);
    }
}