package de.renew.engine.simulator;

public class NonConcurrentChildSimulator
        extends AbstractConcurrentChildSimulator {
    public NonConcurrentChildSimulator(boolean wantEventQueueDelay,
                                       Simulator parentSimulator) {
        super(wantEventQueueDelay, false, parentSimulator);
    }

    public NonConcurrentChildSimulator(Simulator parentSimulator) {
        super(true, false, parentSimulator);
    }
}