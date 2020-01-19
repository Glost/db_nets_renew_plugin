package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This interface allows the receipt of remote events.
 * The listener should be registered at a remote event
 * producer.
 *
 * <p>
 * Remote events are simplified versions of local place or
 * transition events which do not carry any additional
 * information. The listener has to request the current
 * state of the observed object from an accessor object
 * after receiving the event.
 * As the event source is not included in the event message,
 * listeners cannot distinguish different sources and should
 * listen to at most one event producer.
 * </p>
 * <p>
 * To let an object listen to remote events while not exposing
 * itself as remote accessable object, create an instance of
 * <code>RemoteForwarder</code> and let the object's class
 * implement the local <code>EventListener</code> interface.
 * </p>
 * @see RemoteEventForwarder
 * @see EventListener
 *
 * @author Olaf Kummer, Michael Duvigneau
 */
public interface RemoteEventListener extends Remote {

    /**
     * Receives the event message.
     *
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public void update() throws RemoteException;
}