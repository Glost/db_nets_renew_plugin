package de.renew.remote;



/**
 * This interface allows the receipt of not-really-local
 * update events.
 * <p>
 * There does not exist a generic event producer as
 * currently the single possible event producer is a
 * <code>RemoteEventForwarder</code> instance. See its
 * description for more information about the usage.
 * </p>
 *
 * @author Olaf Kummer, Michael Duvigneau
 *
 * @see RemoteEventForwarder
 * @see RemoteEventListener
 */
public interface EventListener {

    /**
     * Receives the event message.
     */
    public void update();
}