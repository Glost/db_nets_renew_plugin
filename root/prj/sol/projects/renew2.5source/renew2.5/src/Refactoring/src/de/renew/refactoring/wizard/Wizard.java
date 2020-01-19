package de.renew.refactoring.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;


/**
 * <p>This abstract class implements a wizard.</p>
 * <p>It provides a window with three buttons (previous, next, cancel) and a
 * panel for the wizard's pages.</p>
 *
 * <p>The wizard is controlled by a {@link WizardController} that is provided
 * in the constructor. The controller must implement {@link
 * WizardController#nextPage()}.</p>
 *
 * <p>Wizard pages are managed in a stack. This means that all pages that have
 * been pushed onto the stack will appear again if the previous button is
 * pushed the correspondent number of times. (The previous button can be
 * disabled by calling {@link #setPreviousButtonEnabled(boolean)}
 * </p>
 *
 * <p>Pressing the next button will:</p>
 * <ul>
 *   <li>call the current page's {@link WizardPage#saveState()} method</li>
 *   <li>determine the next page by calling {@link #nextPage()}</li>
 *   <li>call the current page's {@link WizardPage#willDisappear()} method</li>
 *   <li>push the next page to the page stack</li>
 *   <li>call the next page's {@link WizardPage#didAppear()} method</li>
 * </ul>
 *
 * <p>Pressing the previous button will:</p>
 * <ul>
 *   <li>call the current page's {@link WizardPage#resetState()} method</li>
 *   <li>call the current page's {@link WizardPage#willDisappear()} method</li>
 *   <li>remove the current page from the page stack</li>
 *   <li>call the previous page's {@link WizardPage#didAppear()} method</li>
 * </ul>
 *
 * <p>Pressing the cancel button or (closing the window) will:</p>
 * <ul>
 *   <li>call the current page's {@link WizardPage#resetState()} method</li>
 *   <li>call the current page's {@link WizardPage#willDisappear()} method</li>
 *   <li>remove the current page from the page stack</li>
 *   <li>repeat for all other pages on the stack</li>
 *   <li>close the window</li>
 * </ul>
 *
 * <p>This class is heavily inspired by the <a
 * href="http://www.oracle.com/technetwork/articles/javase/wizard-136789.html">
 * Oracle Tech Network article on wizard dialogs by Robert Eckstein</a>.</p>
 *
 * @see WizardPage
 * @author 2mfriedr
 */
