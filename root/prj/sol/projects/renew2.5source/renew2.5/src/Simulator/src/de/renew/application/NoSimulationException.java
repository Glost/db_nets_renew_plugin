package de.renew.application;



/**
 * Indicates that the method can not be executed because no
 * simulation has been set up before. Many methods of the
 * {@link SimulatorPlugin} do not make sense before
 * {@link SimulatorPlugin#setupSimulation} has been called.
 **/
public class NoSimulationException extends IllegalStateException {

    /**
     * Creates a new <code>NoSimulationException</code> with no
     * detail message.
     **/
    public NoSimulationException() {
        super();
    }

    /**
     * Creates a new <code>NoSimulationException</code> with the
     * specified detail message.
     *
     * @param message  the detail message.
     **/
    public NoSimulationException(String message) {
        super(message);
    }
}