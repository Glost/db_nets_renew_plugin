package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
final public class SingletonDefinition<T> extends AbstractDefinition<T> {
    private final T value;

    public SingletonDefinition(Class<?> service, T value) {
        super(service);
        this.value = value;
    }

    @Override
    public T create() {
        return value;
    }
}