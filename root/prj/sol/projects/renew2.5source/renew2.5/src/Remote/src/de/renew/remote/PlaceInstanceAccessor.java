/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.io.Serializable;

import java.rmi.RemoteException;


/**
 * A place instance accessor allows to view and modify the
 * state of a place instance.
 *
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 *
 * @author Olaf Kummer
 */
public interface PlaceInstanceAccessor extends RemoteEventProducer,
                                               ObjectAccessor {

    /**
     * Get the ID of the place instance that is being accessed with
     * this object. The ID of a place instance is the same as the
     * ID of the place the instance is based on.
     *
     * @return the ID of the place instance
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetElementID getID() throws RemoteException;

    /**
     * Get a place accessor that corresponds to this accessor.
     *
     * @return the place accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public PlaceAccessor getPlace() throws RemoteException;

    /**
     * Get a net instance accessor that corresponds to this accessor.
     *
     * @return the net instance accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetInstanceAccessor getNetInstance() throws RemoteException;

    /**
     * Get the current marking of this place instance in the form
     * of a marking accessor. This accessor represents a snapshot
     * of the place instance, i.e., its contents will not change even if
     * transitions fire.
     *
     * @return the marking accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public MarkingAccessor getMarking() throws RemoteException;

    /**
     * Get the token counts of this place instance in the form
     * of a token counts accessor. This accessor represents a snapshot
     * of the place instance, i.e., its contents will not change even if
     * transitions fire.
     *
     * @return the token counts accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TokenCountsAccessor getTokenCounts() throws RemoteException;

    /**
     * Add a token to the place instance. This method is only
     * applicable for serializable objects. If a primitive value
     * must be inserted, it has to be encapsulated in an object
     * of class Value.
     *
     * <p>
     * To remove a token from a place, use the <code>removeOneOf</code>
     * method of a marking accessor you got via the <code>getMarking</code>
     * method of this place accessor.
     * </p>
     *
     * @see de.renew.util.Value
     * @see MarkingAccessor#removeOneOf
     * @see #getMarking
     *
     * @param token the object to be inserted
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public void addSerializableToken(Serializable token)
            throws RemoteException;
}