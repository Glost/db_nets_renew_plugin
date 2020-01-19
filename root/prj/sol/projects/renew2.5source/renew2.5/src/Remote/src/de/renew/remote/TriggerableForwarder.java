package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The triggerable forwarder represents an object that receives
 * search proposals from the simulator and passes them to a client
 * object that does not want to expose itself as remote object.
 * In such a client object, create an instance of the TriggerableForwarderImpl
 * with the client object as argument. This client has to implement
 * the RemoteTriggerable interface. Use the created forwarder
 * as argument to TransitionInstanceAccessor.findAllBindings.
 */
public interface TriggerableForwarder extends Remote {

    /**
     * Trigger a new search because bindings
     * might have appeared or disappeared.
     * @exception RemoteException An RMI problem occurred.
     */
    public void proposeSearch() throws RemoteException;
}