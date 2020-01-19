package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetElementID;
import de.renew.net.Place;

import java.rmi.RemoteException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>PlaceAccessor</code> interface
 * and nothing more.
 * <p>
 * </p>
 * PlaceAccessorImpl.java
 * Created: Fri Jul 14  2000
 * @author Michael Duvigneau
 */
public class PlaceAccessorImpl extends ObjectAccessorImpl
        implements PlaceAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PlaceAccessorImpl.class);

    /**
     * Creates a new place accessor for the given place.
     *
     * @param place the place to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public PlaceAccessorImpl(Place place, SimulationEnvironment env)
            throws RemoteException {
        super(place, env);
    }

    /* This method is specified by the PlaceAccessor interface. */
    public NetElementID getID() throws RemoteException {
        Future<NetElementID> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetElementID>() {
                public NetElementID call() throws Exception {
                    return ((Place) object).getID();
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
     * Returns the place, if the caller knows that
     * this is the local representation. This is required for setting breakpoints.
     * @return The place.
     */
    public Place getPlace() {
        return (Place) object;
    }
}