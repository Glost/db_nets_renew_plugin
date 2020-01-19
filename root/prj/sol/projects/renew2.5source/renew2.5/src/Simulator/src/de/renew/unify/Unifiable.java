package de.renew.unify;



/**
 * I am the common interface to all of the classes calculator,
 * tuple, list, unknown, and variable. If further subclasses
 * are added, the classes Unify and Copier must be updated.
 **/
interface Unifiable {
    boolean isComplete();

    boolean isBound();
}