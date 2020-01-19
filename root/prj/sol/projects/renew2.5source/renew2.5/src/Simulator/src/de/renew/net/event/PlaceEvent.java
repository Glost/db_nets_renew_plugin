package de.renew.net.event;

import de.renew.net.PlaceInstance;


public class PlaceEvent extends NetEvent {

    /** A general class for Place-related net events. */
    /** Constructs a PlaceEvent using the given PlaceInstance as
     *  the event source.
     */
    public PlaceEvent(PlaceInstance instance) {
        super(instance);
    }

    /** Returns the event source as a PlaceInstance. */
    public PlaceInstance getPlaceInstance() {
        return (PlaceInstance) source;
    }
}