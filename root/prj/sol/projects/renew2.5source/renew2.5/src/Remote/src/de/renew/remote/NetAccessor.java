/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.rmi.RemoteException;


/**
 * A net accessor allows to create accessors for the
 * places and transitions of a net. It also provides
 * the name of the net as it would be output by the
 * getName() method of the net.
 *
 * Access to places and transitions is governed by the
 * IDs of the net elements.
 *
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 *
 * @author Olaf Kummer
 */
public interface NetAccessor extends ObjectAccessor {

    /**
     * Return the name of the net as generated by
     * the method getName() of the net.
     *
     * @return the net's name
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String getName() throws RemoteException;

    /**
     * Provide a list of all valid IDs of places.
     * Applications may use this to iterate through all
     * places or to restrict queries to those places
     * that are actually available.
     * <p>
     * In conjunction with <code>getPlace(int id)</code> this
     * method can be used to create an array of place accessors.
     * It is difficult to pass an array of place accessors
     * directly, because remote accessors must not be
     * serialized as it would happen if they were returned
     * inside an array.
     * </p>
     *
     * @return an array of integer IDs for places
     * @exception java.rmi.RemoteException if an RMI failure occured.
     *
     * @see NetAccessor#getPlace
     */
    public NetElementID[] getPlaceIDs() throws RemoteException;

    /**
     * Return a place accessor for the place with the given
     * ID. Return <code>null</code>, if no such place exists.
     *
     * @param id the place's ID
     * @return a place accessor or <code>null</code>
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public PlaceAccessor getPlace(NetElementID id) throws RemoteException;

    /**
     * Provide a list of all valid IDs of transitions.
     * Applications may use this to iterate through all
     * transitions or to restrict queries to those transitions
     * that are actually available.
     * <p>
     * In conjunction with <code>getTransition(int id)</code> this
     * method can be used to create an array of transition accessors.
     * </p>
     *
     * @return an array of integer IDs for transitions
     * @exception java.rmi.RemoteException if an RMI failure occured.
     *
     * @see NetAccessor#getTransition
     */
    public NetElementID[] getTransitionIDs() throws RemoteException;

    /**
     * Return a transition accessor for the transition with the given
     * ID. Return <code>null</code>, if no such transition exists.
     *
     * @param id the transition's ID
     * @return a transition accessor or <code>null</code>
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TransitionAccessor getTransition(NetElementID id)
            throws RemoteException;
}