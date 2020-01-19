package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractConnector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * @see CH.ifa.draw.framework.Connector
 */
public class BottomConnector extends AbstractConnector {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -1461450322712345462L;

    public BottomConnector() { // only used for Storable implementation
    }

    public BottomConnector(Figure owner) {
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

    //NOTICEsignature 
    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();

        int horizontal = r.x + (r.width / 2);
        int vertical = r.y + r.height;

        return new Point(horizontal, vertical);
    }

    /**
     * The display box of a <code>BottomConnector</code> is a
     * zero-height rectangle covering the bottom of the owner figure.
     *
     * @return the bottom rectangle of the owner figure.
     **/
    public Rectangle displayBox() {
        Rectangle r = owner().displayBox();
        return new Rectangle(r.x, r.y + r.height, r.width, 0);
    }
}