package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.AbstractConnector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A HorizontalConnector locates the connection either at
 * the left or right side of a figure. If possible the
 * connection will be horizontal.
 * @see CH.ifa.draw.framework.Connector
 */
public class HorizontalConnector extends AbstractConnector {
    public HorizontalConnector() { // only used for Storable implementation
    }

    public HorizontalConnector(Figure owner) {
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
        int w = (int) (r.getWidth());
        int horizontal = r.x;
        int vertical = source.y;


        //int vertical = xxx;
        //        logger.debug("----------------------------------------------");
        //        logger.debug("vertical "+vertical);
        //        logger.debug("source.x "+source.x);
        //        logger.debug("source.y "+source.y);
        //        logger.debug("source.width "+source.getWidth());
        //        logger.debug("source.height "+source.getHeight());
        //        logger.debug("r.x      "+r.x);
        //        logger.debug("r.y      "+r.y);
        //        logger.debug("r.width  "+r.getWidth());
        //        logger.debug("r.height "+r.getHeight());
        if (source.y < r.y) {
            vertical = r.y;
        } else if (source.y > r.y + r.getHeight()) {
            vertical = (int) (r.y + r.getHeight());
        }
        if (source.x > r.x) {
            horizontal = r.x + w;
        }
        return new Point(horizontal, vertical);


        //return Geom.angleToPoint(r, 
        //                       (r.intersection(source).equals(r) ? Math.PI : 0)
        //                     + (Geom.pointToAngle(r, Geom.center(source))));
    }
}