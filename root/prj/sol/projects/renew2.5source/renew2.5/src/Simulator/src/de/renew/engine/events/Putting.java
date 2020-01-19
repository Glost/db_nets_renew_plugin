/*
 * Created on 19.10.2004
 */
package de.renew.engine.events;

import de.renew.net.PlaceInstance;


/**
 * @author Sven Offermann
 */
public class Putting extends PlaceEvent {
    private Double time = null;
    private Object token;

    public Putting(Object token, PlaceInstance pInstance, double time) {
        super(pInstance);

        this.token = token;
        this.time = new Double(time);
    }

    public Putting(Object token, PlaceInstance pInstance) {
        super(pInstance);

        this.token = token;
    }

    public String toString() {
        return "Putting " + token + " into " + getPlaceInstance();
    }

    /**
     * @return Returns the token.
     */
    public Object getToken() {
        return token;
    }

    /**
     * @return Returns the time or null if the time is not set.
     */
    public Double getTime() {
        return this.time;
    }
}