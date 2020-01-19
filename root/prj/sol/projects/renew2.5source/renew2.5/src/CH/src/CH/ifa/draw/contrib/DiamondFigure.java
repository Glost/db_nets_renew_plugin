/*
 * Hacked together by Doug lea
 * Tue Feb 25 17:39:44 1997  Doug Lea  (dl at gee)
 *
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.figures.RectangleFigure;

import CH.ifa.draw.framework.Connector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;


/**
 * A diamond with vertices at the midpoints of its enclosing rectangle
 */
public class DiamondFigure extends RectangleFigure implements OutlineFigure {

    /**
     * Constructs a default diamond figure from point (0,0) to (0,0).
     */
    public DiamondFigure() {
        super(new Point(0, 0), new Point(0, 0));
    }

    /**
     * Constructs a diamond figure between the given origin point and the given corner point.
     *
     * @param origin the origin point of the diamond figure.
     * @param corner the corner point of the diamond figure.
     */
    public DiamondFigure(Point origin, Point corner) {
        super(origin, corner);
    }

    /** Return the polygon describing the diamond
     *
     * @return the polygon describing the diamond.
     */
    public Shape polygon() {
        return polygon(0);
    }

    /** Return the polygon describing the diamond
     *
     * @param offset an offset for the polygon.
     * @return the polygon describing the diamond.
     */
    public Shape polygon(int offset) { //NOTICEsignature
        Rectangle r = displayBox();
        GeneralPath thePath = new GeneralPath();

        thePath.moveTo(r.x + r.width / 2, r.y + r.height);
        thePath.lineTo(r.x + r.width, r.y + r.height / 2);
        thePath.lineTo(r.x + r.width / 2, r.y);
        thePath.lineTo(r.x, r.y + r.height / 2);
        thePath.closePath();
        return thePath;
    }

    public Polygon outline() {
        Rectangle r = displayBox();
        r.setRect(r.x, r.y, r.width, r.height);
        Polygon p = new Polygon();
        p.addPoint(r.x, r.y + r.height / 2);
        p.addPoint(r.x + r.width / 2, r.y);
        p.addPoint(r.x + r.width, r.y + r.height / 2);
        p.addPoint(r.x + r.width / 2, r.y + r.height);
        return p;
    }

    public Connector connectorAt(int x, int y) {
        return new ChopPolygonConnector(this);
    }

    public void drawBackground(Graphics g) {
        g.setColor(getFillColor());
        ((Graphics2D) g).fill(polygon());
    }

    public void drawFrame(Graphics g) {
        g.setColor(getFrameColor());
        ((Graphics2D) g).draw(polygon());
    }

    public Insets connectionInsets() {
        Rectangle r = displayBox();
        return new Insets(r.height / 2, r.width / 2, r.height / 2, r.width / 2);
    }

    public boolean containsPoint(int x, int y) {
        return polygon().contains(x, y);
    }
}