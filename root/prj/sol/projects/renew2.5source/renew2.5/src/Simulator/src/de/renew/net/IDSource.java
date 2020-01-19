package de.renew.net;



/**
 * This class allows the generation of unique IDs and
 * the registration of a factory that is responsible for
 * their creation.
 *
 * By default a trivial ID factory is used.
 *
 * @see TrivialIDFactory
 */
public class IDSource {

    /**
     * The factory that creates the IDs.
     */
    private static IDFactory factory = new TrivialIDFactory();

    /**
     * This class is totally static. One must not create instances
     * of it.
     */
    private IDSource() {
    }

    /**
     * Register a new factory.
     *
     * @param newFactory the factory to be registered
     */
    public synchronized static void setFactory(IDFactory newFactory) {
        factory = newFactory;
    }

    /**
     * Create a unique ID.
     *
     * @return the string representation of an ID
     */
    public synchronized static String createID() {
        return factory.createID();
    }
}