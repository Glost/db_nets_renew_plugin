package de.renew.refactoring.wizard;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


/**
 * Base class for wizard pages that show a progress bar before the actual
 * content is loaded.
 *
 * @author 2mfriedr
 */
public abstract class ProgressBarWizardPage<T> extends WizardPage {
    private JProgressBar _progressBar;
    private JLabel _statusLabel;
    private boolean _isInProgress = true;

    public ProgressBarWizardPage(String id) {
        super(id);
        JPanel panel = getPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        _progressBar = new JProgressBar();
        panel.add(_progressBar);
        _statusLabel = new JLabel();
        panel.add(_statusLabel);
    }

    /**
     * Sets the progress of the progress bar.
     *
     * @param progress the progress
     */
    public void setProgress(final int progress) {
        _progressBar.setValue(progress);
    }

    /**
     * Sets the text of the status label.
     *
     * @param status the text
     */
    public void setStatus(final String status) {
        _statusLabel.setText(status);
    }

    /**
     * Sets the text of the status label to a string with format {@code
     * <html>[status]: <i>[itemName]</i></html>}.
     *
     * @param status the status
     * @param itemName the item name
     */
    public void setStatus(final String status, final String itemName) {
        setStatus("<html>" + status + ": <i>" + itemName + "</i></html>");
    }

    /**
     * Returns the progress status.
     *
     * @return {@code true} if {@link #done(Object)} has not been called,
     * otherwise {@code false}
     */
    public boolean isInProgress() {
        return _isInProgress;
    }

    /**
     * This method should be called if the progress is done and the page should
     * load its actual content. The progress bar and status label are removed
     * and {@link #progressDone(Object)} is called.
     *
     * @param result an arbitrary object of type T
     */
    public final void done(T result) {
        _isInProgress = false;
        JPanel panel = getPanel();
        restoreDefaultLayout();

        panel.remove(_progressBar);
        panel.remove(_statusLabel);
        panel.repaint();
        panel.revalidate();

        _progressBar = null;
        _statusLabel = null;

        progressDone(result);
    }

    /**
     * Override point for subclasses.
     *
     * @param result an object representing the result of the operation
     */
    protected abstract void progressDone(T result);
}