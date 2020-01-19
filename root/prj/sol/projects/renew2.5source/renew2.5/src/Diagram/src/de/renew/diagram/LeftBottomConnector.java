package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractConnector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A LeftBottomConnector locates connection points at
 * the left bottom corner.
 *
 * @see CH.ifa.draw.framework.Connector
 */
public class LeftBottomConnector extends AbstractConnector {
    public LeftBottomConnector() { // only used for Storable implementation
    }

    public LeftBottomConnector(Figure owner) {
        super(owner);
    }

    public Point findStart(ConnectionFigure connection) {
        Rectangle r1;
        if (connection.pointCount() == 2) {
            r1 = connection.end().displayBox();
        } else {
            r1 = new Rectangle(connection.pointAt(1), new Dimension(0, 0));
        }

        //NOTICEsignature
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

        //NOTICEsignature
        return chop(connection.end().owner(), r1);
    }

    //NOTICEsignature
    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();

        int horizontal = r.x;
        int vertical = r.y + r.height;

        return new Point(horizontal, vertical);
    }

    /**
     * The display box of a <code>LeftBottomConnector</code> is a
     * zero-height, zero-width rectangle positioned at the bottom
     * left corner of the owner figure.
     *
     * @return the bottom left rectangle of the owner figure.
     **/
    public Rectangle displayBox() {
        Rectangle r = owner().displayBox();
        return new Rectangle(r.x, r.y + r.height, 0, 0);
    }
}