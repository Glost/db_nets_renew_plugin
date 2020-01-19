/*
 * @(#)ChopRoundRectangleConnector.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ChopBoxConnector;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * A ChopRoundRectangleConnector locates a connection point by
 * chopping the connection at the roundrectangle defined by the
 * figure's display box.
 */
public class ChopRoundRectangleConnector extends ChopBoxConnector {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ChopRoundRectangleConnector.class);

    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -3165091511154766610L;

    public ChopRoundRectangleConnector() {
    }

    public ChopRoundRectangleConnector(Figure owner) {
        super(owner);
    }

    protected Point[] intersectEllipseLine(double rx, double ry, double a,
                                           double b) {
        /* Solve quadtratic equation according to abc formula */
        double aa = a * a * rx * rx + ry * ry;
        double bb = 2 * b * a * rx * rx;
        double cc = (b * b - ry * ry) * rx * rx;

        /* x1 and x2 are real roots of aa * x * x + bb * x + cc */
        double x1 = (-bb + Math.sqrt(bb * bb - 4 * aa * cc)) / (2 * aa);
        double x2 = (-bb - Math.sqrt(bb * bb - 4 * aa * cc)) / (2 * aa);

        double y1 = b + a * x1;
        double y2 = b + a * x2;

        // logger.debug("a:" + a + " b:" + b + " aa:" + aa + " bb:" + bb + " cc:" + cc);
        return new Point[] { new Point((int) x1, (int) y1), new Point((int) x2,
                                                                      (int) y2) };
    }

    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();
        Point from = Geom.center(source);
        Point to = Geom.center(r);

        Point targetArc = ((RoundRectangleFigure) target).getArc();

        double angle = Geom.pointToAngle(r, from)
                       + (r.intersection(source).equals(r) ? Math.PI : 0);
        Point toMeet = Geom.angleToPoint(r, angle);

        int leftx = r.x + targetArc.x / 2;
        int rightx = r.x + r.width - targetArc.x / 2;
        int topy = r.y + targetArc.y / 2;
        int downy = r.y + r.height - targetArc.y / 2;
        boolean left = toMeet.x < leftx;
        boolean right = toMeet.x > rightx;
        boolean top = toMeet.y < topy;
        boolean down = toMeet.y > downy;

        // if the connection intersects the rounded area...
        if ((left || right) && (top || down)) {
            int xc; // center of ellipse
            int yc; // center of ellipse
            if (left) {
                xc = leftx;
            } else {
                xc = rightx;
            }
            if (top) {
                yc = topy;
            } else {
                yc = downy;
            }

            double a = (double) (from.y - to.y) / (double) (from.x - to.x);
            double b = (from.y - yc) - (from.x - xc) * a;

            Point[] intersections = intersectEllipseLine(targetArc.x / 2.0,
                                                         targetArc.y / 2.0, a, b);

            Point intersection;

            if (left ^ (intersections[0].x < intersections[1].x)) {
                intersection = intersections[1];
            } else {
                intersection = intersections[0];
            }

            return new Point(intersection.x + xc, intersection.y + yc);

        } else {
            return toMeet;
        }
    }
}