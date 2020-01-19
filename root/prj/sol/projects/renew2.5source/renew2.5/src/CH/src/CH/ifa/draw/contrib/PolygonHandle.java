/*
 * Fri Feb 28 07:47:13 1997  Doug Lea  (dl at gee)
 * Based on PolyLineHandle
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.AbstractHandle;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A handle for a node on the polygon.
 */
public class PolygonHandle extends AbstractHandle {
    private int fIndex;
    private Locator fLocator;

    /**
     * Constructs a polygon handle.
     * @param owner the owning polygon figure.
     * @param l     the locator
     * @param index the index of the node associated with this handle.
     */
    public PolygonHandle(PolygonFigure owner, Locator l, int index) {
        super(owner);
        fLocator = l;
        fIndex = index;
    }

    public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
        super.invokeStart(e, x, y, view);
        if (e.getClickCount() > 1) {
            myOwner().removePointAt(fIndex);
            view.selectionInvalidateHandles();
        }
    }

    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        //myOwner().setPointAt(new Point(x, y), fIndex);
        Point p;
        PolygonFigure plf = myOwner();
        if (e.isControlDown() && fIndex >= 0 && fIndex < plf.pointCount()) {
            int prevCount = fIndex - 1;
            int nextCount = fIndex + 1;
            if (fIndex == 0) {
                prevCount = plf.pointCount() - 1;
            } else if (fIndex == plf.pointCount() - 1) {
                nextCount = 0;
            }
            Point prev = plf.pointAt(prevCount);
            Point next = plf.pointAt(nextCount);
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

    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        myOwner().smoothPoints();
        super.invokeEnd(x, y, anchorX, anchorY, view);
    }

    public Point locate() {
        return fLocator.locate(owner());
    }

    private PolygonFigure myOwner() {
        return (PolygonFigure) owner();
    }
}