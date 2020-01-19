package de.renew.refactoring.inline;



/**
 * Interface for inline steps.
 *
 * @see InlineController
 * @author 2mfriedr
 */
public interface InlineStep {

    /**
     * Adds a listener.
     *
     * @param listener the listener
     */
    public void addListener(InlineStepListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener
     */
    public void removeListener(InlineStepListener listener);
}