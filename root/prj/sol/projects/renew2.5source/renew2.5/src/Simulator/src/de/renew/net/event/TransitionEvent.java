package de.renew.net.event;

import de.renew.net.TransitionInstance;


public abstract class TransitionEvent extends NetEvent {

    /** A general class for Transition-related net events. */
    protected TransitionEvent(TransitionInstance instance) {
        super(instance);
    }

    /** Returns the event source as a TransitionInstance. */
    public TransitionInstance getTransitionInstance() {
        return (TransitionInstance) source;
    }
}