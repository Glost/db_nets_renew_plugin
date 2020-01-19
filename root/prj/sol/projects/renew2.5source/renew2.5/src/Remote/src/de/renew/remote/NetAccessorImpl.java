package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;

import java.rmi.RemoteException;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>NetAccessor</code> interface
 * and nothing more.
 * <p>
 * </p>
 * NetAccessorImpl.java
 * Created: Thu Jul 13  2000
 * @author Michael Duvigneau
 */
public class NetAccessorImpl extends ObjectAccessorImpl implements NetAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetAccessorImpl.class);

    /**
     * Creates a new net accessor for the given net.
     *
     * @param net the net to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetAccessorImpl(Net net, SimulationEnvironment env)
            throws RemoteException {
        super(net, env);
    }

    /* This method is specified by the NetAccessor interface. */
    public String getName() throws RemoteException {
        return ((Net) object).getName();
    }

    // -------------------------------------------- Place handling ----

    /* This method is specified by the NetAccessor interface. */
    public NetElementID[] getPlaceIDs() throws RemoteException {
        final Net net = (Net) object;
        Future<NetElementID[]> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetElementID[]>() {
                public NetElementID[] call() throws Exception {
                    synchronized (net) {
                        NetElementID[] placeIDs = new NetElementID[((Net) object)
                                                                   .placeCount()];
                        Iterator<Place> places = net.places().iterator();
                        for (int i = 0; i < placeIDs.length; i++) {
                            placeIDs[i] = places.next().getID();
                        }
                        return placeIDs;
                    }
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
     * Return a place accessor for the place with the given
     * ID. Return <code>null</code>, if no such place exists.
     *
     * This method is specified by the NetAccessor interface.
     *
     * <p>
     * An additional remark to the validity of the given id:
     * It is not checked if the set of places of the accessed
     * net has changed since the last call to <code>getPlaceIDs()
     * </code>.
     * </p>
     *
     * @see NetAccessor#getPlace
     */
    public PlaceAccessor getPlace(final NetElementID id)
            throws RemoteException {
        Future<PlaceAccessor> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<PlaceAccessor>() {
                public PlaceAccessor call() throws Exception {
                    Place result = ((Net) object).getPlaceWithID(id);
                    if (result == null) {
                        return null;
                    }
                    return new PlaceAccessorImpl(result, getEnvironment());
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

    // --------------------------------------- Transition handling ----

    /* This method is specified by the NetAccessor interface. */
    public NetElementID[] getTransitionIDs() throws RemoteException {
        final Net net = (Net) object;
        Future<NetElementID[]> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetElementID[]>() {
                public NetElementID[] call() throws Exception {
                    synchronized (net) {
                        NetElementID[] transitionIDs = new NetElementID[((Net) object)
                                                                        .transitionCount()];
                        Iterator<Transition> transitions = net.transitions()
                                                              .iterator();
                        for (int i = 0; i < transitionIDs.length; i++) {
                            transitionIDs[i] = transitions.next().getID();
                        }
                        return transitionIDs;
                    }
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
     * Return a transition accessor for the transition with the given
     * ID. Return <code>null</code>, if no such transition exists.
     *
     * This method is specified by the NetAccessor interface.
     *
     * <p>
     * An additional remark to the validity of the given id:
     * It is not checked if the set of transitions of the accessed
     * net has changed since the last call to <code>getTransitionIDs()
     * </code>.
     * </p>
     *
     * @see NetAccessor#getTransition
     */
    public TransitionAccessor getTransition(final NetElementID id)
            throws RemoteException {
        Future<TransitionAccessor> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<TransitionAccessor>() {
                public TransitionAccessor call() throws Exception {
                    Transition result = ((Net) object).getTransitionWithID(id);

                    if (result == null) {
                        return null;
                    }
                    return new TransitionAccessorImpl(result, getEnvironment());
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
}