package de.renew.net.event;

import de.renew.net.PlaceInstance;


public class TokenEvent extends PlaceEvent {

    /** This event occurs when a single token (contained in the
     *  event object) is put into the corresponding PlaceInstance.
     */


    /** The token that has been added or removed. */
    protected Object token;

    /** Constructs a new TokenEvent using the given PlaceInstance
     *  as the event source and the given token.
     */
    public TokenEvent(PlaceInstance instance, Object token) {
        super(instance);
        this.token = token;
    }

    /** Returns the token that has been added or removed. */
    public Object getToken() {
        return token;
    }
}