package de.renew.net;



/**
 * An ID factory is responsible for creating unique ID strings.
 * Depending on the requirements of the application, a factory
 * may generate IDs that are unique to this process, to this application,
 * or even globally unique.
 */
public interface IDFactory {

    /**
     * Create another ID.
     *
     * @return a unique string
     */
    public String createID();
}