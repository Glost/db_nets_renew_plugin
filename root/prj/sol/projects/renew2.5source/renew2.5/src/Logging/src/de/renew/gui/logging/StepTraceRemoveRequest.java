/*
 * Created on Aug 30, 2004
 *
 */
package de.renew.gui.logging;

public class StepTraceRemoveRequest {
    private StepTrace stepTrace;
    private StepTraceRepository repository;
    private boolean veto = false;

    public StepTraceRemoveRequest(StepTraceRepository repository,
                                  StepTrace stepTrace) {
        this.stepTrace = stepTrace;
        this.repository = repository;
    }

    public StepTrace getStepTrace() {
        return this.stepTrace;
    }

    public StepTraceRepository getRepository() {
        return this.repository;
    }

    public void veto() {
        this.veto = true;
    }

    public boolean hasVeto() {
        return this.veto;
    }
}