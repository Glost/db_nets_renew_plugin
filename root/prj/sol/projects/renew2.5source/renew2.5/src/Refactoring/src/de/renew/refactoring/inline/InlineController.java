package de.renew.refactoring.inline;

import de.renew.refactoring.wizard.WizardController;


/**
 * Interface for inline controllers.
 * The method {@link #nextStep()} is only used as a structural hint for inline
 * controller implementations.
 *
 * @see WizardController
 * @author 2mfriedr
 */
public interface InlineController {

    /**
     * Returns the next step, similarly to {@link WizardController#getNextPage()}.
     */
    public InlineStep nextStep();

    /**
     * Adds a listener.
     *
     * @param listener the listener
     */
    public void addListener(InlineListener listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener
     */
    public void removeListener(InlineListener listener);
}