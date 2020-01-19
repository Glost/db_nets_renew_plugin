package de.renew.engine.simulator;

import de.renew.engine.common.StepIdentifier;


/**
 * A simulator controls the search for and activation of
 * bindings.
 * <p>
 * A simulator is part of the simulation facade. Therefore,
 * implementations should automatically switch to simulation
 * threads when appropriate: With exception of {@link #isActive()}
 * and {@link #isSequential()}, this applies to all methods declared
 * in this interface. The two methods that just query the state can
 * be called within any thread.
 * </p>
 * <p>
 * However, the constructor and any implementation-dependent code
 * of simulators may assume that it is executed within a simulation
 * thread.
 * </p>
 *
 * @author kummer
 *
 */
public interface Simulator {

    /**
     * The simulation was halted before I could determine
     * enabledness.
     **/
    public final static int statusStopped = 0;

    /**
     * The simulation of the step was completed.
     **/
    public final static int statusStepComplete = 1;

    /**
     * The simulation of the step was completed.
     * There are no more enabled bindings.
     **/
    public final static int statusLastComplete = 2;

    /**
     * There is no more enabled transition.
     **/
    public final static int statusCurrentlyDisabled = 3;

    /**
     * There will never be an enabled transition.
     **/
    public final static int statusDisabled = 4;

    /**
     * Return true, if the simulation could possibly
     * continue in the future.
     **/
    public boolean isActive();

    /**
     * Start the simulation in the background.
     **/
    public void startRun();

    /**
     * Gently stop the simulation.
     **/
    public void stopRun();

    /**
     * Terminate the simulation once and for all.
     * Do some final clean-up and exit all threads.
     **/
    public void terminateRun();

    /**
     * Perform just one more step. Return a status code according
     * to the five possibilities above.
     **/
    public int step();

    /**
     * Make sure to refresh internal data structures after the
     * firing of a transition outside the control of this simulator.
     * This is be required by simulators that cache possible bindings.
     **/
    public void refresh();

    /**
     * Tells whether the simulator operates in a strictly
     * sequential manner.
     **/
    public boolean isSequential();

    /**
     * Returns the next simulation step identifier
     */
    public StepIdentifier nextStepIdentifier();

    /**
     * Returns the current simulation step identifier
     */
    public StepIdentifier currentStepIdentifier();

    /**
     * Collect all simulation run IDs of this simulator (all its aggregated
     * instances).
     **/
    public long[] collectSimulationRunIds();
}