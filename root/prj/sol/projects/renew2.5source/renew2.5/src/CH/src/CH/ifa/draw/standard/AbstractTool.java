/*
 * @(#)AbstractTool.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Tool;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.net.URI;


/**
 * Default implementation support for Tools.
 *
 * @see DrawingView
 * @see Tool
 */
public class AbstractTool implements Tool {
    protected DrawingEditor fEditor;

    /**
     * The position of the initial mouse down.
     */
    protected int fAnchorX;

    /**
     * The position of the initial mouse down.
     */
    protected int fAnchorY;

    /**
     * Constructs a tool for the given view.
     * @param editor the given view.
     */
    public AbstractTool(DrawingEditor editor) {
        fEditor = editor;
    }

    /**
     * Activates the tool for the given view. This method is called
     * whenever the user switches to this tool. Use this method to
     * reinitialize a tool.
     */
    public void activate() {
        fEditor.view().clearSelection();
        fEditor.view().checkDamage();
    }

    /**
     * Deactivates the tool. This method is called whenever the user
     * switches to another tool. Use this method to do some clean-up
     * when the tool is switched. Subclassers should always call
     * super.deactivate.
     */
    public void deactivate() {
        fEditor.view().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Handles mouse down events in the drawing view.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        fAnchorX = x;
        fAnchorY = y;
    }

    /**
     * Handles mouse drag events in the drawing view.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
    }

    /**
     * Handles mouse up in the drawing view.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
    }

    /**
     * Handles mouse moves (if the mouse button is up).
     */
    public void mouseMove(MouseEvent evt, int x, int y) {
    }

    /**
     * Handles key down events in the drawing view.
     */
    public void keyDown(KeyEvent evt, int key) {
        fEditor.dispatchEvent(evt);
    }

    /**
     * Gets the tool's drawing.
     * @return the tool's drawing.
     */
    public Drawing drawing() {
        return fEditor.drawing();
    }

    /**
     * Gets the tool's editor.
     * @return the tool's editor.
     */
    public DrawingEditor editor() {
        return fEditor;
    }

    /**
     * Gets the tool's view.
     * @return the tool's view.
     */
    public DrawingView view() {
        return fEditor.view();
    }

    /**
     * Provides a default implementation which draws nothing.
     * This should be appropriate for most tool implementations
     * since they use drawing figures to give visual feedback.
     **/
    public void draw(Graphics g) {
    }

    /**
     * Provides the possibility to extend the tool's action.
     * (Activated by ctrl-click.)
     *
     * @param uri - argument of the action.
     */
    public void simAccess(URI uri) {
    }
}