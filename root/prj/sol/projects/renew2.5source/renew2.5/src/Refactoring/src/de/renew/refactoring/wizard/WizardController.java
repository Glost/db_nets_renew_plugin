package de.renew.refactoring.wizard;



/**
 * Superclass for wizard controllers that provides methods for setting the
 * wizard buttons' states.
 *
 * @see Wizard
 * @author 2mfriedr
 */
public abstract class WizardController {

    /**
     * Returns the wizard's window title.
     *
     * @return the title
     */
    protected abstract String getTitle();

    /**
    * Returns the next page to be shown by the wizard.
    *
    * @return the next page
    */
    protected abstract WizardPage getNextPage();

    private WizardButtons _buttons;

    /**
     * This method provides the controller with a reference to the wizard
     * buttons. It is called in the wizard's constructor.
     *
     * @param buttons the proxy
     */
    protected final void setWizardButtons(WizardButtons buttons) {
        _buttons = buttons;
    }

    /**
     * @see WizardButtons#setPreviousButtonEnabled(boolean)
     */
    protected final void setPreviousButtonEnabled(boolean enabled) {
        if (_buttons != null) {
            _buttons.setPreviousButtonEnabled(enabled);
        }
    }

    /**
     * @see WizardButtons#setNextButtonEnabled(boolean)
     */
    protected final void setNextButtonEnabled(boolean enabled) {
        if (_buttons != null) {
            _buttons.setNextButtonEnabled(enabled);
        }
    }

    /**
     * @see WizardButtons#focusNextButton()
     */
    protected final void focusNextButton() {
        if (_buttons != null) {
            _buttons.focusNextButton();
        }
    }
}