package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetElementID;
import de.renew.net.Transition;

import java.rmi.RemoteException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class implements the <code>TransitionAccessor</code> interface
 * and nothing more.
 * <p>
 * </p>
 * TransitionAccessorImpl.java
 * Created: Sun Jul 16  2000
 * @author Michael Duvigneau
 */
public class TransitionAccessorImpl extends ObjectAccessorImpl
        implements TransitionAccessor {

    /**
     * Creates a new transition accessor for the given transition.
     *
     * @param transition the transition to access
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public TransitionAccessorImpl(Transition transition,
                                  SimulationEnvironment env)
            throws RemoteException {
        super(transition, env);
    }

    /* This method is specified by the TransitionAccessor interface. */
    public NetElementID getID() throws RemoteException {
        Future<NetElementID> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetElementID>() {
                public NetElementID call() throws Exception {
                    return ((Transition) object).getID();
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
     * Returns the transition, if the caller knows that
     * this is the local representation. This is required for setting breakpoints.
     * @return The transition.
     */
    public Transition getTransition() {
        return (Transition) object;
    }
}