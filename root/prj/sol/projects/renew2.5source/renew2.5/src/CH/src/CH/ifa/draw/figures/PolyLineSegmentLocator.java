/*
 * @(#)PolyLineMiddleLocator.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractLocator;

import CH.ifa.draw.util.Geom;

import java.awt.Point;


/**
 * A poly line figure consists of a list of points.
 * It has an optional line decoration at the start and end.
 *
 * @see LineDecoration
 */
class PolyLineSegmentLocator extends AbstractLocator {
    int fSegment;

    public PolyLineSegmentLocator(int segment) {
        fSegment = segment;
    }

    public Point locate(Figure owner) {
        PolyLineable plf = (PolyLineable) owner;


        // guard against changing PolyLineFigures -> temporary hack
        // might already be solved by DrawingView.selectionHandlesInvalidated()
        if (fSegment < plf.pointCount() - 1) {
            return Geom.middle(plf.pointAt(fSegment), plf.pointAt(fSegment + 1));
        }
        return new Point(0, 0);
    }
}