/*
 * @(#)EllipseFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import java.io.IOException;

import java.util.Vector;


/**
 * An elliptical pie or arc figure.  The figure represents an arc segment
 * of an ellipse.  If the figure is filled with some color, it represents a
 * pie segment of an ellipse.
 * <p>
 * </p>
 * Created: 13 Jul 2008
 *
 * @author Michael Duvigneau
 **/
public class PieFigure extends AttributeFigure {
    /*
     * Serialization support.
     */


    //private static final long serialVersionUID = ;
    //private int pieFigureSerializedDataVersion = 1;
    static final int START_ANGLE = 1;
    static final int END_ANGLE = 2;

    /**
     * Determines position and size of the elliptical arc or pie
     * specifying position and size of its bounding box.
     * @serial
     **/
    private Rectangle fDisplayBox;

    /**
     * Determines angle (relative to displayBox) where the elliptical pie
     * or arc starts.  If <code>endAngle &lt; startAngle</code> then the arc
     * loops around 0 degrees.
     * The value of this field is always in the interval [0,360[.
     * @serial
     **/
    private double startAngle;

    /**
     * Determines angle (relative to displayBox) where the elliptical pie
     * or arc ends.  If <code>endAngle &lt; startAngle</code> then the arc
     * loops around 0 degrees.
     * The value of this field is always in the interval [0,360[.
     * @serial
     **/
    private double endAngle;

    public PieFigure() {
        this(new Point(0, 0), new Point(0, 0));
    }

    public PieFigure(Point origin, Point corner) {
        this(origin, corner, 180, 90);
    }

    public PieFigure(Point origin, Point corner, double startAngle,
                     double endAngle) {
        basicDisplayBox(origin, corner);
        setStartAngle(startAngle);
        setEndAngle(endAngle);
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = normalizeAngle(startAngle);
        changed();
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = normalizeAngle(endAngle);
        changed();
    }

    public void setAngle(int angleKind, double angle) {
        switch (angleKind) {
        case START_ANGLE:
            setStartAngle(angle);
            break;
        case END_ANGLE:
            setEndAngle(angle);
            break;
        default:
            throw new IllegalArgumentException("Undefined angle kind: "
                                               + angleKind);
        }
    }

    public double getStartAngle() {
        return startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public double getAngle(int angleKind) {
        switch (angleKind) {
        case START_ANGLE:
            return startAngle;
        case END_ANGLE:
            return endAngle;
        default:
            throw new IllegalArgumentException("Undefined angle kind: "
                                               + angleKind);
        }
    }

    static double normalizeAngle(double angle) {
        // Duplicate execution of modulo is intended: inner modulo
        // operation may result negative values that are compensated by
        // adding 360 and computing modulo again
        return (360 + (angle % 360)) % 360;
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        handles.add(new PieAngleHandle(this, START_ANGLE));
        handles.add(new PieAngleHandle(this, END_ANGLE));
        BoxHandleKit.addHandles(this, handles);
        return handles;
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    /**
     * Checks if a point is inside the figure.
     */
    public boolean containsPoint(int x, int y) {
        if (super.containsPoint(x, y)) {
            return Geom.ellipseContainsPoint(displayBox(), x, y);
        } else {
            return false;
        }
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    public void drawBackground(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        int arcType = Arc2D.PIE;
        double angleExtent = endAngle - startAngle;
        if (angleExtent < 0) {
            angleExtent += 360;
        }
        Shape s = new Arc2D.Double(r.x, r.y, r.width, r.height, startAngle,
                                   angleExtent, arcType);
        g2.fill(s);

    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        int arcType = Arc2D.PIE;
        if (ColorMap.isTransparent(getFillColor())) {
            arcType = Arc2D.OPEN;
        }
        double angleExtent = endAngle - startAngle;
        if (angleExtent < 0) {
            angleExtent += 360;
        }
        Shape s = new Arc2D.Double(r.x, r.y, r.width, r.height, startAngle,
                                   angleExtent, arcType);
        g2.draw(s);
    }

    public Insets connectionInsets() {
        Rectangle r = fDisplayBox;
        int cx = r.width / 2;
        int cy = r.height / 2;
        return new Insets(cy, cx, cy, cx);
    }

    public Connector connectorAt(int x, int y) {
        return new ChopPieConnector(this);
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fDisplayBox.x);
        dw.writeInt(fDisplayBox.y);
        dw.writeInt(fDisplayBox.width);
        dw.writeInt(fDisplayBox.height);
        dw.writeDouble(startAngle);
        dw.writeDouble(endAngle);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
        startAngle = dr.readDouble();
        endAngle = dr.readDouble();
    }
}