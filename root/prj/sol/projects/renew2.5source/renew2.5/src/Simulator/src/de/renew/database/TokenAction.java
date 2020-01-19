package de.renew.database;

import de.renew.net.NetElementID;
import de.renew.net.PlaceInstance;


public class TokenAction {
    private PlaceInstance placeInstance;
    private Object token;
    private String tokenID;
    private double time;

    TokenAction(PlaceInstance placeInstance, Object token, double time) {
        this.placeInstance = placeInstance;
        this.token = token;
        this.time = time;


        // Precalculate token ID. It will be needed anyway.
        this.tokenID = placeInstance.getTokenID(token);
    }

    public PlaceInstance getPlaceInstance() {
        return placeInstance;
    }

    /**
     * Returns the moved token.
     *
     * @see java.lang.Object
     * @see de.renew.util.Value
     *
     * @return the token, possibly wrapped in a <code>Value</code> object.
     */
    public Object getToken() {
        return token;
    }

    public String getTokenID() {
        return tokenID;
    }

    public String getNetID() {
        return placeInstance.getNetInstance().getID();
    }

    public NetElementID getPlaceID() {
        return placeInstance.getPlace().getID();
    }

    public String getPlaceName() {
        return placeInstance.getPlace().toString();
    }

    /**
     * Returns the time stamp of the moved token.
     *
     * @return a <code>double</code> valued time stamp
     */
    public double getTime() {
        return time;
    }
}