/*
 * @(#)ChildDragTracker.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.ParentFigure;
import CH.ifa.draw.framework.PartialSelectableFigure;

import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * ChildDragTracker implements the dragging of a clicked
 * ChildFigure.
 *
 * @see SelectionTool
 */
public class ChildDragTracker extends DragTracker {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ChildDragTracker.class);

    /**
     * Tells whether we have to track the anchor figure to detect alternate
     * parent figures. If <code>false</code>, this class behaves like its
     * superclass.
     **/
    private boolean doTracking = false;

    /**
     * Tells whether we just started the <code>doTracking</code> mode.
     * Then the first move will bring the anchor figure to the front of the
     * drawing.
     **/
    private boolean startTracking = false;

    /**
     * Stores the original location of the anchor figure, so we can
     * compute the offset to its original parent figure later.
     **/
    private Point startPoint;

    /**
     * Keeps track of the last figure found behind the current position of
     * the anchor figure. The <code>lastFigure</code> is not necessarily a
     * parent figure. It may also be <code>null</code>, if there is no
     * figure at the current location.
     **/
    private Figure lastFigure = null;

    /**
     * Stores the original parent of the anchor figure.
     **/
    private ParentFigure oldParent;

    /**
     * Keeps track of the current figure that could serve as alternate
     * parent for the anchor figure.
     **/
    private ParentFigure newParent = null;

    public ChildDragTracker(DrawingEditor editor, ChildFigure anchor) {
        super(editor, anchor);
        startPoint = anchor.center();
        oldParent = anchor.parent();
    }

    public void mouseDown(MouseEvent e, int x, int y) {
        super.mouseDown(e, x, y);
        doTracking = fAnchorFigure != null
                     && fAnchorFigure instanceof ChildFigure
                     && view().selectionCount() == 1;
        startTracking = doTracking;
    }

    protected void drag(int dx, int dy) {
        if (doTracking) {
            if (startTracking && ((dx != 0) || (dy != 0))) {
                fEditor.drawing().bringToFront(fAnchorFigure);
                startTracking = false;
            }
            fAnchorFigure.moveBy(dx, dy);
        } else {
            super.drag(dx, dy);
        }
    }

    private void findNewParent(MouseEvent e, int x, int y) {
        Figure figure = drawing().findFigureWithout(x, y, fAnchorFigure);
        boolean selectableFigureFound = true; //figure != null;
        if (figure instanceof PartialSelectableFigure) {
            PartialSelectableFigure partFig = (PartialSelectableFigure) figure;
            selectableFigureFound = false;
            if (partFig.isModifierSelectable() && e.isAltDown()) {
                selectableFigureFound = true;
            } else if (partFig.isSelectableInRegion(x, y)) {
                selectableFigureFound = true;
            }
        }

        // select new found figure if it is not the last selected figure
        if (selectableFigureFound && figure != lastFigure) {
            lastFigure = figure;
            if (figure instanceof ParentFigure
                        && ((ChildFigure) fAnchorFigure).parent() != figure
                        && ((ChildFigure) fAnchorFigure).canBeParent((ParentFigure) figure)) {
                //Toolkit.getDefaultToolkit().beep();
                newParent = (ParentFigure) figure;
                view().clearSelection();
                view().addToSelection(newParent);
            } else if (newParent != null && oldParent != null) {
                view().clearSelection();
                view().addToSelection(oldParent);
                newParent = null;
            }
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
        super.mouseDrag(e, x, y);
        if (doTracking) {
            findNewParent(e, x, y);
        }
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        super.mouseUp(e, x, y);
        if (doTracking && fAnchorFigure != null) {
            Point endPoint = fAnchorFigure.center();
            if (!startPoint.equals(endPoint)) {
                findNewParent(e, x, y);
                if (newParent != null) {
                    logger.debug("New Parent " + newParent + "!");
                    fAnchorFigure.moveBy(startPoint.x - endPoint.x,
                                         startPoint.y - endPoint.y);
                    ((ChildFigure) fAnchorFigure).setParent(newParent);
                }
            }
        }
    }
}