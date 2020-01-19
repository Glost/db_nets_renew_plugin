/*
 * Created on Nov 19, 2004
 *
 */
package de.renew.engine.events;

import de.renew.engine.simulator.SimulationThreadPool;


/**
 * Informs about the start of a net simulation.
 *
 * @author Sven Offermann
 *
 */
public class SimulationStart implements SimulationEvent {
    public SimulationStart() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    public String toString() {
        return "New net simulation started...";
    }
}