package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetElementID;
import de.renew.net.PlaceInstance;
import de.renew.net.event.PlaceEvent;
import de.renew.net.event.PlaceEventListener;
import de.renew.net.event.TokenEvent;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>PlaceInstanceAccessor</code>
 * interface and nothing more than needed to implement it.
 * <p>
 *
 * </p>
 * PlaceInstanceAccessorImpl.java
 * Created: Sun Jul 16  2000
 * @author Michael Duvigneau
 */
public class PlaceInstanceAccessorImpl extends ObjectAccessorImpl
        implements PlaceEventListener, PlaceInstanceAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PlaceInstanceAccessorImpl.class);

    // ------------------------------------ Place event forwarding ----


    /**
     * The set of registered remote event listeners that want
     * to be informed about place events.
     */
    private Set<RemoteEventListener> listeners = Collections.synchronizedSet(new HashSet<RemoteEventListener>());

    /**
     * Creates a new place instance accessor for the given place
     * instance.
     *
     * @param place the place instance to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public PlaceInstanceAccessorImpl(PlaceInstance place,
                                     SimulationEnvironment env)
            throws RemoteException {
        super(place, env);
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public NetElementID getID() throws RemoteException {
        return ((PlaceInstance) object).getPlace().getID();
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public PlaceAccessor getPlace() throws RemoteException {
        return new PlaceAccessorImpl(((PlaceInstance) object).getPlace(),
                                     getEnvironment());
    }

    /**
     * Returns the place instance, if the caller knows that
     * this is the local representation. This is required for setting breakpoints.
     * @return The place instance.
     */
    public PlaceInstance getPlaceInstance() {
        return (PlaceInstance) object;
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public NetInstanceAccessor getNetInstance() throws RemoteException {
        return new NetInstanceAccessorImpl(((PlaceInstance) object)
                   .getNetInstance(), getEnvironment());
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public MarkingAccessor getMarking() throws RemoteException {
        return new MarkingAccessorImpl((PlaceInstance) object, getEnvironment());
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public TokenCountsAccessor getTokenCounts() throws RemoteException {
        return new TokenCountsAccessorImpl((PlaceInstance) object);
    }

    /* This method is specified by the PlaceInstanceAccessor interface. */
    public void addSerializableToken(Serializable token)
            throws RemoteException {
        // TODO: implement this de.renew.remote.PlaceInstanceAccessor method
    }

    /**
     * Registers the given remote event listener so that it will
     * receive future remote event messages concerning the accessed
     * place instance.
     * <p>
     * If the listener is registered already, the additional
     * registration try is ignored. The <code>equals()</code>
     * method serves as indicator to allow the mechanism to
     * work also for remote objects.
     * </p>
     * <p>
     * This method is specified by the <code>RemoteEventProducer</code>
     * interface which is required by the <code>PlaceInstanceAccessor</code>
     * interface.
     * </p>
     *
     * @param listener the listener to register
     * @exception java.rmi.RemoteException if a RMI failure occured.
     */
    public void addRemoteEventListener(final RemoteEventListener listener)
            throws RemoteException {
        final PlaceInstanceAccessorImpl impl = this;
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    synchronized (listeners) {
                        // We register this accessor as PlaceEventListener
                        // when adding the first remote event listener.
                        if (listeners.isEmpty()) {
                            ((PlaceInstance) object).addPlaceEventListener(impl);
                        }

                        listeners.add(listener);
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
     * not receive future remote event messages from this place
     * instance accessor.
     * <p>
     * All listeners that equal the specified one will be
     * unregistered. If the listener was not registered, the
     * unregistration try is ignored.
     * </p>
     * <p>
     * This method is specified by the <code>RemoteEventProducer</code>
     * interface which is required by the <code>PlaceInstanceAccessor</code>
     * interface.
     * </p>
     *
     * @param listener the listener to unregister
     * @exception java.rmi.RemoteException if a RMI failure occured.
     */
    public void removeRemoteEventListener(final RemoteEventListener listener)
            throws RemoteException {
        final PlaceInstanceAccessorImpl impl = this;
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    synchronized (listeners) {
                        listeners.remove(listener);


                        // We unregister this accessor as PlaceEventListener
                        // when removing the last remote event listener.
                        if (listeners.isEmpty()) {
                            ((PlaceInstance) object).removePlaceEventListener(impl);
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

    /**
     * Converts the local place event into a remote event and
     * forwards it to all remote listeners.
     */
    public void markingChanged(PlaceEvent event) {
        fireRemoteEvent();
    }

    /**
     * Converts the local place event into a remote event and
     * forwards it to all remote listeners.
     */
    public void tokenAdded(TokenEvent event) {
        fireRemoteEvent();
    }

    /**
     * Converts the local place event into a remote event and
     * forwards it to all remote listeners.
     */
    public void tokenRemoved(TokenEvent event) {
        fireRemoteEvent();
    }

    /**
     * Converts the local place event into a remote event and
     * forwards it to all remote listeners.
     */
    public void tokenTested(TokenEvent event) {
        fireRemoteEvent();
    }

    /**
     * Converts the local place event into a remote event and
     * forwards it to all remote listeners.
     */
    public void tokenUntested(TokenEvent event) {
        fireRemoteEvent();
    }

    /**
     * The common reaction to all place events is to send a
     * remote event to all currently registered remote event
     * listeners.
     */
    private void fireRemoteEvent() {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    Iterator<RemoteEventListener> iterator;
                    RemoteEventListener listener;
                    synchronized (listeners) {
                        iterator = listeners.iterator();
                        while (iterator.hasNext()) {
                            listener = iterator.next();
                            try {
                                listener.update();
                            } catch (RemoteException e) {
                                logger.error("PlaceInstanceAccessor: Remote event to "
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
    }
}