/*
 * @(#)AbstractHandle.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * AbstractHandle provides default implementation for the
 * Handle interface.
 * <p>
 * Provides default undo support which can be avoided by
 * calling {@link #noChangesMade}.
 * </p>
 *
 * @see Figure
 * @see Handle
 * @see CH.ifa.draw.framework.UndoRedoManager
 */
public abstract class AbstractHandle implements Handle {

    /**
     * The standard size of a handle.
     */
    public static final int HANDLESIZE = 8;
    private Figure fOwner;
    private boolean didChange;

    /**
     * Initializes the owner of the figure.
     */
    public AbstractHandle(Figure owner) {
        fOwner = owner;
        noChangesMade();
    }

    /**
     * Locates the handle on the figure. The handle is drawn
     * centered around the returned point.
     */
    public abstract Point locate();

    /**
     * Tracks the start of the interaction. The default implementation
     * prepares an undo snapshot.
     * <p>
     * If the Handle does not modify the drawing, the undo
     * snapshot can be prevented by a call to {@link #noChangesMade}
     * before {@link #invokeEnd invokeEnd(...)} is called.
     * </p>
     * @param x the x position where the interaction started
     * @param y the y position where the interaction started
     * @param view the handles container
     */
    public void invokeStart(int x, int y, DrawingView view) {
        view.editor().prepareUndoSnapshot();
        changesMade();
    }

    public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
        invokeStart(x, y, view);
    }

    /**
     * Tracks a step of the interaction.
     * @param x the current x position
     * @param y the current y position
     * @param anchorX the x position where the interaction started
     * @param anchorY the y position where the interaction started
     */
    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        invokeStep(x, y, anchorX, anchorY, view);
    }

    /**
     *
     * @param x
     * @param y
     * @param anchorX
     * @param anchorY
     * @param view
     */
    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
    }

    /**
     * Tracks the end of the interaction. The default implementation commits
     * the undo snapshot prepared by {@link #invokeStart invokeStart()}.
     * @param x the current x position
     * @param y the current y position
     * @param anchorX the x position where the interaction started
     * @param anchorY the y position where the interaction started
     * @param view the drawing view where the user interaction occured.
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        if (didChange) {
            view.editor().commitUndoSnapshot();
        }
        noChangesMade();
    }

    public void invokeEnd(MouseEvent e, int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        invokeEnd(x, y, anchorX, anchorY, view);
    }

    /**
     * Inform the undo support that the drawing was not
     * modified. In consequence, no undo snapshot will
     * be taken on {@link #invokeEnd invokeEnd(...)}.
     **/
    protected void noChangesMade() {
        didChange = false;
    }

    /**
     * Inform the undo support that the drawing was or
     * will be modified. In consequence, an undo snapshot
     * will be taken on {@link #invokeEnd invokeEnd(...)}.
     **/
    protected void changesMade() {
        didChange = true;
    }

    /**
     * Gets the handle's owner.
     */
    public Figure owner() {
        return fOwner;
    }

    /**
     * Gets the display box of the handle.
     */
    public Rectangle displayBox() {
        Point p = locate();
        return new Rectangle(p.x - HANDLESIZE / 2, p.y - HANDLESIZE / 2,
                             HANDLESIZE, HANDLESIZE);
    }

    /**
     * Tests if a point is contained in the handle.
     */
    public boolean containsPoint(int x, int y) {
        return displayBox().contains(x, y);
    }

    /**
     * Draws this handle.
     */
    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.white);
        g.fillRect(r.x, r.y, r.width, r.height);

        g.setColor(Color.black);
        g.drawRect(r.x, r.y, r.width, r.height);
    }
}