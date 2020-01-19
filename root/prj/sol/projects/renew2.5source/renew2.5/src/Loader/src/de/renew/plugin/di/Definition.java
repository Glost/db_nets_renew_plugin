package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
public interface Definition<T> {

    /**
     * Creates a service instance.
     *
     * @return The new instance.
     */
    T create();

    /**
     * @return The service being defined.
     */
    Class<?> getService();
}