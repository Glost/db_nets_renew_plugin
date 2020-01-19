/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.rmi.RemoteException;


/**
 * A net instance accessor allows to create accessors
 * for the place and transition instances of a net instance.
 * It also provides the name of the net instance as it would
 * be output by its <code>toString()</code> method.
 *
 * Access to places and transitions is governed by the
 * IDs of the net elements.
 *
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * </p>
 *
 * NetInstanceAccessor.java
 * Created: Sun Jul 16  2000
 * @author Michael Duvigneau
 */
public interface NetInstanceAccessor extends ObjectAccessor {

    /**
     * Return the accessor for the net this instance is based
     * on.
     *
     * @return the net accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetAccessor getNet() throws RemoteException;

    /**
     * Return the instance-distinguishing ID of the net instance.
     *
     * @return the net instance's ID, as per {@link de.renew.net.NetInstance#getID()}
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String getID() throws RemoteException;

    /**
     * Return a place instance accessor for the instance of the
     * place with the given ID in this net instance. Return
     * <code>null</code>, if no such place exists.
     *
     * <p>
     * To obtain a set of valid place IDs, use the <code>getPlaceIDs</code>
     * method of the net accessor which in turn can be obtained by a call
     * to <code>getNet()</code>.
     * </p>
     *
     * @see NetAccessor#getPlaceIDs
     * @see #getNet
     *
     * @param id the place's ID
     * @return a place instance accessor or <code>null</code>
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public PlaceInstanceAccessor getPlaceInstance(NetElementID id)
            throws RemoteException;

    /**
     * Return a transition instance accessor for the instance of the
     * transition with the given ID in this net instance. Return
     * <code>null</code>, if no such transition exists.
     *
     * <p>
     * To obtain a set of valid transition IDs, use the
     * <code>getTransitionIDs</code> method of the net accessor
     * which in turn can be obtained by a call to <code>getNet()</code>.
     * </p>
     *
     * @see NetAccessor#getTransitionIDs
     * @see #getNet
     *
     * @param id the transition's ID
     * @return a transition instance accessor or <code>null</code>
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TransitionInstanceAccessor getTransitionInstance(NetElementID id)
            throws RemoteException;
}