package de.renew.plugin.di;

import java.util.concurrent.Callable;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
final public class FactoryDefinition<T> extends AbstractDefinition<T> {
    private Callable<T> factory;

    public FactoryDefinition(Class<?> service, Callable<T> factory) {
        super(service);
        setFactory(factory);
    }

    @Override
    public T create() {
        try {
            return factory.call();
        } catch (Exception e) {
            return null;
        }
    }

    public Callable<T> getFactory() {
        return factory;
    }

    public void setFactory(Callable<T> factory) {
        this.factory = factory;
    }
}