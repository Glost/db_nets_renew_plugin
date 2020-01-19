/*
 * @(#)CreationTool.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.HJDError;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A tool to create new figures. The figure to be
 * created is specified by a prototype.
 *
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld029.htm>Prototype</a></b><br>
 * CreationTool creates new figures by cloning a prototype.
 * <hr>
 * @see Figure
 * @see Object#clone
 */
public class CreationTool extends UndoableTool {

    /**
     * the anchor point of the interaction
     */
    private Point fAnchorPoint;

    /**
     * the currently created figure
     */
    private Figure fCreatedFigure;

    /**
     * the prototypical figure that is used to create new figures.
     */
    private Figure fPrototype;

    /**
     * Initializes a CreationTool with the given prototype.
     */
    public CreationTool(DrawingEditor editor, Figure prototype) {
        super(editor);
        fPrototype = prototype;
    }

    /**
     * Constructs a CreationTool without a prototype.
     * This is for subclassers overriding createFigure.
     */
    protected CreationTool(DrawingEditor editor) {
        super(editor);
        fPrototype = null;
    }

    protected Figure getPrototype() {
        return fPrototype;
    }

    /**
     * Sets the cross hair cursor.
     */
    public void activate() {
        super.activate();
        view().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        fAnchorPoint = null;
        fCreatedFigure = null;
    }

    /**
     * Creates a new figure by cloning the prototype.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        fCreatedFigure = createFigure();
        if (e.isControlDown()) {
            fAnchorPoint = new Point(x,
                                     x - fCreatedFigure.displayBox().x
                                     + fCreatedFigure.displayBox().y);
        } else {
            fAnchorPoint = new Point(x, y);
        }
        fCreatedFigure.displayBox(fAnchorPoint, fAnchorPoint);
        view().add(fCreatedFigure);
        changesMade();
    }

    /**
     * Creates a new figure by cloning the prototype.
     */
    protected Figure createFigure() {
        if (fPrototype == null) {
            throw new HJDError("No prototype defined");
        }
        return (Figure) fPrototype.clone();
    }

    /**
     * Adjusts the extent of the created figure
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        if (e.isControlDown()) {
            fCreatedFigure.displayBox(fAnchorPoint,
                                      new Point(x,
                                                x
                                                - fCreatedFigure.displayBox().x
                                                + fCreatedFigure.displayBox().y));
        } else {
            fCreatedFigure.displayBox(fAnchorPoint, new Point(x, y));
        }
    }

    /**
     * Checks if the created figure is empty. If it is, the figure
     * is removed from the drawing.
     * @see Figure#isEmpty
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        if (fCreatedFigure.isEmpty()) {
            drawing().remove(fCreatedFigure);
            noChangesMade();
        }
        fCreatedFigure = null;
        editor().toolDone();
    }

    /**
     * Gets the currently created figure
     */
    protected Figure createdFigure() {
        return fCreatedFigure;
    }
}