/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.rmi.RemoteException;


/**
 * A transition accessor allows to view and modify the state of
 * a transition.
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * </p>
 *
 * TransitionAccessor.java
 * Created: Mon Jul 10  2000
 * @author Michael Duvigneau
 */
public interface TransitionAccessor extends ObjectAccessor {

    /**
     * Returns the internal ID assigned to the transition.
     *
     * @return the ID of the transition
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetElementID getID() throws RemoteException;
}