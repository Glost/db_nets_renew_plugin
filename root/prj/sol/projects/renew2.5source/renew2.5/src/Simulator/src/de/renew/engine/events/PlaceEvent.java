/*
 * Created on 07.12.2004
 *
 */
package de.renew.engine.events;

import de.renew.net.PlaceInstance;


/**
 * @author Sven Offermann
 *
 */
public class PlaceEvent extends NetEvent {
    private PlaceInstance pInstance;

    public PlaceEvent(PlaceInstance pInstance) {
        super(pInstance.getNetInstance());

        this.pInstance = pInstance;
    }

    /**
     * @return Returns the pInstance.
     */
    public PlaceInstance getPlaceInstance() {
        return pInstance;
    }
}