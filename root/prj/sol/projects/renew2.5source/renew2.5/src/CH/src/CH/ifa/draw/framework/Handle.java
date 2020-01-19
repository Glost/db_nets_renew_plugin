/*
 * @(#)Handle.java 5.1
 *
 */
package CH.ifa.draw.framework;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * Handles are used to change a figure by direct manipulation.
 * Handles know their owning figure and they provide methods to
 * locate the handle on the figure and to track changes.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld004.htm>Adapter</a></b><br>
 * Handles adapt the operations to manipulate a figure to a common interface.
 *
 * @see Figure
 */
public interface Handle {
    public static final int HANDLESIZE = 8;

    /**
     * Locates the handle on the figure. The handle is drawn
     * centered around the returned point.
     */
    public abstract Point locate();

    /**
     * Tracks the start of the interaction. The default implementation
     * does nothing.
     * @param e the mouse event which started the interaction
     * @param x the x position where the interaction started
     * @param y the y position where the interaction started
     * @param view the handles container
     */
    public void invokeStart(MouseEvent e, int x, int y, DrawingView view);

    /**
     * Tracks a step of the interaction.
     * @param e the mouse event which started the interaction
     * @param x the current x position
     * @param y the current y position
     * @param anchorX the x position where the interaction started
     * @param anchorY the y position where the interaction started
     */
    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view);

    /**
     * Tracks the end of the interaction.
     * @param e the mouse event which started the interaction
     * @param x the current x position
     * @param y the current y position
     * @param anchorX the x position where the interaction started
     * @param anchorY the y position where the interaction started
     */
    public void invokeEnd(MouseEvent e, int x, int y, int anchorX, int anchorY,
                          DrawingView view);

    /**
     * Gets the handle's owner.
     */
    public Figure owner();

    /**
     * Gets the display box of the handle.
     */
    public Rectangle displayBox();

    /**
     * Tests if a point is contained in the handle.
     */
    public boolean containsPoint(int x, int y);

    /**
     * Draws this handle.
     */
    public void draw(Graphics g);
}