package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The producer interface for managing remote event listeners.
 * Classes implementing this interface promise to send their
 * events to all currently registered remote event listeners.
 *
 * @author Olaf Kummer, Michael Duvigneau
 * @see RemoteEventListener
 */
public interface RemoteEventProducer extends Remote {

    /**
     * Registers an event listener.
     *
     * @param listener the listener that will receive the events
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public void addRemoteEventListener(RemoteEventListener listener)
            throws RemoteException;

    /**
     * Unregisters an event listener.
     *
     * @param listener the listener that is receiving events
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public void removeRemoteEventListener(RemoteEventListener listener)
            throws RemoteException;
}