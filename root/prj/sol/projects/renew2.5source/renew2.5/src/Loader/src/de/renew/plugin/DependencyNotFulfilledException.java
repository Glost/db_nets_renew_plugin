package de.renew.plugin;

import java.util.Collection;


/**
 * This Exception is thrown by the DependencyCheckList if a
 * dependency relation is broken.
 *
 * @author J&ouml;rn Schumacher
 * @param <O>
 */
public class DependencyNotFulfilledException extends Exception {
    private Collection<?> _elements;

    public DependencyNotFulfilledException() {
        super();
    }

    public DependencyNotFulfilledException(String message) {
        super(message);
    }

    public DependencyNotFulfilledException(String message,
                                           Collection<?> elements) {
        super(message);
        this._elements = elements;
    }

    public Collection<?> getElements() {
        return _elements;
    }
}