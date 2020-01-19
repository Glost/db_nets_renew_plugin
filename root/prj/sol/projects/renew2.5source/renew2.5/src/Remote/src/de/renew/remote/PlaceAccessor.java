/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.rmi.RemoteException;


/**
 * A place accessor allows to view and modify the state of a place.
 *
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 *
 * @author Olaf Kummer
 */
public interface PlaceAccessor extends ObjectAccessor {

    /**
     * Returns the internal ID assigned to the place.
     *
     * @return the ID of the place
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetElementID getID() throws RemoteException;
    // Something like:
    // public RemoteBreakpoint addBreakpoint(Para meters) throws RemoteException;
}