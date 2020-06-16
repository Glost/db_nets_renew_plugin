package de.renew.dbnets.datalogic;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.LateExecutable;
import de.renew.unify.Impossible;

public class ActionCallExecutable implements LateExecutable {

    private final ActionCall actionCall;

    public ActionCallExecutable(ActionCall actionCall) {
        this.actionCall = actionCall;
    }

    @Override
    public int phase() {
        return ACTION;
    }

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public void execute(StepIdentifier stepIdentifier) throws Impossible {
//        actionCall.performAction();
    }

    @Override
    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        // TODO: mark for rollback.
    }
}
