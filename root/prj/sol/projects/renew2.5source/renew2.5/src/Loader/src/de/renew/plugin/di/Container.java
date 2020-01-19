package de.renew.plugin.di;

import java.util.HashMap;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
public class Container implements ServiceContainer {
    private final HashMap<Class<?>, Definition<?>> definitions;

    public Container() {
        definitions = new HashMap<Class<?>, Definition<?>>();
    }

    @Override
    public boolean has(Class<?> service) {
        return definitions.containsKey(service);
    }

    @Override
    public Object get(Class<?> service) throws MissingDependencyException {
        if (!has(service)) {
            throw new MissingDependencyException(service);
        }

        return definitions.get(service).create();
    }

    @Override
    public <T> void set(Class<?> service, T singleton) {
        addDefinition(new SingletonDefinition<T>(service, singleton));
    }

    @Override
    public void addDefinition(Definition<?> definition) {
        definitions.put(definition.getService(), definition);
    }
}