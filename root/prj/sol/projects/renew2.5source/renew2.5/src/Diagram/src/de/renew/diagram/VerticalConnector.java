package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractConnector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A VerticalConnector locates connection points by
 * choping the connection between the centers of the
 * two figures at the display box.
 * @see CH.ifa.draw.framework.Connector
 */
public class VerticalConnector extends AbstractConnector {
    public VerticalConnector() { // only used for Storable implementation
    }

    public VerticalConnector(Figure owner) {
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
        return chop(connection.start().owner(), r1, connection.startPoint().y);

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
        return chop(connection.end().owner(), r1, connection.startPoint().y);
    }

    //NOTICEsignature
    protected Point chop(Figure target, Rectangle source, int xxx) {
        Rectangle r = target.displayBox();
        int h = (int) (r.getHeight());
        int off = (int) (source.getWidth() / 2);
        int horizontal = source.x + off;
        int vertical = r.y;


        if (source.x + off < r.x) {
            horizontal = r.x;
        } else if (source.x + off > r.x + r.getWidth()) {
            horizontal = (int) (r.x + r.getWidth());
        }
        if (source.y > r.y) {
            vertical = r.y + h;
        }
        return new Point(horizontal, vertical);


    }

    /**
    * The display box of a <code>VerticalConnector</code> is a
    * zero-height rectangle covering the top of the owner figure.
    *
    * @return the top rectangle of the owner figure.
    **/
    public Rectangle displayBox() {
        Rectangle r = owner().displayBox();
        return new Rectangle(r.x, r.y, 0, r.height);
    }
}