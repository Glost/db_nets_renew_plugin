/*
 * Fri Feb 28 07:47:05 1997  Doug Lea  (dl at gee)
 * Based on PolyLineFigure
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.InsertPointHandle;
import CH.ifa.draw.figures.PolyLineable;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.AbstractLocator;

import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;


/**
 * A scalable, rotatable polygon with an arbitrary number of points
 */
public class PolygonFigure extends AttributeFigure implements OutlineFigure,
                                                              PolyLineable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PolygonFigure.class);

    /**
     * Distance threshold for smoothing away or locating points
     **/
    static final int TOO_CLOSE = 2;
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 6254089689239215026L;
    private static final String SMOOTHINGSTRATEGY = "ch.ifa.draw.polygon.smoothing";
    private static final String SMOOTHING_INLINE = "alignment";
    private static final String SMOOTHING_DISTANCES = "distances";
    @SuppressWarnings("unused")
    private int polygonFigureSerializedDataVersion = 1;
    public final static int LINE_SHAPE = 0;
    public final static int BSPLINE_SHAPE = 1;

    /**
     * The polygon to be displayed by this figure.
     * @serial
     **/
    protected Polygon fPoly = new Polygon();

    public PolygonFigure() {
        super();
    }

    public PolygonFigure(int x, int y) {
        fPoly.addPoint(x, y);
    }

    public PolygonFigure(Polygon p) {
        fPoly = new Polygon(p.xpoints, p.ypoints, p.npoints);
    }

    public Rectangle displayBox() {
        return bounds(fPoly);
    }

    public boolean isEmpty() {
        return (fPoly.npoints < 3
               || (size().width < TOO_CLOSE) && (size().height < TOO_CLOSE));
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>(fPoly.npoints);
        for (int i = 0; i < fPoly.npoints; i++) {
            handles.addElement(new PolygonHandle(this, locator(i), i));
        }
        for (int i = 0; i < fPoly.npoints - 1; i++) {
            handles.addElement(new InsertPointHandle(this, i));
        }
        handles.addElement(new PolygonScaleHandle(this));
        //handles.addElement(new PolygonPointAddHandle(this));
        return handles;
    }

    public void basicDisplayBox(Point origin, Point corner) {
        Rectangle r = displayBox();
        int dx = origin.x - r.x;
        int dy = origin.y - r.y;
        fPoly.translate(dx, dy);
        r = displayBox();
        Point oldCorner = new Point(r.x + r.width, r.y + r.height);
        Polygon p = getPolygon();
        scaleRotate(oldCorner, p, corner, true, true);
    }

    /**
     * return a copy of the raw polygon
     * @return a copy of the raw polygon.
     **/
    public Polygon getPolygon() {
        return new Polygon(fPoly.xpoints, fPoly.ypoints, fPoly.npoints);
    }

    public Polygon outline() {
        return getPolygon();
    }

    public Point center() {
        return center(fPoly);
    }

    /**
     * @return all points of this polygon figure.
     */
    public Enumeration<Point> points() {
        Vector<Point> pts = new Vector<Point>(fPoly.npoints);
        for (int i = 0; i < fPoly.npoints; ++i) {
            pts.addElement(new Point(fPoly.xpoints[i], fPoly.ypoints[i]));
        }
        return pts.elements();
    }

    public int pointCount() {
        return fPoly.npoints;
    }

    public void basicMoveBy(int dx, int dy) {
        fPoly.translate(dx, dy);
    }

    public void drawBackground(Graphics g) {
        GeneralPath shape = new GeneralPath();
        int i = 0;
        int[] x = fPoly.xpoints;
        int[] y = fPoly.ypoints;
        int max = fPoly.npoints;
        shape.moveTo(x[i], y[i]);
        while (i < max) {
            shape.lineTo(x[i], y[i]);
            i++;
        }
        shape.closePath();
        ((Graphics2D) g).fill(shape);
    }

    public void drawFrame(Graphics g) {
        GeneralPath shape = new GeneralPath();
        int i = 0;
        int[] x = fPoly.xpoints;
        int[] y = fPoly.ypoints;
        int max = fPoly.npoints;
        shape.moveTo(x[i], y[i]);
        while (i < max) {
            shape.lineTo(x[i], y[i]);
            i++;
        }
        shape.closePath();
        ((Graphics2D) g).draw(shape);
    }

    public boolean containsPoint(int x, int y) {
        logger.debug("Contains Point x=" + x + ", y= " + y + " ? ");
        return fPoly.contains(x, y);
    }

    public Connector connectorAt(int x, int y) {
        return new ChopPolygonConnector(this);
    }

    /**
     * Adds a node to the list of points.
     * @param x the x coordinate of the point to add.
     * @param y the y coordinate of the point to add.
     */
    public void addPoint(int x, int y) {
        fPoly.addPoint(x, y);
        changed();
    }

    /**
     * Changes the position of a node.
     */
    public void setPointAt(Point p, int i) {
        willChange();
        fPoly.xpoints[i] = p.x;
        fPoly.ypoints[i] = p.y;
        //Define a new Polygon as moving points is not allowed see Java Bug 4269933
        fPoly = new Polygon(fPoly.xpoints, fPoly.ypoints, fPoly.npoints);
        changed();
    }

    /**
     * Insert a node at the given point.
     */
    public void insertPointAt(Point p, int i) {
        willChange();
        int n = fPoly.npoints + 1;
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int j = 0; j < i; ++j) {
            xs[j] = fPoly.xpoints[j];
            ys[j] = fPoly.ypoints[j];
        }
        xs[i] = p.x;
        ys[i] = p.y;
        for (int j = i; j < fPoly.npoints; ++j) {
            xs[j + 1] = fPoly.xpoints[j];
            ys[j + 1] = fPoly.ypoints[j];
        }

        fPoly = new Polygon(xs, ys, n);
        changed();
    }

    public void removePointAt(int i) {
        willChange();
        int n = fPoly.npoints - 1;
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int j = 0; j < i; ++j) {
            xs[j] = fPoly.xpoints[j];
            ys[j] = fPoly.ypoints[j];
        }
        for (int j = i; j < n; ++j) {
            xs[j] = fPoly.xpoints[j + 1];
            ys[j] = fPoly.ypoints[j + 1];
        }
        fPoly = new Polygon(xs, ys, n);
        changed();
    }

    /**
     * Scale and rotate relative to anchor
     *
     * @param anchor the anchor for the transformation.
     * @param originalPolygon the original polygon.
     *
     * @param p the difference between p and the anchor relative to the
     *         center of the polygon define the scale factor and the angle
     *         of the transformations.
     *
     * @param scale if the polygon should be scaled.
     * @param rotate if the polygon should be rotated.
     **/
    public void scaleRotate(Point anchor, Polygon originalPolygon, Point p,
                            boolean scale, boolean rotate) {
        willChange();

        // use center to determine relative angles and lengths
        Point ctr = center(originalPolygon);
        double anchorLen = Geom.length(ctr.x, ctr.y, anchor.x, anchor.y);

        if (anchorLen > 0.0) {
            double newLen = Geom.length(ctr.x, ctr.y, p.x, p.y);
            double ratio = newLen / anchorLen;

            double anchorAngle = Math.atan2(anchor.y - ctr.y, anchor.x - ctr.x);
            double newAngle = Math.atan2(p.y - ctr.y, p.x - ctr.x);
            double rotation = newAngle - anchorAngle;

            if (!scale) {
                ratio = 1;
            }
            if (!rotate) {
                rotation = 0;
            }
            int n = originalPolygon.npoints;
            int[] xs = new int[n];
            int[] ys = new int[n];

            for (int i = 0; i < n; ++i) {
                int x = originalPolygon.xpoints[i];
                int y = originalPolygon.ypoints[i];
                double l = Geom.length(ctr.x, ctr.y, x, y) * ratio;
                double a = Math.atan2(y - ctr.y, x - ctr.x) + rotation;
                xs[i] = (int) (ctr.x + l * Math.cos(a) + 0.5);
                ys[i] = (int) (ctr.y + l * Math.sin(a) + 0.5);
            }
            fPoly = new Polygon(xs, ys, n);
        }
        changed();
    }

    /**
     * Remove points that are nearly colinear with others
     **/
    public void smoothPoints() {
        willChange();
        boolean removed = false;
        int n = fPoly.npoints;
        do {
            removed = false;
            int i = 0;
            while (i < n && n >= 3) {
                int nxt = (i + 1) % n;
                int prv = (i - 1 + n) % n;
                String strategy = DrawPlugin.getCurrent().getProperties()
                                            .getProperty(SMOOTHINGSTRATEGY);
                boolean doremove = false;
                if (strategy == null || strategy.equals("")
                            || SMOOTHING_INLINE.equals(strategy)) {
                    if ((distanceFromLine(fPoly.xpoints[prv],
                                                  fPoly.ypoints[prv],
                                                  fPoly.xpoints[nxt],
                                                  fPoly.ypoints[nxt],
                                                  fPoly.xpoints[i],
                                                  fPoly.ypoints[i]) < TOO_CLOSE)) {
                        doremove = true;
                    }
                } else if (SMOOTHING_DISTANCES.equals(strategy)) {
                    if (Math.abs(fPoly.xpoints[prv] - fPoly.xpoints[i]) < 5
                                && Math.abs(fPoly.ypoints[prv]
                                                    - fPoly.ypoints[i]) < 5) {
                        doremove = true;
                    }
                }
                if (doremove) {
                    removed = true;
                    --n;
                    for (int j = i; j < n; ++j) {
                        fPoly.xpoints[j] = fPoly.xpoints[j + 1];
                        fPoly.ypoints[j] = fPoly.ypoints[j + 1];
                    }
                } else {
                    ++i;
                }
            }
        } while (removed);
        if (n != fPoly.npoints) {
            fPoly = new Polygon(fPoly.xpoints, fPoly.ypoints, n);
        }
        changed();
    }

    /**
     * Splits the segment at the given point if a segment was hit.
     *
     * @param x the x coordinate of the given point.
     * @param y the y coordinate of the given point.
     * @return the index of the segment or -1 if no segment was hit.
     */
    public int splitSegment(int x, int y) {
        int i = findSegment(x, y);
        if (i != -1) {
            insertPointAt(new Point(x, y), i + 1);
            return i + 1;
        } else {
            return -1;
        }
    }

    public Point pointAt(int i) {
        return new Point(fPoly.xpoints[i], fPoly.ypoints[i]);
    }

    /**
     * Return the point on the polygon that is farthest from the center
     *
     * @return the point on the polygon that is farthest from the center.
     **/
    public Point outermostPoint() {
        Point ctr = center();
        int outer = 0;
        long dist = 0;

        for (int i = 0; i < fPoly.npoints; ++i) {
            long d = Geom.length2(ctr.x, ctr.y, fPoly.xpoints[i],
                                  fPoly.ypoints[i]);
            if (d > dist) {
                dist = d;
                outer = i;
            }
        }

        return new Point(fPoly.xpoints[outer], fPoly.ypoints[outer]);
    }

    /**
     * Gets the segment that is hit by the given point.
     * @param x the x coordinate of the given point.
     * @param y the y coordinate of the given point.
     * @return the index of the segment or -1 if no segment was hit.
     */
    public int findSegment(int x, int y) {
        double dist = TOO_CLOSE;
        int best = -1;

        for (int i = 0; i < fPoly.npoints; i++) {
            int n = (i + 1) % fPoly.npoints;
            double d = distanceFromLine(fPoly.xpoints[i], fPoly.ypoints[i],
                                        fPoly.xpoints[n], fPoly.ypoints[n], x, y);
            if (d < dist) {
                dist = d;
                best = i;
            }
        }
        return best;
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fPoly.npoints);
        for (int i = 0; i < fPoly.npoints; ++i) {
            dw.writeInt(fPoly.xpoints[i]);
            dw.writeInt(fPoly.ypoints[i]);
        }
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        int size = dr.readInt();
        int[] xs = new int[size];
        int[] ys = new int[size];
        for (int i = 0; i < size; i++) {
            xs[i] = dr.readInt();
            ys[i] = dr.readInt();
        }
        fPoly = new Polygon(xs, ys, size);
    }

    /**
     * Creates a locator for the point with the given index.
     *
     * @param pointIndex the index of the point.
     *
     * @return the locator for the point with the given index or (-1,-1)
     *          if the index is invalid.
     */
    public static Locator locator(final int pointIndex) {
        return new AbstractLocator() {
                public Point locate(Figure owner) {
                    PolygonFigure plf = (PolygonFigure) owner;

                    // guard against changing PolygonFigures -> temporary hack
                    if (pointIndex < plf.pointCount()) {
                        return ((PolygonFigure) owner).pointAt(pointIndex);
                    }
                    return new Point(-1, -1);
                }
            };
    }

    /**
     * Compute the distance of point c from line segment form point a to point b.
     * if perpendicular projection is outside segment it, returns Double.MAX_VALUE.
     * If a and b are the same, returns the distance between that point and c.
     *
     * @param xa the x coordinate of the first point on the line.
     * @param ya the y coordinate of the first point on the line.
     * @param xb the x coordinate of the second point on the line.
     * @param yb the y coordinate of the second point on the line.
     * @param xc the x coordinate of the point c,
     *         whose distance should be computed.
     * @param yc the y coordinate of the point c,
     *         whose distance should be computed.
     *
     * @return the distance of point c from line segment form point a to point b.
     *          if perpendicular projection is outside segment it, returns Double.MAX_VALUE.
     *          If a and b are the same, returns the distance between that point and c.
     **/
    public static double distanceFromLine(int xa, int ya, int xb, int yb,
                                          int xc, int yc) {
        // source:http://vision.dai.ed.ac.uk/andrewfg/c-g-a-faq.html#q7
        //Let the point be C (XC,YC) and the line be AB (XA,YA) to (XB,YB).
        //The length of the
        //      line segment AB is L:
        //
        //                    ___________________
        //                   |        2         2
        //              L = \| (XB-XA) + (YB-YA)
        //and
        //
        //                  (YA-YC)(YA-YB)-(XA-XC)(XB-XA)
        //              r = -----------------------------
        //                              L**2
        //
        //                  (YA-YC)(XB-XA)-(XA-XC)(YB-YA)
        //              s = -----------------------------
        //                              L**2
        //
        //      Let I be the point of perpendicular projection of C onto AB, the
        //
        //              XI=XA+r(XB-XA)
        //              YI=YA+r(YB-YA)
        //
        //      Distance from A to I = r*L
        //      Distance from C to I = s*L
        //
        //      If r < 0 I is on backward extension of AB
        //      If r>1 I is on ahead extension of AB
        //      If 0<=r<=1 I is on AB
        //
        //      If s < 0 C is left of AB (you can just check the numerator)
        //      If s>0 C is right of AB
        //      If s=0 C is on AB
        int xdiff = xb - xa;
        int ydiff = yb - ya;
        long l2 = xdiff * xdiff + ydiff * ydiff;

        if (l2 == 0) {
            return Geom.length(xa, ya, xc, yc);
        }

        double rnum = (ya - yc) * (ya - yb) - (xa - xc) * (xb - xa);
        double r = rnum / l2;

        if (r < 0.0 || r > 1.0) {
            return Double.MAX_VALUE;
        }

        double xi = xa + r * xdiff;
        double yi = ya + r * ydiff;
        double xd = xc - xi;
        double yd = yc - yi;
        return Math.sqrt(xd * xd + yd * yd);

        /*
          for directional version, instead use
          double snum =  (ya-yc) * (xb-xa) - (xa-xc) * (yb-ya);
          double s = snum / l2;

          double l = Math.sqrt((double)l2);
          return = s * l;
          */
    }

    /**
     * Replacement for built in Polygon.getBounds that doesn't always update?
     *
     * @param p the polygon, which bounds are returned.
     * @return the bounds of the given polygon.
     */
    public static Rectangle bounds(Polygon p) {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        int n = p.npoints;
        for (int i = 0; i < n; i++) {
            int x = p.xpoints[i];
            int y = p.ypoints[i];
            if (x > maxx) {
                maxx = x;
            }
            if (x < minx) {
                minx = x;
            }
            if (y > maxy) {
                maxy = y;
            }
            if (y < miny) {
                miny = y;
            }
        }

        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    /**
     * Computes the center of the given polygon.
     *
     * @param p the given polygon.
     * @return the center of the given polygon.
     */
    public static Point center(Polygon p) {
        long sx = 0;
        long sy = 0;
        int n = p.npoints;
        for (int i = 0; i < n; i++) {
            sx += p.xpoints[i];
            sy += p.ypoints[i];
        }

        return new Point((int) (sx / n), (int) (sy / n));
    }

    public static Point chop(Polygon poly, Point p) {
        Point ctr = center(poly);
        int cx = -1;
        int cy = -1;
        long len = Long.MAX_VALUE;

        // Try for points along edge
        for (int i = 0; i < poly.npoints; ++i) {
            int nxt = (i + 1) % poly.npoints;
            Point chop = Geom.intersect(poly.xpoints[i], poly.ypoints[i],
                                        poly.xpoints[nxt], poly.ypoints[nxt],
                                        p.x, p.y, ctr.x, ctr.y);
            if (chop != null) {
                long cl = Geom.length2(chop.x, chop.y, p.x, p.y);
                if (cl < len) {
                    len = cl;
                    cx = chop.x;
                    cy = chop.y;
                }
            }
        }

        // if none found, pick closest vertex
        if (len == Long.MAX_VALUE) {
            for (int i = 0; i < poly.npoints; ++i) {
                long l = Geom.length2(poly.xpoints[i], poly.ypoints[i], p.x, p.y);
                if (l < len) {
                    len = l;
                    cx = poly.xpoints[i];
                    cy = poly.ypoints[i];
                }
            }
        }
        return new Point(cx, cy);
    }
}