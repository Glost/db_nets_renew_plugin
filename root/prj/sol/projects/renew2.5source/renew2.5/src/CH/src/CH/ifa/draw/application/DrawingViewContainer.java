package CH.ifa.draw.application;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;


/**
 * View container for a drawing.
 */
public interface DrawingViewContainer {

    /**
     * @return the location of the drawing view container.
     */
    public Point getLocation();

    /**
     * @return the size of the drawing view container.
     */
    public Dimension getSize();

    /**
     * Function called when the drawing view container releases the discard.
     */
    public void onDiscardRelease();

    /**
     * @return the view of the drawing view container.
     */
    public DrawingView view();

    /**
     * validates the drawing view container.
     */
    public void validate();

    /**
     * Sets the title of the drawing view container to the name of the given drawing.
     * @param drawing the drawing which name should be used as title of the drawing view container.
     */
    public void setTitle(Drawing drawing);

    /**
     * Sets the title to the given String.
     * @param title new title of the drawing view container.
     */
    public void setTitle(String title);

    /**
     * Shows the drawing view container.
     */
    public void setVisible(boolean b);

    /**
     * Requests the focus.
     */
    public void requestFocus();

    /**
     * Sets the state to the new value.
     * @param state the new state.
     */
    public void setState(int state);

    /**
     * @return the frame of the drawing view container.
     */
    public JFrame getFrame();

    /**
     * Discards the drawing view container.
     */
    public void discard();
}