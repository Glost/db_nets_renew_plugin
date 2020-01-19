package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.simulator.Binding;
import de.renew.engine.simulator.SimulationThreadPool;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Implements the BindingAccessor interface.
 * The binding accessor wraps bindings to provide the
 * functionality to list and fire bindings from the client.
 *
 * @author Thomas Jacob
 */
public class BindingAccessorImpl extends UnicastRemoteObject
        implements BindingAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BindingAccessorImpl.class);

    /**
     * The simulation environment this object is situated in.
     */
    private SimulationEnvironment environment;

    /**
     * The object for the accessor.
     */
    protected final Binding binding;

    /**
     * Creates a new BindingAccessor.
     * @param binding The binding for the accessor.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public BindingAccessorImpl(Binding binding,
                               SimulationEnvironment environment)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.binding = binding;
        this.environment = environment;
    }

    /**
     * Executes the binding.
     * @param asynchronous Whether to execute the binding asynchronously.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public boolean execute(final boolean asynchronous)
            throws RemoteException {
        final StepIdentifier stepIdentifier = environment.getSimulator()
                                                         .nextStepIdentifier();
        Future<Boolean> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return binding.execute(stepIdentifier, asynchronous);
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
     * Returns the description of the binding.
     * @return The description.
     * @exception java.rmi.RemoteException if an RMI failure occured.
     */
    public String getDescription() throws RemoteException {
        Future<String> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<String>() {
                public String call() throws Exception {
                    return binding.getDescription();
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