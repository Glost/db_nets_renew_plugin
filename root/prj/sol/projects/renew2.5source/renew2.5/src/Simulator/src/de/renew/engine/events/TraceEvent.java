/*
 * Created on Nov 19, 2004
 *
 */
package de.renew.engine.events;

import de.renew.engine.simulator.SimulationThreadPool;


/**
 * @author Sven Offerman
 *
 */
public class TraceEvent implements SimulationEvent {
    private String traceMessage;

    public TraceEvent(String traceMessage) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.traceMessage = traceMessage;
    }

    public String toString() {
        return this.traceMessage;
    }
}