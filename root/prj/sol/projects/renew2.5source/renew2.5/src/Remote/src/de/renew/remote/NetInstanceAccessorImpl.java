package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetElementID;
import de.renew.net.NetInstance;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInstance;

import java.rmi.RemoteException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>NetInstanceAccessor</code>
 * interface and nothing more.
 * <p>
 * </p>
 * NetInstanceAccessorImpl.java
 * Created: Sun Jul 16  2000
 * @author Michael Duvigneau
 */
public class NetInstanceAccessorImpl extends ObjectAccessorImpl
        implements NetInstanceAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetInstanceAccessorImpl.class);

    /**
     * Creates a new net instance accessor for the given net instance.
     *
     * @param netInstance the net instance to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public NetInstanceAccessorImpl(NetInstance netInstance,
                                   SimulationEnvironment environment)
            throws RemoteException {
        super(netInstance, environment);
    }

    /* This method is specified by the NetInstanceAccessor interface. */
    public NetAccessor getNet() throws RemoteException {
        Future<NetAccessor> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetAccessor>() {
                public NetAccessor call() throws Exception {
                    return new NetAccessorImpl(((NetInstance) object).getNet(),
                                               getEnvironment());
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

    /* This method is specified by the NetInstanceAccessor interface. */
    public String getID() throws RemoteException {
        return ((NetInstance) object).getID();
    }

    /* This method is specified by the NetInstanceAccessor interface. */
    public PlaceInstanceAccessor getPlaceInstance(final NetElementID id)
            throws RemoteException {
        Future<PlaceInstanceAccessor> future = SimulationThreadPool.getCurrent()
                                                                   .submitAndWait(new Callable<PlaceInstanceAccessor>() {
                public PlaceInstanceAccessor call() throws Exception {
                    Place place = ((NetInstance) object).getNet()
                                   .getPlaceWithID(id);

                    if (place == null) {
                        return null;
                    }
                    PlaceInstance placeInstance = ((NetInstance) object)
                                                   .getInstance(place);

                    if (placeInstance == null) {
                        return null;
                    }
                    return new PlaceInstanceAccessorImpl(placeInstance,
                                                         getEnvironment());
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

    /* This method is specified by the NetInstanceAccessor interface. */
    public TransitionInstanceAccessor getTransitionInstance(final NetElementID id)
            throws RemoteException {
        Future<TransitionInstanceAccessor> future = SimulationThreadPool.getCurrent()
                                                                        .submitAndWait(new Callable<TransitionInstanceAccessor>() {
                public TransitionInstanceAccessor call()
                        throws Exception {
                    Transition transition = ((NetInstance) object).getNet()
                                             .getTransitionWithID(id);

                    if (transition == null) {
                        return null;
                    }
                    TransitionInstance transitionInstance = ((NetInstance) object)
                                                            .getInstance(transition);

                    if (transitionInstance == null) {
                        return null;
                    }
                    return new TransitionInstanceAccessorImpl(transitionInstance,
                                                              getEnvironment());
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
     * Return a place instance accessor for the instance of the
     * given place in this net instance. Return <code>null</code>,
     * if no such place instance exists.
     *
     * <p>
     * This method was initially specified by the NetInstanceAccessor
     * interface and should provide the functionality of<code>
     * de.renew.net.NetInstance.getInstance(Place)</code>.
     * (An analogous method for transitions was planned, too.)
     *
     * It was removed from the interface because there may be
     * <b>unexpected results</b> with the currently imaginable
     * implementations:
     *
     * If the method is called with a place not belonging to the
     * net that is accessed by this accessor, and there exists a
     * place with the same ID in the accessed net, then a place
     * instance that is not an instance of the specified place
     * will be returned.
     *
     * But I don't know how to deny that for the following
     * reasons: <ul>
     *
     * <li> Places don't know the net they belong to. So there is
     * no direct way to check if a 'correct' place was passed
     * as argument. </li>
     *
     * <li> I cannot be sure that all <code>PlaceAccessor</code>s are
     * <code>PlaceAccessorImpl</code>s. (For the moment, there
     * aren't any other implementations. But, who knows the future?
     * What happens if a stub is passed?) Otherwise I could
     * extract the accessed place by a package protected method.
     * </li>
     * </ul>
     * </p>
     *
     * @param place an accessor for the place instance's place
     * @return a place instance accessor or <code>null</code>
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    PlaceInstanceAccessor getInstance(PlaceAccessor place)
            throws RemoteException {
        return getPlaceInstance(place.getID());
    }
}