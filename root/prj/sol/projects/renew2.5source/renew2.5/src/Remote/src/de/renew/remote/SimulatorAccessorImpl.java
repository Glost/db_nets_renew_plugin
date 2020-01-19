package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.engine.simulator.BindingList;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;
import de.renew.net.NetInstanceList;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * A simulator accessor allows an application to control
 * a remote simulator by starting and stopping the search process,
 * monitoring firing transitions, listing well-known net instances,
 * etc.
 *
 * A simulator accessor can be registered at the RMI registry as a
 * starting point for remote accesses.
 */
public class SimulatorAccessorImpl extends UnicastRemoteObject
        implements SimulatorAccessor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimulatorAccessorImpl.class);
    private final SimulationEnvironment environment;

    public SimulatorAccessorImpl(SimulationEnvironment environment)
            throws RemoteException {
        super(0, SocketFactoryDeterminer.getInstance(),
              SocketFactoryDeterminer.getInstance());
        this.environment = environment;
    }

    public NetInstanceAccessor[] getNetInstances() throws RemoteException {
        Future<NetInstanceAccessor[]> future = SimulationThreadPool.getCurrent()
                                                                   .submitAndWait(new Callable<NetInstanceAccessor[]>() {
                public NetInstanceAccessor[] call() throws Exception {
                    NetInstance[] netInstances = NetInstanceList
                                                   .getAll();
                    NetInstanceAccessor[] result = new NetInstanceAccessor[netInstances.length];

                    for (int i = 0; i < netInstances.length; i++) {
                        result[i] = new NetInstanceAccessorImpl(netInstances[i],
                                                                environment);
                    }
                    return result;
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
     * Query whether the simulator is still running.
     *
     * return true, if simulator could possibly make another step
     * in the future.
     */
    public boolean isActive() throws RemoteException {
        Future<Boolean> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return environment.getSimulator().isActive();
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
     * Start the simulator in the background. Return immediately.
     */
    public void startRun() throws RemoteException {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    environment.getSimulator().startRun();
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
     * Stop the simulator as soon as possible.
     * Already firing transitions may continue to fire after
     * this method has returned, but no new transitions will
     * start firing.
     */
    public void stopRun() throws RemoteException {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    environment.getSimulator().stopRun();
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
     * Terminate the simulator once and for all.
     * Do some final clean-up and exit all threads.
     *
     * Already firing transitions may continue to fire after
     * this method has returned, but no new transitions will
     * start firing nor is it allowed to restart the simulator
     * by another call.
     */
    public void terminateRun() throws RemoteException {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    environment.getSimulator().terminateRun();
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
     * Try to perform one more step, then stop.
     * Return a status code according
     * to the five possibilities listed in the Simulator interface.
     *
     * @see de.renew.engine.simulator.Simulator#statusStopped
     * @see de.renew.engine.simulator.Simulator#statusStepComplete
     * @see de.renew.engine.simulator.Simulator#statusLastComplete
     * @see de.renew.engine.simulator.Simulator#statusCurrentlyDisabled
     * @see de.renew.engine.simulator.Simulator#statusDisabled
     *
     * @return the status code after the possible step
     */
    public int step() throws RemoteException {
        Future<Integer> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Integer>() {
                public Integer call() throws Exception {
                    return environment.getSimulator().step();
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
        return -1;

    }

    /**
     * Make sure to refresh internal data structures after the
     * firing of a transition outside the control of this simulator.
     * This is be required by simulators that cache possible bindings.
     */
    public void refresh() throws RemoteException {
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    environment.getSimulator().refresh();
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
     * Stop the simulator and return only after the last transition
     * has completed firing.
     */
    public void totallyStopSimulation() throws RemoteException { //NOTICEthrows
        Future<Object> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    environment.getSimulator().stopRun();
                    BindingList.waitUntilEmpty();
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