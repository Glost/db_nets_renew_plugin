package de.renew.refactoring.wizard;

import javax.swing.JPanel;


/**
 * Base class for wizard page panels that are used for input.
 *
 * @author 2mfriedr
 */
public abstract class InputPanel<T> extends JPanel {
    private static final long serialVersionUID = -341172914205278992L;

    /**
    * Returns the current input.
    *
    * @return the input
    */
    public abstract T getInput();

    /**
     * This method should be called by subclasses when the input is changed.
     *
     * @param input the input
     */
    public void inputChanged(T input) {
    }

    /**
     * Requests focus for the input component.
     */
    public abstract void focus();
}