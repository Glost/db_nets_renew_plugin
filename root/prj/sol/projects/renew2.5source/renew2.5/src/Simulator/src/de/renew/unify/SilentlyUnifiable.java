package de.renew.unify;

import java.util.Set;


/**
 * @author Friedrich Delgado Friedrichs
 *
 * Generic interface to allow any class implementing {@link Unifiable} to
 * specify its own implementation of unifySilently, without having to modify
 * Unify or Copier.
 */
public interface SilentlyUnifiable extends Unifiable {

    /**
     * @param right
     *            the object to unify with
     * @param recorder
     * @param listeners
     * @throws Impossible
     */
    public void unifySilently(Object right, StateRecorder recorder,
                              Set<Notifiable> listeners)
            throws Impossible;

    /**
     * @param copier
     * @return a fresh copy of the object
     */
    public SilentlyUnifiable copy(Copier copier);
}
