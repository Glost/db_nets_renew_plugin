/*
 * Created on 19.10.2004
 */
package de.renew.engine.events;

import de.renew.net.PlaceInstance;


/**
 * @author Sven Offermann
 */
public class Removing extends PlaceEvent {
    private Object token;

    public Removing(Object token, PlaceInstance pInstance) {
        super(pInstance);

        this.token = token;
    }

    public String toString() {
        return "Removing " + token + " in " + getPlaceInstance();
    }

    /**
     * @return Returns the token.
     */
    public Object getToken() {
        return token;
    }
}