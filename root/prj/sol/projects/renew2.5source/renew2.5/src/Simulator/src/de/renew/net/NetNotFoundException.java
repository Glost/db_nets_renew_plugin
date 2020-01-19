package de.renew.net;



/**
 * Thrown when trying to access a net by its name, but no
 * definition for the net with the specified name could be found.
 * <p>
 * Thrown by {@link Net#forName} or
 * {@link de.renew.net.loading.NetLoader#loadNet}.
 * </p>
 * NetNotFoundException.java
 * Created: Tue Dec  4  2001
 * @author Michael Duvigneau
 **/
public class NetNotFoundException extends Exception {

    /**
     * Constructs a <code>NetNotFoundException</code> with no
     * detail message.
     **/
    public NetNotFoundException() {
        super();
    }

    /**
     * Constructs a <code>NetNotFoundException</code> with an
     * exception that was raised while trying to load the net.
     *
     * @param cause    the exception that was raised while loading
     *                 the net.
     **/
    public NetNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a <code>NetNotFoundException</code> with the
     * specified detail message.
     *
     * @param message  the detail message. Typically the name of
     *                 the missing net.
     **/
    public NetNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>NetNotFoundException</code> with the
     * specified detail message and optional exception that was
     * raised while loading the net.
     *
     * @param message  the detail message. Typically the name of
     *                 the missing net.
     * @param cause    the exception that was raised while loading
     *                 the net.
     **/
    public NetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}