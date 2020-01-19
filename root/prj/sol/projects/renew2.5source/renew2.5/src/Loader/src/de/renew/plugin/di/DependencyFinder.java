package de.renew.plugin.di;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-10
 */
public interface DependencyFinder {

    /**
     * Finds a dependency.
     *
     * Dependencies are found by a class name.
     *
     * @param type Type of dependency to find.
     * @return An instance of the required dependency.
     * @throws MissingDependencyException Exception will be thrown if the
     *                                    dependency could not be found.
     */
    Object findDependency(Class<?> type) throws MissingDependencyException;
}