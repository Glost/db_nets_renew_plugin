/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import de.renew.net.NetElementID;

import java.rmi.RemoteException;


/**
 * A transition instance accessor allows to view and modify the
 * state of a transition instance.
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * </p>
 *
 * TransitionInstanceAccessor.java
 * Created: Mon Jul 10  2000
 * @author Michael Duvigneau
 */
public interface TransitionInstanceAccessor extends RemoteEventProducer,
                                                    ObjectAccessor {

    /**
     * Returns all bindings for the transition instance
     * and registeres a remote triggerable for search proposals.
     * The triggerable is a one-shot listener: when a search proposal is
     * given via {@link TriggerableForwarder#proposeSearch}, the
     * triggerable forwarder will be deregistered automatically.
     * <p>
     * Objects that call this method are required to call
     * {@link #forgetBindings} when they are no longer interested in
     * binding updates. Otherwise, the remote triggerable would remain
     * registered until the next marking change in the transition's
     * neighbourhood. A side effect is that simulation state serialization
     * is impossible during that time.
     * </p>
     *
     * @param triggerableForwarder The triggerable forwarder.
     * @return The bindings.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public BindingAccessor[] findAllBindings(TriggerableForwarder triggerableForwarder)
            throws RemoteException;

    /**
     * Deregisters a remote triggerable for search proposals that was
     * registered by <code>findAllBindings</code>.
     * <p>
     * Objects that call <code>findAllBindings</code> are required to call
     * this method when they are no longer interested in binding updates.
     * See {@link #findAllBindings} for an explanation.
     * </p>
     *
     * @param triggerableForwarder the triggerable forwarder to deregister
     * @exception RemoteException if an RMI failure occured.
     **/
    public void forgetBindings(TriggerableForwarder triggerableForwarder)
            throws RemoteException;

    /**
     * Fires one binding for the accessor's transition. If multiple
     * bindings are available, one is chosen non-determistically.
     * <p>
     * This method's result does <em>not</em> depend on a call to
     * <code>findAllBindings</code>. It can be used stand-alone.
     * </p>
     *
     * @return Whether a binding could be fired.
     * @exception RemoteException If an RMI problem occurred.
     */
    public boolean fireOneBinding() throws RemoteException;

    /**
     * Get the ID of the transition instance that is being accessed with
     * this object. The ID of a transition instance is the same as the
     * ID of the transition the instance is based on.
     *
     * @return the ID of the transition instance
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetElementID getID() throws RemoteException;

    /**
     * Get a transition accessor that corresponds to this accessor.
     *
     * @return the transition accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TransitionAccessor getTransition() throws RemoteException;

    /**
     * Get a net instance accessor that corresponds to this accessor.
     *
     * @return the net instance accessor
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetInstanceAccessor getNetInstance() throws RemoteException;

    /**
     * Returns whether the transition instance is currently firing
     * at least one occurrance.
     * @return Whether it is firing.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean isFiring() throws RemoteException;
}