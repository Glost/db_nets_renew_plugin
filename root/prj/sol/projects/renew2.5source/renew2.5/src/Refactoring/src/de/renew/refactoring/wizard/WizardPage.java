package de.renew.refactoring.wizard;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * This abstract class represents one page in a wizard. It is typically used as
 * an anonymous inner class.
 *
 * @see Wizard
 * @author 2mfriedr
 *
 */
public abstract class WizardPage {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(WizardPage.class);
    private String _id;
    private JPanel _panel;

    /**
     * Creates a new page with a specified identifier. The identifier must be
     * unique per wizard, as it is used to store and lookup the wizard's pages.
     * @param id the identifier
     */
    public WizardPage(final String id) {
        _panel = new JPanel();
        restoreDefaultLayout();
        _id = id;
    }

    /**
     * Returns the page's identifier.
     *
     * @return the identifier
     */
    public String getId() {
        return _id;
    }

    /**
     * Returns the page's panel.
     *
     * @return the panel
     */
    public JPanel getPanel() {
        return _panel;
    }

    /**
     * Restores the panel layout to the default {@link BoyLayout}. This is
     * useful for subclasses that change the layout.
     */
    void restoreDefaultLayout() {
        _panel.setLayout(new BoxLayout(_panel, BoxLayout.PAGE_AXIS));
    }

    /**
     * Override point for subclasses.
     * This method is called before {@link #didAppear()} to determine if the
     * next button's title should be set to "finish".
     *
     * @return {@code false}
     */
    protected boolean isLastPage() {
        return false;
    }

    /**
     * Override point for subclasses.
     * This method is called before {@link #didAppear()} to determine the next
     * button's title. If {@code null} is returned, the default title will be
     * used. {@link #isLastPage()} should still return {@code true} if the page
     * is the last page and has a custom title.
     *
     * @return {@code null}
     */
    protected String nextButtonTitle() {
        return null;
    }

    /**
     * Override point for subclasses.
     * This method is called before {@link #didAppear()} to determine the
     * page's initially focused component.
     *
     * @return {@code null}
     */
    protected Component initialFocusComponent() {
        return null;
    }

    /**
     * This method is called by {@link Wizard} before {@link #didAppear()}.
     * It can't be overridden by subclasses.
     */
    final void internalDidAppear() {
        final Component focusComponent = initialFocusComponent();

        if (focusComponent != null) {
            logger.debug("component requesting focus: " + focusComponent);
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        focusComponent.requestFocusInWindow();
                    }
                });
        }

        didAppear();
    }

    /**
     * Override point for subclasses.
     * This method is called only once per page, when the page is loaded and
     * before it is displayed ({@link #didAppear()}. It is typically used to
     * create and hook up the page's components.
     */
    protected void didLoad() {
    }

    /**
     * Override point for subclasses.
     * This method is called every time the page is displayed in the wizard.
     * It is typically used to set the initial state of the previous and next
     * buttons.
     */
    protected void didAppear() {
    }

    /**
     * Override point for subclasses.
     * This method is called when the page will disappear in the wizard.
     */
    protected void willDisappear() {
    }

    /**
     * This method is called when the page should save the information that was
     * acquired on the page, e.g. when the "next" or "finish" button is pressed.
     */
    protected abstract void saveState();

    /**
     * This method is called when the page should reset the information that
     * was acquired on the page, e.g. when the "previous" button is pressed or
     * the wizard is cancelled.
     */
    protected abstract void resetState();
}