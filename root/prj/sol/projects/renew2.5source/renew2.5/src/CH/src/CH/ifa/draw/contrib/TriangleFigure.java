/*
 * Hacked together by Doug lea
 * Tue Feb 25 17:30:58 1997  Doug Lea  (dl at gee)
 *
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.figures.RectangleFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import java.io.IOException;

import java.util.Vector;


/**
 * A triangle with same dimensions as its enclosing rectangle,
 * and apex at any of 8 places
 */
public class TriangleFigure extends RectangleFigure implements OutlineFigure {
    static double[] rotations = { -Math.PI / 2, -Math.PI / 4, 0.0, Math.PI / 4, Math.PI / 2, Math.PI * 3 / 4, Math.PI, -Math.PI * 3 / 4 };

    /**
     * Determines the location of the apex in relation
     * to the bounding box of the triangle. There are
     * eight possible locations, numbered from 0 to 7:
     * <pre>
     *        7---0---1
     *        |  /*\  |
     *        6 /   \ 2
     *        |/     \|
     *        5===4===3
     * </pre>
     * The example triangle in the figure above has the
     * value <code>fRotation = 0</code>. The star *
     * indicates the apex.
     * @serial
     **/
    protected int fRotation = 0;

    public TriangleFigure() {
        super(new Point(0, 0), new Point(0, 0));
    }

    public TriangleFigure(Point origin, Point corner) {
        super(origin, corner);
    }

    public Vector<Handle> handles() {
        Vector<Handle> h = super.handles();
        h.addElement(new TriangleRotationHandle(this));
        return h;
    }

    public void rotate(double angle) {
        willChange();
        // logger.debug("a:"+angle);
        double dist = Double.MAX_VALUE;
        int best = 0;
        for (int i = 0; i < rotations.length; ++i) {
            double d = Math.abs(angle - rotations[i]);
            if (d < dist) {
                dist = d;
                best = i;
            }
        }
        fRotation = best;
        changed();
    }

    /** Return the polygon describing the triangle **/
    public Shape polygon() {
        Rectangle r = displayBox();
        GeneralPath p = new GeneralPath();
        switch (fRotation) {
        case 0:
            p.moveTo(r.x + r.width / 2, r.y);
            p.lineTo(r.x + r.width, r.y + r.height);
            p.lineTo(r.x, r.y + r.height);
            p.closePath();
            break;
        case 1:
            p.moveTo(r.x + r.width, r.y);
            p.lineTo(r.x + r.width, r.y + r.height);
            p.lineTo(r.x, r.y);
            p.closePath();
            break;
        case 2:
            p.moveTo(r.x + r.width, r.y + r.height / 2);
            p.lineTo(r.x, r.y + r.height);
            p.lineTo(r.x, r.y);
            p.closePath();
            break;
        case 3:
            p.moveTo(r.x + r.width, r.y + r.height);
            p.lineTo(r.x, r.y + r.height);
            p.lineTo(r.x + r.width, r.y);
            p.closePath();
            break;
        case 4:
            p.moveTo(r.x + r.width / 2, r.y + r.height);
            p.lineTo(r.x, r.y);
            p.lineTo(r.x + r.width, r.y);
            p.closePath();
            break;
        case 5:
            p.moveTo(r.x, r.y + r.height);
            p.lineTo(r.x, r.y);
            p.lineTo(r.x + r.width, r.y + r.height);
            p.closePath();
            break;
        case 6:
            p.moveTo(r.x, r.y + r.height / 2);
            p.lineTo(r.x + r.width, r.y);
            p.lineTo(r.x + r.width, r.y + r.height);
            p.closePath();
            break;
        case 7:
            p.moveTo(r.x, r.y);
            p.lineTo(r.x + r.width, r.y);
            p.lineTo(r.x, r.y + r.height);
            p.closePath();
            break;
        }

        return p;
    }

    public Polygon outline() {
        Rectangle r = displayBox();
        Polygon p = new Polygon();
        switch (fRotation) {
        case 0:
            p.addPoint(r.x + r.width / 2, r.y);
            p.addPoint(r.x + r.width, r.y + r.height);
            p.addPoint(r.x, r.y + r.height);
            break;
        case 1:
            p.addPoint(r.x + r.width, r.y);
            p.addPoint(r.x + r.width, r.y + r.height);
            p.addPoint(r.x, r.y);
            break;
        case 2:
            p.addPoint(r.x + r.width, r.y + r.height / 2);
            p.addPoint(r.x, r.y + r.height);
            p.addPoint(r.x, r.y);
            break;
        case 3:
            p.addPoint(r.x + r.width, r.y + r.height);
            p.addPoint(r.x, r.y + r.height);
            p.addPoint(r.x + r.width, r.y);
            break;
        case 4:
            p.addPoint(r.x + r.width / 2, r.y + r.height);
            p.addPoint(r.x, r.y);
            p.addPoint(r.x + r.width, r.y);
            break;
        case 5:
            p.addPoint(r.x, r.y + r.height);
            p.addPoint(r.x, r.y);
            p.addPoint(r.x + r.width, r.y + r.height);
            break;
        case 6:
            p.addPoint(r.x, r.y + r.height / 2);
            p.addPoint(r.x + r.width, r.y);
            p.addPoint(r.x + r.width, r.y + r.height);
            break;
        case 7:
            p.addPoint(r.x, r.y);
            p.addPoint(r.x + r.width, r.y);
            p.addPoint(r.x, r.y + r.height);
            break;
        }

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
        switch (fRotation) {
        case 0:
            return new Insets(r.height, r.width / 2, 0, r.width / 2);
        case 1:
            return new Insets(0, r.width, r.height, 0);
        case 2:
            return new Insets(r.height / 2, 0, r.height / 2, r.width);
        case 3:
            return new Insets(r.height, r.width, 0, 0);
        case 4:
            return new Insets(0, r.width / 2, r.height, r.width / 2);
        case 5:
            return new Insets(r.height, 0, 0, r.width);
        case 6:
            return new Insets(r.height / 2, r.width, r.height / 2, 0);
        case 7:
            return new Insets(0, 0, r.height, r.width);
        default:
            return null;
        }
    }

    public boolean containsPoint(int x, int y) {
        return polygon().contains(x, y);
    }

    public Point center() {
        return PolygonFigure.center(outline());
    }

    public Point chop(Point p) {
        return PolygonFigure.chop(outline(), p);
    }

    public Object clone() {
        TriangleFigure figure = (TriangleFigure) super.clone();
        figure.fRotation = fRotation;
        return figure;
    }

    //-- store / load ----------------------------------------------
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fRotation);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fRotation = dr.readInt();
    }
}