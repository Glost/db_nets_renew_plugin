package de.renew.engine.simulator;

import de.renew.engine.common.StepIdentifier;


public class AbstractConcurrentChildSimulator
        extends AbstractConcurrentSimulator {
    private Simulator parentSimulator;

    public AbstractConcurrentChildSimulator(boolean wantEventQueueDelay,
                                            boolean wantConcurrentExecution,
                                            Simulator parentSimulator) {
        super(wantEventQueueDelay, wantConcurrentExecution);
        this.parentSimulator = parentSimulator;
    }

    public Simulator getParentSimulator() {
        return this.parentSimulator;
    }

    @Override
    public StepIdentifier nextStepIdentifier() {
        return getParentSimulator().nextStepIdentifier();
    }

    @Override
    public StepIdentifier currentStepIdentifier() {
        return getParentSimulator().currentStepIdentifier();
    }
}