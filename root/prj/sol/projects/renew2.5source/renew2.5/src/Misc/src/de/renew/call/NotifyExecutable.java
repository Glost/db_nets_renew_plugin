package de.renew.call;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;

import de.renew.unify.Tuple;


class NotifyExecutable implements LateExecutable {
    SynchronisationRequest synchronisation;
    Tuple parameters;

    NotifyExecutable(SynchronisationRequest synchronisation, Tuple parameters) {
        this.synchronisation = synchronisation;
        this.parameters = parameters;
    }

    public int phase() {
        return COMPLETION_NOTIFY;
    }

    // I'll return quickly.
    public boolean isLong() {
        return false;
    }

    public void execute(StepIdentifier stepIdentifier) {
        synchronisation.resultParameters = parameters;
        synchronisation.resultSemaphor.V();
    }

    public void executeAfterException(StepIdentifier stepIdentifier,
                                      Throwable throwable) {
        // This should not happen, strictly speaking.
        // We do not perform any actions in order to
        // enable the user to detect this unusual situation.
    }
}