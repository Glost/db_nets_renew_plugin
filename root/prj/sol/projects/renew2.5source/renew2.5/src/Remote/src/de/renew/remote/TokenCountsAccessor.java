package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Provides access to the token counts of a place instance.
 * This is a light-weight version of the marking accessor.
 * Its contents will not change even if transitions fire.
 *
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI. This accessor does not extend the
 * <code>de.renew.remote.Accessor</code> interface because it
 * does not represent an object that locally exists.
 * </p>
 *
 * @author Thomas Jacob
 */
public interface TokenCountsAccessor extends Remote {

    /**
     * Return the number of free tokens in this marking.
     * Even if two tokens are equal, they are both counted individually.
     * Currently tested tokens are not included in this count.
     *
     * @return the number of free tokens
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public int getFreeTokenCount() throws RemoteException;

    /**
     * Return the number of currently tested tokens in this marking.
     *
     * @return the number of tested tokens
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public int getTestedTokenCount() throws RemoteException;

    /**
     * Returns whether the place instance has no tokens.
     *
     * @return Whether the place instance is empty.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean isEmpty() throws RemoteException;
}