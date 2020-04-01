package de.renew.net;

import de.renew.net.event.FiringEvent;

public class DBNetTransitionInstance extends TransitionInstance {

    private final DBNetTransition transition;

    public DBNetTransitionInstance(DBNetControlLayerInstance netInstance, DBNetTransition transition) {
        super(netInstance, transition);
        this.transition = transition;
    }

    @Override
    synchronized void firingStarted(FiringEvent fe) {
        super.firingStarted(fe);
        transition.performAction();
    }

    @Override
    synchronized void firingComplete(FiringEvent fe) {
        super.firingComplete(fe);
    }
}
