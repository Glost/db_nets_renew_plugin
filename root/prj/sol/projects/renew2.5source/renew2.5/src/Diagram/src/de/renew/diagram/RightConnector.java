package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractConnector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A ChopBoxConnector locates connection points by
 * choping the connection between the centers of the
 * two figures at the display box.
 * @see CH.ifa.draw.framework.Connector
 */
public class RightConnector extends AbstractConnector {
    public RightConnector() { // only used for Storable implementation
    }

    public RightConnector(Figure owner) {
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


        int horizontal = r.x + r.width;
        int vertical = r.y + r.height;
        if (source.y < r.y + r.height / 2) {
            vertical = r.y;
        }
        return new Point(horizontal, vertical);


    }

    /**
     * The display box of a <code>RightBottomConnector</code> is a
     * zero-height, zero-width rectangle positioned at the bottom
     * right corner of the owner figure.
     *
     * @return the bottom right rectangle of the owner figure.
     **/
    public Rectangle displayBox() {
        Rectangle r = owner().displayBox();
        return new Rectangle(r.x + r.width, r.y + r.height, 0, 0);
    }
}