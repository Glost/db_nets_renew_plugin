package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.searcher.TriggerCollection;
import de.renew.engine.searcher.Triggerable;
import de.renew.engine.simulator.Binding;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.SimulatorHelper;

import de.renew.net.NetElementID;
import de.renew.net.TransitionInstance;
import de.renew.net.event.FiringEvent;
import de.renew.net.event.TransitionEventListener;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>TransitionInstanceAccessor</code>
 * interface and nothing more than needed to implement it.
 * <p>
 * </p>
 * TransitionInstanceAccessorImpl.java
 * Created: Mon Jul 17  2000
 * @author Michael Duvigneau
 */
public class TransitionInstanceAccessorImpl extends ObjectAccessorImpl
        implements TransitionInstanceAccessor, TransitionEventListener,
                           Triggerable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TransitionInstanceAccessorImpl.class);

    /**
     * The set of registered remote event listeners that want
     * to be informed about transition events.
     */
    private Set<RemoteEventListener> firingListeners = Collections
                                                           .synchronizedSet(new HashSet<RemoteEventListener>());

    /**
     * The set of registered triggerable forwarders that want
     * to be informed about search proposals.
     */
    private Set<TriggerableForwarder> proposalListeners = Collections
                                                              .synchronizedSet(new HashSet<TriggerableForwarder>());

    /**
     * The triggers of this triggerable.
     */
    private TriggerCollection triggers = new TriggerCollection(this);

    /**
     * Creates a new transition instance accessor for the given transition
     * instance.
     *
     * @param transitionInstance the transition instance to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TransitionInstanceAccessorImpl(TransitionInstance transitionInstance,
                                          SimulationEnvironment environment)
            throws RemoteException {
        super(transitionInstance, environment);
    }

    /**
     * Registers the given remote event listener so that it will
     * receive future remote event messages concerning the accessed
     * transition instance.
     * <p>
     * If the listener is registered already, the additional
     * registration try is ignored. The <code>equals()</code>
     * method serves as indicator to allow the mechanism to
     * work also for remote objects.
     * </p>
     * <p>
     * This method is specified by the <code>RemoteEventProducer</code>
     * interface which is required by the <code>TransitionInstanceAccessor</code>
     * interface.
     * </p>
     *
     * @param listener the listener to register
     * @exception java.rmi.RemoteException if a RMI failure occured.
     */
    public void addRemoteEventListener(RemoteEventListener listener)
            throws RemoteException {
        synchronized (firingListeners) {
            // We register this accessor as TransitionEventListener
            // when adding the first remote event listener.
            if (firingListeners.isEmpty()) {
                ((TransitionInstance) object).addTransitionEventListener(this);
            }

            firingListeners.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     **/
    public BindingAccessor[] findAllBindings(final TriggerableForwarder triggerableForwarder)
            throws RemoteException {
        final TransitionInstanceAccessorImpl impl = this;
        Future<BindingAccessor[]> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<BindingAccessor[]>() {
                public BindingAccessor[] call() throws Exception {
                    synchronized (proposalListeners) {
                        proposalListeners.add(triggerableForwarder);
                    }

                    BindingAccessor[] bindings;
                    try {
                        triggers.clear();
                        Collection<Binding> allBindings = SimulatorHelper
                                               .findAllBindings((TransitionInstance) object,
                                                                impl);
                        Iterator<Binding> iter = allBindings.iterator();

                        bindings = new BindingAccessor[allBindings.size()];
                        for (int i = 0; iter.hasNext(); i++) {
                            bindings[i] = new BindingAccessorImpl(iter.next(),
                                                                  getEnvironment());
                        }
                    } catch (RemoteException e) {
                        logger.error(e.getMessage(), e);
                        bindings = new BindingAccessor[0];
                    }

                    return bindings;
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
        return null;

    }

    /**
     * {@inheritDoc}
     **/
    public void forgetBindings(final TriggerableForwarder triggerableForwarder)
            throws RemoteException {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    synchronized (proposalListeners) {
                        proposalListeners.remove(triggerableForwarder);
                        if (proposalListeners.isEmpty()) {
                            triggers.clear();
                        }
                    }
                }
            });
    }

    /**
     * {@inheritDoc}
     **/
    public boolean fireOneBinding() throws RemoteException {
        Future<Boolean> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean asynchronous = !getEnvironment().getSimulator()
                                                .isSequential();
                    return ((TransitionInstance) object).fireOneBinding(asynchronous);
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
        return false;

    }

    /**
     * The common reaction to all transition events is to send a
     * remote event to all currently registered remote event
     * listeners.
     */
    private void fireRemoteEvent() {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    Iterator<RemoteEventListener> iterator;
                    RemoteEventListener listener;
                    synchronized (firingListeners) {
                        iterator = firingListeners.iterator();
                        while (iterator.hasNext()) {
                            listener = iterator.next();
                            try {
                                listener.update();
                            } catch (RemoteException e) {
                                logger.error("TransitionInstanceAccessor: Remote event to "
                                             + listener
                                             + " probably got lost due to " + e);
                            }
                        }
                    }
                    return null;
                }
            });
        try {
            future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
    }

    /**
     * Converts the local transition event into a remote event and
     * forwards it to all remote listeners.
     */
    public void firingComplete(FiringEvent event) {
        fireRemoteEvent();
    }

    /**
     * Converts the local transition event into a remote event and
     * forwards it to all remote listeners.
     */
    public void firingStarted(FiringEvent event) {
        fireRemoteEvent();
    }

    /* This method is specified by the TransitionInstanceAccessor interface. */
    public NetElementID getID() throws RemoteException {
        return ((TransitionInstance) object).getTransition().getID();
    }

    /* This method is specified by the TransitionInstanceAccessor interface. */
    public TransitionAccessor getTransition() throws RemoteException {
        return new TransitionAccessorImpl(((TransitionInstance) object)
                   .getTransition(), getEnvironment());
    }

    /**
     * Returns the transition instance, if the caller knows that
     * this is the local representation. This is required for setting breakpoints.
     * @return The transition instance.
     */
    public TransitionInstance getTransitionInstance() {
        return (TransitionInstance) object;
    }

    /* This method is specified by the TransitionInstanceAccessor interface. */
    public NetInstanceAccessor getNetInstance() throws RemoteException {
        return new NetInstanceAccessorImpl(((TransitionInstance) object)
                   .getNetInstance(), getEnvironment());
    }

    /**
     * Returns whether the transition instance is currently firing
     * at least one occurrance.
     * @return Whether it is firing.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean isFiring() throws RemoteException {
        return ((TransitionInstance) object).isFiring();
    }

    /**
     * Called when a trigger proposes a new search.
     */
    public synchronized void proposeSearch() {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    List<TriggerableForwarder> triggerableForwarders;
                    synchronized (proposalListeners) {
                        triggerableForwarders = new ArrayList<TriggerableForwarder>(proposalListeners);
                        proposalListeners.clear();
                        triggers.clear();
                    }
                    for (Iterator<TriggerableForwarder> i = triggerableForwarders
                                                            .iterator();
                                 i.hasNext();) {
                        TriggerableForwarder forwarder = null;
                        try {
                            forwarder = i.next();
                            forwarder.proposeSearch();
                        } catch (RemoteException e) {
                            logger.error("TransitionInstanceAccessor: Search proposal to "
                                         + forwarder
                                         + " probably got lost due to " + e);
                        }
                    }
                    return null;
                }
            });
        try {
            future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }
    }

    /**
     * Unregisters the given remote event listener so that it will
     * not receive future remote event messages from this transition
     * instance accessor.
     * <p>
     * All listeners that equal the specified one will be
     * unregistered. If the listener was not registered, the
     * unregistration try is ignored.
     * </p>
     * <p>
     * This method is specified by the <code>RemoteEventProducer</code>
     * interface which is required by the <code>TransitionInstanceAccessor</code>
     * interface.
     * </p>
     *
     * @param listener the listener to unregister
     * @exception java.rmi.RemoteException if a RMI failure occured.
     */
    public void removeRemoteEventListener(RemoteEventListener listener)
            throws RemoteException {
        synchronized (firingListeners) {
            firingListeners.remove(listener);


            // We unregister this accessor as TransitionEventListener
            // when removing the last remote event listener.
            if (firingListeners.isEmpty()) {
                ((TransitionInstance) object).removeTransitionEventListener(this);
            }
        }
    }

    /**
     * Returns the triggers of this triggerable.
     * @return The triggers of this triggerable.
     */
    public TriggerCollection triggers() {
        return triggers;
    }

    /**
     * This class does not use synchronous notifications, because
     * it might lead to deadlocks for some applications. Asynchronous
     * notifications are always safe, although they might arrive slightly
     * later.
     *
     * @return a <code>boolean</code> value
     */
    public boolean wantSynchronousNotification() {
        return false;
    }
}