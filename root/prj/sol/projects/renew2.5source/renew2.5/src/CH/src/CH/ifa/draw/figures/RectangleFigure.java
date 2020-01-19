/*
 * @(#)RectangleFigure.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.BoxHandleKit;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import java.io.IOException;

import java.util.Vector;


/**
 * A rectangle figure.
 */
public class RectangleFigure extends AttributeFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 184722075881789163L;

    /**
     * Determines position and size of the rectangle by
     * specifying position and size of its bounding box.
     * @serial
     **/
    private Rectangle fDisplayBox;
    @SuppressWarnings("unused")
    private int rectangleFigureSerializedDataVersion = 1;

    public RectangleFigure() {
        this(new Point(0, 0), new Point(0, 0));
    }

    public RectangleFigure(Point origin, Point corner) {
        basicDisplayBox(origin, corner);
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin);
        fDisplayBox.add(corner);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = new Vector<Handle>();
        BoxHandleKit.addHandles(this, handles);
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
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
        g2.fill(s);
    }

    public void drawFrame(Graphics g) {
        Rectangle r = displayBox();
        Graphics2D g2 = (Graphics2D) g;
        Shape s = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
        g2.draw(s);
    }

    //-- store / load ----------------------------------------------
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