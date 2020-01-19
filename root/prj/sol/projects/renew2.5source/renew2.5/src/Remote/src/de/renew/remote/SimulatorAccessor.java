/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package de.renew.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * A simulator accessor allows an application to control
 * a remote simulator by starting and stopping the search process,
 * monitoring firing transition, listing well-known net instances,
 * etc.
 *
 * A simulator accessor can be registered at the RMI registry as a
 * starting point for remote accesses.
 *
 * Many methods in this interface delegate to methods of a
 * simulator object.
 *
 * @see de.renew.engine.simulator.Simulator
 */
public interface SimulatorAccessor extends Remote {

    /**
     * Get accessors for all registered net instances.
     * Not all net instances have to be registered (maybe none is),
     * but on the other hand, there may be many registered net instances.
     *
     * This method provides a starting point to view individual
     * net instances. The net instances are returned in no particular
     * order, but they may be queried for their name etc.
     *
     * @return an array of net instance accessors for registered
     * net instances
     */
    public NetInstanceAccessor[] getNetInstances() throws RemoteException;

    /**
     * Query whether the simulator is still running.
     *
     * return true, if simulator could possibly make another step
     * in the future.
     */
    public boolean isActive() throws RemoteException;

    /**
     * Start the simulator in the background. Return immediately.
     */
    public void startRun() throws RemoteException;

    /**
     * Stop the simulator as soon as possible.
     * Already firing transitions may continue to fire after
     * this method has returned, but no new transitions will
     * start firing.
     */
    public void stopRun() throws RemoteException;

    /**
     * Terminate the simulator once and for all.
     * Do some final clean-up and exit all threads.
     *
     * Already firing transitions may continue to fire after
     * this method has returned, but no new transitions will
     * start firing nor is it allowed to restart the simulator
     * by another call.
     */
    public void terminateRun() throws RemoteException;

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
    public int step() throws RemoteException;

    /**
     * Make sure to refresh internal data structures after the
     * firing of a transition outside the control of this simulator.
     * This is be required by simulators that cache possible bindings.
     */
    public void refresh() throws RemoteException;
}