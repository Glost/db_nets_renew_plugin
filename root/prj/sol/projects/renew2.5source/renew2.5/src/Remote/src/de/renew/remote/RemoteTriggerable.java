package de.renew.remote;



/**
 * A remote triggerable is a client object that gets the search
 * proposals forwarded from the triggerable forwarder.
 * @see TriggerableForwarder for details.
 */
public interface RemoteTriggerable {

    /**
     * Trigger a new search because bindings
     * might have appeared or disappeared.
     */
    public void proposeSearch();
}