/*
 * @(#)EllipseFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import java.io.IOException;

import java.util.Vector;


/**
 * An ellipse figure.
 */
public class EllipseFigure extends AttributeFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -6856203289355118951L;

    /**
     * Determines position and size of the ellipse by
     * specifying position and size of its bounding box.
     * @serial
     **/
    private Rectangle fDisplayBox;
    @SuppressWarnings("unused")
    private int ellipseFigureSerializedDataVersion = 1;

    public EllipseFigure() {
        this(new Point(0, 0), new Point(0, 0));
    }

    public EllipseFigure(Point origin, Point corner) {
        basicDisplayBox(origin, corner);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        BoxHandleKit.addHandles(this, handles);
//        handles.addElement(new PolygonScaleHandle(this));
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
        Shape s = new Ellipse2D.Double(r.x, r.y, r.width, r.height);
//        g2.rotate(45,r.getCenterX(),r.getCenterY());
        g2.fill(s);
//        g2.rotate(-45,r.getCenterX(),r.getCenterY());
    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Ellipse2D.Double(r.x, r.y, r.width, r.height);
//        g2.rotate(45,r.getCenterX(),r.getCenterY());
        g2.draw(s);
    }

    public Insets connectionInsets() {
        Rectangle r = fDisplayBox;
        int cx = r.width / 2;
        int cy = r.height / 2;
        return new Insets(cy, cx, cy, cx);
    }

    public Connector connectorAt(int x, int y) {
        return new ChopEllipseConnector(this);
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fDisplayBox.x);
        dw.writeInt(fDisplayBox.y);
        dw.writeInt(fDisplayBox.width);
        dw.writeInt(fDisplayBox.height);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
    }
}