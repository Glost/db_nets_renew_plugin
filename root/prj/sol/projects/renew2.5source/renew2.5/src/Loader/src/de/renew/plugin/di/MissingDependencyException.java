package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-10
 */
public class MissingDependencyException extends Exception {
    public MissingDependencyException(Class<?> dependency) {
        super(String.format("Dependency '%s' is missing",
                            dependency.getSimpleName()));
    }
}