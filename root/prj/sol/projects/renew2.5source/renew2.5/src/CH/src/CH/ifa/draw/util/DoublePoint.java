package CH.ifa.draw.util;

import java.awt.Point;

import java.util.Vector;


/**
 * The coordinates have double precision
 */
public class DoublePoint {
    public double x;
    public double y;

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DoublePoint(Point p) {
        x = p.x;
        y = p.y;

    }

    public DoublePoint(DoublePoint p) {
        x = p.x;
        y = p.y;
    }

    public DoublePoint(int xi, int yi) {
        x = xi;
        y = yi;
    }

    /**
     * Constructs and initializes a point at the origin (0, 0) of the coordinate space.
     */
    public DoublePoint() {
        x = 0;
        y = 0;
    }

    /**
     * Converts DoublePoint to Point
     */
    public Point getPoint() {
        return new Point((int) x, (int) y);
    }

    /**
     * Converts Vector of Points to Vector of DoublePoints
     * @param points Vector of Points
     * @return Vector of DoublePoints
     */
    public static Vector<DoublePoint> convertPointVector(Vector<Point> points) {
        int size = points.size();
        if (size == 0) {
            return new Vector<DoublePoint>();
        }
        Vector<DoublePoint> vec = new Vector<DoublePoint>();
        for (int i = 0; i < size; i++) {
            vec.addElement(new DoublePoint(points.elementAt(i)));
        }
        return vec;
    }

    /**
     * Converts Vector of DoublePoints to Vector of Points
     * @param points Vector of DoublePoints
     * @return Vector of Points
     */
    public static Vector<Point> convertDoublePointVector(Vector<DoublePoint> points) {
        int size = points.size();
        DoublePoint dP;
        if (size == 0) {
            return new Vector<Point>();
        }
        Vector<Point> vec = new Vector<Point>();
        for (int i = 0; i < size; i++) {
            dP = points.elementAt(i);
            vec.addElement(dP.getPoint());
        }
        return vec;
    }
}