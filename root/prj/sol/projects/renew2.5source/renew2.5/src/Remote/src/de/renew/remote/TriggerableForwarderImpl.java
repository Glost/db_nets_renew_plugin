package de.renew.remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * Implements the triggerable forwarder interface.
 * @see TriggerableForwarder for details.
 */
public class TriggerableForwarderImpl extends UnicastRemoteObject
        implements TriggerableForwarder {

    /**
     * The remote triggerable to pass search proposals to.
     */
    RemoteTriggerable triggerable;

    /**
     * Creates a new triggerable forwarder.
     * @param triggerable The remote triggerable to pass search proposals to.
     * @exception RemoteException An RMI problem occurred.
     */
    public TriggerableForwarderImpl(RemoteTriggerable triggerable)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.triggerable = triggerable;
    }

    /**
     * Returns whether this forwarder equals another object.
     * This is if and only if the other object is a triggerable
     * forwarder, too, and if is has an equals triggerable.
     * @param object The other object.
     * @return Whether both objects are equals.
     */
    public boolean equals(Object object) {
        if (object instanceof TriggerableForwarder) {
            return triggerable.equals(((TriggerableForwarderImpl) object).triggerable);
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code of the forwarder.
     * This is the hash code of the triggerable.
     * @return The hash code.
     */
    public int hashCode() {
        return triggerable.hashCode();
    }

    /**
     * Trigger a new search because bindings
     * might have appeared or disappeared.
     * @exception RemoteException An RMI problem occurred.
     */
    public void proposeSearch() throws RemoteException {
        triggerable.proposeSearch();
    }
}