package de.renew.engine.common;

import de.renew.engine.events.Firing;
import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.TransitionInstance;


/**
 * I am a special executable. I will output a trace message
 * during execution. I am useful for debugging purposes
 * and for console traces of firing sequences.
 **/
public class TraceExecutable implements LateExecutable {

    /**
     * A transition to which the trace output is related.
     */
    private TransitionInstance transitionInstance;

    /**
     * I (a trace executable) am created. I store the argument
     * message for further procession during the execute phase.
     *
     * @param message
     *   the message that I must output
     **/
    public TraceExecutable(String message, TransitionInstance transitionInstance) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.transitionInstance = transitionInstance;
    }

    /**
     * I will always act during the phase 0. Maybe at some
     * time this could be configured.
     *
     * @return 0 (the phase that I must await)
     **/
    public int phase() {
        return TRACE;
    }

    /**
     * I output my message.
     *
     **/
    public void execute(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        SimulatorEventLogger.log(stepIdentifier,
                                 new Firing(transitionInstance),
                                 transitionInstance);
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
    }

    /**
     * I cannot produce side effects that require the attention of the
     * simulation engine.
     *
     * @return false
     **/
    public boolean isLong() {
        return false;
    }
}