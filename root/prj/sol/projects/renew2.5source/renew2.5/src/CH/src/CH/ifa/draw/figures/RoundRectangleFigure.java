/*
 * @(#)RoundRectangleFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import java.io.IOException;

import java.util.Vector;


/**
 * A round rectangle figure.
 * @see RadiusHandle
 */
public class RoundRectangleFigure extends AttributeFigure {
    private static final int DEFAULT_ARC = 8;
    private static final double DEFAULT_RATIO = 0.1;

    /**
     * The name of the attribute that contains a boolean
     * value determining if the corner arc's radiuses
     * should be scaled when the rectangle's size changes.
     **/
    public static final String ARC_SCALE_ATTR = "ArcScale";
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 7907900248924036885L;

    /**
     * Determines position and size of the rectangle by
     * specifying position and size of its bounding box.
     * @serial
     **/
    private Rectangle fDisplayBox = null;

    /**
     * The absolute horizontal radius of the corner arcs.
     * @serial
     **/
    private int fArcWidth;

    /**
     * The absolute vertical radius of the corner arcs.
     * @serial
     **/
    private int fArcHeight;

    /**
     * The relative horizontal radius of the corner arcs,
     * given as fraction of the rectangle width.
     * The value must conform to {@link #fArcWidth}.
     * <p>
     * This field is transient because it can be derived
     * from <code>fArcWidth</code> on deserialization.
     * </p>
     **/
    private transient double fXRatio;

    /**
     * The relative vertical radius of the corner arcs,
     * given as fraction of the rectangle width.
     * The value must conform to {@link #fArcHeight}.
     * <p>
     * This field is transient because it can be derived
     * from <code>fArcHeight</code> on deserialization.
     * </p>
     **/
    private transient double fYRatio;
    @SuppressWarnings("unused")
    private int roundRectangleSerializedDataVersion = 1;

    public RoundRectangleFigure() {
        this(new Point(0, 0), new Point(0, 0));
        fArcWidth = fArcHeight = DEFAULT_ARC;
        fXRatio = fYRatio = DEFAULT_RATIO;
    }

    public RoundRectangleFigure(Point origin, Point corner) {
        fArcWidth = fArcHeight = DEFAULT_ARC;
        fXRatio = (double) fArcWidth / (double) (corner.x - origin.x);
        fYRatio = (double) fArcHeight / (double) (corner.y - origin.y);
        basicDisplayBox(origin, corner);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        willChange();
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
        if (scaleArc()) {
            fArcWidth = (int) ((corner.x - origin.x) * fXRatio);
            fArcHeight = (int) ((corner.y - origin.y) * fYRatio);
        } else {
            recalculateRatio();
        }
        handlesChanged();
        changed();
    }

    /**
     * Recalculates {@link #fXRatio} and {@link #fYRatio} so that
     * they conform to {@link #fArcWidth} and {@link #fArcHeight}.
     * Should be called after each modification to fArcWidth or
     * fArcHeight.
     **/
    private void recalculateRatio() {
        fXRatio = (double) getArcWidth() / (double) fDisplayBox.width;
        fYRatio = (double) getArcHeight() / (double) fDisplayBox.height;
    }

    /**
     * Tells whether the corner arc's radiuses should be scaled
     * when the rectangle's size changes. Evaluates the attribute
     * <code>ARC_SCALE_ATTR</code>, defaults to <code>false</code>.
     **/
    private boolean scaleArc() {
        Boolean attr = (Boolean) getAttribute(ARC_SCALE_ATTR);
        if (attr != null) {
            return attr.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Sets the arc's witdh and height.
     */
    public void setArc(int width, int height) {
        willChange();
        fArcWidth = width;
        fArcHeight = height;
        recalculateRatio();
        changed();
    }

    public int getArcWidth() {
        // Make sure that the stored arc dimensions, which
        // might be too big in preparation for a later
        // enlargement, are not passed naively to the environment.
        if (fArcWidth > fDisplayBox.width) {
            return fDisplayBox.width;
        } else {
            return fArcWidth;
        }
    }

    public int getArcHeight() {
        if (fArcHeight > fDisplayBox.height) {
            return fDisplayBox.height;
        } else {
            return fArcHeight;
        }
    }

    /**
     * Gets the arc's width and height.
     */
    public Point getArc() {
        return new Point(getArcWidth(), getArcHeight());
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        BoxHandleKit.addHandles(this, handles);

        handles.addElement(new RadiusHandle(this));

        return handles;
    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    public void drawBackground(Graphics g) {
        Rectangle r = displayBox();
        Shape s = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height,
                                             getArcWidth(), getArcHeight());
        ((Graphics2D) g).fill(s);
    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Shape s = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height,
                                             getArcWidth(), getArcHeight());
        ((Graphics2D) g).draw(s);
    }

    public Insets connectionInsets() {
        return new Insets(getArcWidth() / 2, getArcHeight() / 2,
                          getArcWidth() / 2, getArcHeight() / 2);
    }

    public Connector connectorAt(int x, int y) {
        return new ChopRoundRectangleConnector(this); // just for demo purposes
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeInt(fDisplayBox.x);
        dw.writeInt(fDisplayBox.y);
        dw.writeInt(fDisplayBox.width);
        dw.writeInt(fDisplayBox.height);
        dw.writeInt(fArcWidth);
        dw.writeInt(fArcHeight);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(),
                                    dr.readInt());
        fArcWidth = dr.readInt();
        fArcHeight = dr.readInt();
        recalculateRatio();
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except recalculating the transient fields
     * <code>fXRatio</code> and <code>fYRatio</code>.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        recalculateRatio();
    }
}