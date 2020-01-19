package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The binding accessor wraps bindings to provide the
 * functionality to list and fire bindings from the client.
 *
 * @author Thomas Jacob
 */
public interface BindingAccessor extends Remote {

    /**
     * Executes the binding.
     * @param asynchronous Whether to execute the binding asynchronously.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean execute(boolean asynchronous) throws RemoteException;

    /**
     * Returns the description of the binding.
     * @return The description.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String getDescription() throws RemoteException;
}