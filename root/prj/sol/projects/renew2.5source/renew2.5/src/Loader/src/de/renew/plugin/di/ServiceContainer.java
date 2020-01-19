package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
public interface ServiceContainer {
    boolean has(Class<?> service);

    Object get(Class<?> service) throws MissingDependencyException;

    <T> void set(Class<?> service, T singleton);

    void addDefinition(Definition<?> definition);
}