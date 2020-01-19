/*
 * @(#)PolyLineHandle.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.LocatorHandle;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A handle for a node on the polyline.
 */
public class PolyLineHandle extends LocatorHandle {
    private int fIndex;

    /**
     * Constructs a poly line handle.
     * @param owner the owning polyline figure.
     * @param l     the locator
     * @param index the index of the node associated with this handle.
     */
    public PolyLineHandle(PolyLineable owner, Locator l, int index) {
        super(owner, l);
        fIndex = index;
    }

    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        Point p;
        PolyLineable plf = myOwner();
        if (e.isControlDown() && fIndex > 0 && fIndex < plf.pointCount() - 1) {
            Point prev = plf.pointAt(fIndex - 1);
            Point next = plf.pointAt(fIndex + 1);
            Point corner1 = new Point(prev.x, next.y);
            Point corner2 = new Point(next.x, prev.y);
            if (Geom.length(x, y, corner1.x, corner1.y) < Geom.length(x, y,
                                                                              corner2.x,
                                                                              corner2.y)) {
                p = corner1;
            } else {
                p = corner2;
            }
        } else {
            p = new Point(x, y);
        }
        myOwner().setPointAt(p, fIndex);
    }

    public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
        super.invokeStart(e, x, y, view);
        if (e.getClickCount() > 1) {
            myOwner().removePointAt(fIndex);
            view.selectionInvalidateHandles();
        }
    }

    protected int getIndex() {
        return fIndex;
    }

    protected PolyLineable myOwner() {
        return (PolyLineable) owner();
    }
}