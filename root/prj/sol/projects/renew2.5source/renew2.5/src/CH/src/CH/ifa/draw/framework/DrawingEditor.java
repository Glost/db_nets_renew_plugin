/*
 * @(#)DrawingEditor.java 5.1
 *
 */
package CH.ifa.draw.framework;

import CH.ifa.draw.application.DrawingViewContainer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;


/**
 * DrawingEditor defines the interface for coordinating
 * the different objects that participate in a drawing editor.
 *
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld022.htm>Mediator</a></b><br>
 * DrawingEditor is the mediator. It decouples the participants
 * of a drawing editor.
 *
 * @see Tool
 * @see DrawingView
 * @see Drawing
 */
public interface DrawingEditor {

    /**
     * Gets the editor's current drawing view.
     */
    DrawingView view();

    /**
     * Gets the drawing view that was active before the current drawing view.
     */
    DrawingView previousView();

    /**
     * Gets the editor's current drawing.
     */
    Drawing drawing();

    /**
     * Gets the editor's current tool.
     */
    Tool tool();

    /**
     * Gets the editor's default tool.
     * This is usually the selection tool.
     */
    Tool defaultTool();

    /**
     * Sets the sticky tool mode.
     */
    void setStickyTools(boolean sticky);

    /**
     * Gets the sticky tool mode.
     */
    boolean isStickyTools();

    /**
     * retrieves an Image from the editor, which can be used as the standard icon image.
     */
    public Image getIconImage();

    /**
     * processes a single event
     */
    public void dispatchEvent(KeyEvent evt);

    /**
     * Informs the editor that a tool has done its interaction.
     * This method can be used to switch back to the default tool.
     */
    void toolDone();

    /**
     * Informs that the current selection has changed.
     * Override this method to handle selection changes.
     */
    void selectionChanged(DrawingView view);

    /**
     * Informs that the menu entries should be rechecked
     * if they are enabled.
     **/
    void menuStateChanged();

    /**
     * Shows a status message in the editor's user interface
     */
    void showStatus(String string);

    /**
     * Takes a snapshot of the drawing currently active and
     * remembers it until it will be added by commitUndoSnapshot().
     * Any previously prepared snapshot will be forgotten.
     * <p>
     * Convenience method, equivalent to <code>
     * getUndoRedoManager().prepareUndoSnapshot(drawing())</code>.
     * </p>
     **/
    void prepareUndoSnapshot();

    /**
     * Takes the last prepared snapshot and
     * appends it to the undo history of the drawing.
     * The redo history is cleared.
     * <p>
     * Convenience method, equivalent to <code>
     * getUndoRedoManager().commitUndoSnapshot(drawing())</code>.
     * </p>
     **/
    void commitUndoSnapshot();

    /**
     * Returns the undo and redo history manager.
     **/
    UndoRedoManager getUndoRedoManager();

    public void drawingViewContainerActivated(DrawingViewContainer viewContainer);

    public void drawingViewContainerClosing(DrawingViewContainer viewContainer);

    void setCurrentDrawing(DrawingViewContainer dvc);

    Dimension getSize();

    Point getLocationOnScreen();
}