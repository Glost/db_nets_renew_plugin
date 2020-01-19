/*
 * Created on 19.10.2004
 */
package de.renew.engine.events;

import de.renew.net.PlaceInstance;


/**
 * @author Sven Offermann
 */
public class QuicklyTesting extends PlaceEvent {
    private Object token;

    public QuicklyTesting(Object token, PlaceInstance pInstance) {
        super(pInstance);

        this.token = token;
    }

    public String toString() {
        return "Quickly Testing " + token + " in " + getPlaceInstance();
    }

    /**
     * @return Returns the token.
     */
    public Object getToken() {
        return token;
    }
}