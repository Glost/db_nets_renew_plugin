package de.renew.refactoring.inline;



/**
 * Interface for inline step listeners.
 *
 * @author 2mfriedr
 */
public interface InlineStepListener {

    /**
     * Informs the listener that the step was finished.
     */
    public void inlineStepFinished();

    /**
     * Informs the listener that the step was cancelled, e.g. by clicking
     * outside the step's interface.
     */
    public void inlineStepCancelled();
}