package de.renew.application;



/**
 * Indicates that the method can not be executed because a
 * simulation has been set up before. Some methods of the
 * {@link SimulatorPlugin} that try to set up exceptions fail
 * if there is already a simulation running.
 **/
public class SimulationRunningException extends IllegalStateException {

    /**
     * Creates a new <code>SimulationRunningException</code> with no
     * detail message.
     **/
    public SimulationRunningException() {
        super();
    }

    /**
     * Creates a new <code>SimulationRunningException</code> related to
     * the given cause.
     **/
    public SimulationRunningException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new <code>SimulationRunningException</code> with the
     * specified detail message.
     *
     * @param message  the detail message.
     **/
    public SimulationRunningException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>SimulationRunningException</code> with the
     * specified detail message and in relation to the given cause.
     *
     * @param message  the detail message.
     **/
    public SimulationRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}