public class Wizard extends WindowAdapter implements ActionListener,
                                                     WizardButtons {
    private static final String PREVIOUS_BUTTON_ACTION_COMMAND = "PREVIOUS";
    private static final String NEXT_BUTTON_ACTION_COMMAND = "NEXT";
    private static final String CANCEL_BUTTON_ACTION_COMMAND = "CANCEL";
    private JDialog _dialog;
    private JPanel _pagePanel;
    private WizardPageStack _pages = new WizardPageStack();
    private WizardController _controller;
    private Set<WizardListener> _listeners = new HashSet<WizardListener>();
    private JButton _previousButton;
    private JButton _nextButton;
    private JButton _cancelButton;
    private boolean _nextButtonFinish = false;

    /**
     * Constructs a new wizard.
     *
     * @param controller the wizard controller
     */
    public Wizard(final WizardController controller) {
        _controller = controller;
        _controller.setWizardButtons(this);
        initComponents(_controller.getTitle());
        start();
    }

    /**
     * Shows the wizard window and presents the first page.
     */
    private void start() {
        showNextPage();
        _dialog.setVisible(true);
    }

    /**
     * Adds a wizard listener.
     *
     * @param listener the listener
     */
    public boolean addListener(WizardListener listener) {
        return _listeners.add(listener);
    }

    /**
     * Removes a wizard listener.
     *
     * @param listener the listener
     */
    public boolean removeListener(WizardListener listener) {
        return _listeners.remove(listener);
    }

    private void initComponents(final String title) {
        _dialog = new JDialog();
        _dialog.setTitle(title);
        _dialog.setSize(500, 500);
        _dialog.setMinimumSize(new Dimension(300, 300));
        _dialog.getContentPane().setLayout(new BorderLayout());
        _dialog.addWindowListener(this);

        JPanel buttonPanel = new JPanel();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        _pagePanel = new JPanel();
        _pagePanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        _pagePanel.setLayout(new CardLayout());

        _previousButton = new JButton("Back");
        _nextButton = new JButton("Next");
        _cancelButton = new JButton("Cancel");

        _previousButton.setActionCommand(PREVIOUS_BUTTON_ACTION_COMMAND);
        _nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        _cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

        _previousButton.addActionListener(this);
        _nextButton.addActionListener(this);
        _cancelButton.addActionListener(this);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(_previousButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(_nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(_cancelButton);
        buttonPanel.add(buttonBox, BorderLayout.EAST);
        _dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        _dialog.getContentPane().add(_pagePanel, BorderLayout.CENTER);

        _dialog.getRootPane().setDefaultButton(_nextButton);
    }

    private void showNextPage() {
        save();

        WizardPage next = _controller.getNextPage();

        if (next == null) {
            return;
        }

        next.didLoad();
        _pages.push(next);
        _pagePanel.add(next.getPanel(), next.getId());

        configureForPage(next);
    }

    private void save() {
        WizardPage current = getCurrentPage();
        if (current != null) {
            current.saveState();
            current.willDisappear();
        }
    }

    private void showPreviousPage() {
        popCurrentPage();
        configureForPage(getCurrentPage());
    }

    private void popCurrentPage() {
        WizardPage current = getCurrentPage();
        current.resetState();
        current.willDisappear();
        _pages.pop();
        _pagePanel.remove(current.getPanel());
    }

    private void cancel() {
        while (_pages.size() > 0) {
            popCurrentPage();
        }
        dispose();
    }

    private void dispose() {
        informListeners();
        _dialog.dispose();
    }

    private void configureForPage(WizardPage page) {
        _pagePanel.revalidate();
        ((CardLayout) _pagePanel.getLayout()).show(_pagePanel, page.getId());

        internalSetPreviousButtonEnabled(_pages.size() > 1);
        setNextButtonTitleToFinish(page.isLastPage());

        String nextButtonTitle = page.nextButtonTitle();
        if (nextButtonTitle != null) {
            setNextButtonTitle(nextButtonTitle);
        }

        page.internalDidAppear();
    }

    private WizardPage getCurrentPage() {
        return _pages.peek();
    }

    // Window Listener
    @Override
    public void windowClosing(WindowEvent e) {
        cancel();
    }

    private void informListeners() {
        for (WizardListener listener : _listeners) {
            listener.wizardFinished();
        }
    }

    // Action Listener
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(PREVIOUS_BUTTON_ACTION_COMMAND)) {
            backButtonPressed();
        } else if (e.getActionCommand().equals(NEXT_BUTTON_ACTION_COMMAND)) {
            if (_nextButtonFinish) {
                finishButtonPressed();
            } else {
                nextButtonPressed();
            }
        } else if (e.getActionCommand().equals(CANCEL_BUTTON_ACTION_COMMAND)) {
            cancelButtonPressed();
        }
    }

    private void backButtonPressed() {
        showPreviousPage();
    }

    private void nextButtonPressed() {
        showNextPage();
    }

    private void finishButtonPressed() {
        save();
        dispose();
    }

    private void cancelButtonPressed() {
        cancel();
    }


    /**
     * Sets the previous button's state.
     *
     * @param enabled {@code true} if the previous button should be enabled,
     * {@code false} if the previous button should be disabled
     */
    private void internalSetPreviousButtonEnabled(final boolean enabled) {
        _previousButton.setEnabled(enabled);
    }


    // Wizard Button Facade
    @Override
    public void setPreviousButtonEnabled(final boolean enabled) {
        if (enabled) {
            return;
        }
        _previousButton.setEnabled(false);
    }

    @Override
    public void setNextButtonEnabled(final boolean enabled) {
        _nextButton.setEnabled(enabled);
    }

    @Override
    public void focusNextButton() {
        _nextButton.requestFocusInWindow();
    }

    /**
     * Sets the next button's title to "Next" or "Finish".
     *
     * @param finish {@code true} if the back button's title should be set to
     * "Finish", otherwise {@code false}
     */
    private void setNextButtonTitleToFinish(final boolean finish) {
        _nextButtonFinish = finish;
        _nextButton.setText(finish ? "Finish" : "Next");
    }

    private void setNextButtonTitle(final String title) {
        _nextButton.setText(title);
    }
}