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
public class Firing extends TransitionEvent {
    public Firing(TransitionInstance tInstance) {
        super(tInstance);
    }

    public String toString() {
        return "Firing " + getTransitionInstance();
    }
}