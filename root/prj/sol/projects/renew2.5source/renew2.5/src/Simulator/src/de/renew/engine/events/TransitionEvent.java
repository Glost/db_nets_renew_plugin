/*
 * Created on 07.12.2004
 *
 */
package de.renew.engine.events;

import de.renew.net.TransitionInstance;


/**
 * @author Sven Offermann
 *
 */
public class TransitionEvent extends NetEvent {
    private TransitionInstance tInstance;

    public TransitionEvent(TransitionInstance tInstance) {
        super(tInstance.getNetInstance());

        this.tInstance = tInstance;
    }

    /**
     * @return Returns the pInstance.
     */
    public TransitionInstance getTransitionInstance() {
        return this.tInstance;
    }
}