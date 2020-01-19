package de.renew.util;



/**
 * Indicates that the called object is not the singleton
 * instance of that object's class. This can happen if the
 * constructor is called when a singleton already exists. It
 * can also happen if a former singleton instance has been
 * retracted.
 **/
public class SingletonException extends IllegalStateException {

    /**
     * Creates a new <code>SingletonException</code> with no
     * detail message.
     **/
    public SingletonException() {
        super();
    }

    /**
     * Creates a new <code>SingletonException</code> with the
     * specified detail message.
     *
     * @param message  the detail message.
     **/
    public SingletonException(String message) {
        super(message);
    }
}