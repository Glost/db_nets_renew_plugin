package de.renew.refactoring.wizard;

public interface WizardButtons {

    /**
     * Overrides the previous button's state. The button is normally enabled if
     * there is a previous page and disabled if there is none. If this method
     * is called, the previous button can be force disabled, e.g. if the last
     * wizard page is presented.
     *
     * @param enabled {@code false} if the previous button should be disabled.
     * {@code true} has no effect.
     */
    public void setPreviousButtonEnabled(boolean enabled);

    /**
     * Enables or disables the next button.
     *
     * @param enabled {@code true} if the next button should be enabled,
     * otherwise {@code false}
     */
    public void setNextButtonEnabled(boolean enabled);

    /**
     * Requests focus for the next button.
     */
    public void focusNextButton();
}