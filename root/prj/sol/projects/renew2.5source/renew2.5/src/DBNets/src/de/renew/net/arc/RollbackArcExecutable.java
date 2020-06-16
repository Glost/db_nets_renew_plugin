package de.renew.net.arc;

import de.renew.engine.common.StepIdentifier;
import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;
import de.renew.unify.Variable;

public class RollbackArcExecutable extends OutputArcExecutable {

    public RollbackArcExecutable(PlaceInstance pInstance,
                                 TransitionInstance tInstance,
                                 Variable tokenVar,
                                 Variable timeVar,
                                 boolean trace) {
        super(pInstance, tInstance, tokenVar, timeVar, trace);
    }

    @Override
    public void execute(StepIdentifier stepIdentifier) {
        super.execute(stepIdentifier); // TODO: if...
    }
}
