/*
 * @(#)ChopEllipseConnector.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ChopBoxConnector;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * A ChopEllipseConnector locates a connection point by
 * chopping the connection at the ellipse defined by the
 * figure's display box.
 */
public class ChopEllipseConnector extends ChopBoxConnector {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -3165091511154766610L;

    public ChopEllipseConnector() {
    }

    public ChopEllipseConnector(Figure owner) {
        super(owner);
    }

    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();
        Point from = Geom.center(source);
        double angle = Geom.pointToAngle(r, from)
                       + (r.intersection(source).equals(r) ? Math.PI : 0);
        return Geom.ovalAngleToPoint(r, angle);
    }
}