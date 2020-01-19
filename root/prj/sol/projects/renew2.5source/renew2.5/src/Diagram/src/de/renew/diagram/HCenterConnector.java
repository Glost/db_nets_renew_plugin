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
public class HCenterConnector extends AbstractConnector {
    public HCenterConnector() { // only used for Storable implementation
    }

    public HCenterConnector(Figure owner) {
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

        int horizontal = r.x;
        int vertical = (int) (r.y + r.getHeight() / 2);


        if (source.x > r.x) {
            horizontal = r.x + r.width;
        }
        return new Point(horizontal, vertical);


    }

    /**
     * The display box of a <code>HCenterConnector</code> is a
     * zero-height rectangle covering the top of the owner figure.
     *
     * @return the top rectangle of the owner figure.
     **/
    public Rectangle displayBox() {
        Rectangle r = owner().displayBox();
        return new Rectangle(r.x, (r.y + r.height / 2), r.width, 0);
    }
}