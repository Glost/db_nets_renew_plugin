package de.renew.net;

public class DBNetTransitionInstance extends TransitionInstance {

    private final DBNetTransition transition;

    public DBNetTransitionInstance(DBNetControlLayerInstance netInstance, DBNetTransition transition) {
        super(netInstance, transition);
        this.transition = transition;
    }
}
