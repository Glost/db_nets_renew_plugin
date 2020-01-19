package de.renew.plugin.load;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * This interface combines the basic methods of the interface
 * {@link PropertyChangeListener}<br>
 * and the method {@link Component#setBounds(int, int, int, int)} to resize and
 * move the component which implements this interface.
 *
 * @author Eva Mueller
 * @date Nov 27, 2010
 * @version 0.2
 * @update Jan 23, 2012 Dominic Dibbern
 */
public interface IExtendedProgressBar extends PropertyChangeListener {

    /**
     * Get the current state of the progress specified by an {@link Integer}.
     *
     * @return {@link Integer} The actual progress.
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public int getValue();

    /**
     * Moves and resizes this component. The new location of the top-left corner
     * is specified by <code>x</code> and <code>y</code>, and the new size is
     * specified by <code>width</code> and <code>height</code>.
     *
     * @param x
     *            the new <i>x</i>-coordinate of this component
     * @param y
     *            the new <i>y</i>-coordinate of this component
     * @param width
     *            the new <code>width</code> of this component
     * @param height
     *            the new <code>height</code> of this component
     * @see {@link Component#setBounds(int, int, int, int)}
     */
    public void setBounds(int x, int y, int width, int height);

    /**
     * Notify about the change of a property.<br>
     * If propertyName is <b>progress</b> then the {@link JProgressBar} will be
     * updated.<br>
     * If propertyName is <b>pluginLoaded</b> then the {@link JTextArea} will be
     * updated.
     */
    public void propertyChange(PropertyChangeEvent evt);

    public void close();
}