/*
 * Created on 19.10.2004
 */
package de.renew.engine.events;

import de.renew.net.PlaceInstance;


/**
 * @author Sven Offermann
 */
public class Clearing extends PlaceEvent {
    public Clearing(PlaceInstance pInstance) {
        super(pInstance);
    }

    public String toString() {
        return "Clearing " + getPlaceInstance();
    }
}