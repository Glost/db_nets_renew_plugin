package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
abstract class AbstractDefinition<T> implements Definition<T> {
    private final Class<?> service;

    public AbstractDefinition(Class<?> service) {
        this.service = service;
    }

    final public Class<?> getService() {
        return service;
    }
}