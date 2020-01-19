package de.renew.application;



/**
 * Indicates that a given shadow net system did not contain
 * information about the net compiler to use or that the
 * specified compiler could not be applied.
 * <p>
 * FIXME: Class not in use since the SimulatorPlugin no longer maintains a
 * main ShadowNetSystem.
 * </p>
 **/
public class IllegalCompilerException extends IllegalArgumentException {

    /**
     * Creates a new <code>IllegalCompilerException</code> with no
     * detail message.
     **/
    public IllegalCompilerException() {
        super();
    }

    /**
     * Creates a new <code>IllegalCompilerException</code> with the
     * specified detail message.
     *
     * @param message  the detail message.
     **/
    public IllegalCompilerException(String message) {
        super(message);
    }
}