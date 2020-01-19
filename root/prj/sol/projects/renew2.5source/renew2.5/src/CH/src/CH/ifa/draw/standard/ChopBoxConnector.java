/*
 * @(#)ChopBoxConnector.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.Geom;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A ChopBoxConnector locates connection points by
 * choping the connection between the centers of the
 * two figures at the display box.
 * @see CH.ifa.draw.framework.Connector
 */
public class ChopBoxConnector extends AbstractConnector {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -1461450322712345462L;

    public ChopBoxConnector() { // only used for Storable implementation
    }

    public ChopBoxConnector(Figure owner) {
        super(owner);
    }

    public Point findStart(ConnectionFigure connection) {
        Rectangle r1;
        if (connection.pointCount() == 2) {
            r1 = connection.end().displayBox();
        } else {
            r1 = new Rectangle(connection.pointAt(1), new Dimension(0, 0));
        }

        return chop(connection.start().owner(), r1);
    }

    public Point findEnd(ConnectionFigure connection) {
        Rectangle r1;
        if (connection.pointCount() == 2) {
            r1 = connection.start().displayBox();
        } else {
            r1 = new Rectangle(connection.pointAt(connection.pointCount() - 2),
                               new Dimension(0, 0));
        }

        return chop(connection.end().owner(), r1);
    }

    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();
        return Geom.angleToPoint(r,
                                 (r.intersection(source).equals(r) ? Math.PI : 0)
                                 + (Geom.pointToAngle(r, Geom.center(source))));
    }
}