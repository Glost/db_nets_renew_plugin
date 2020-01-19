/*
 * Created on 14.09.2005
 *
 */
package de.renew.engine.events;

import de.renew.engine.simulator.SimulationThreadPool;


/**
 * @author Sven Offermann
 *
 */
public abstract class ExceptionEvent implements SimulationEvent {
    private Throwable t;

    public ExceptionEvent(Throwable t) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.t = t;
    }

    /**
      * @return Returns the net instance.
      */
    public Throwable getException() {
        return this.t;
    }
}