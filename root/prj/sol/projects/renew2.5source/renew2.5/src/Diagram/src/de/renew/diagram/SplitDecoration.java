/*
 * SplitDecoration.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import java.io.IOException;


/**
 * A SplitDecoration marks the SplitFigure as a XOR,OR or AND SplitFigure.
 *
 */
public class SplitDecoration implements FigureDecoration {
    private static final int DEFAULT_SIZE = 20;
    protected Color _fillColor;
    protected Color _frameColor;
    protected int _size;
    protected int _halfSize;

    public SplitDecoration() {
        setSize(DEFAULT_SIZE);
    }

    /**
     * Draws the Decoration. The two points describe a rectangle, which is
     * the bounding box of the diamond.
     */
    public void draw(Graphics g, int x, int y, Color fillColor, Color lineColor) {
        // TBD: reuse the Polygon object
        Shape shape = outline(x - _halfSize, y - _halfSize, _size, _size);


        // OR Decoration is allways white inside.
        fillColor = _fillColor;
        lineColor = _frameColor;

        Graphics2D g2 = (Graphics2D) g;
        if (!ColorMap.isTransparent(fillColor)) {
            g2.setColor(fillColor);
            g2.fill(shape);
        }


        g2.setColor(lineColor);
        g2.draw(shape);

    }

    /** Return the polygon describing the diamond  **/
    public Shape outline(int x1, int y1, int x2, int y2) {
        return outline(new Rectangle(x1, y1, x2, y2));
    }

    /** Return the polygon describing the diamond **/
    public Shape outline(Rectangle r) {
        GeneralPath p = new GeneralPath();

        p.moveTo(r.x, r.y + r.height / 2);
        p.lineTo(r.x + r.width / 2, r.y);
        p.lineTo(r.x + r.width, r.y + r.height / 2);
        p.lineTo(r.x + r.width / 2, r.y + r.height);
        p.closePath();
        return p;
    }

    public void setSize(int size) {
        _size = size;
        _halfSize = size / 2;
    }

    public int getSize() {
        return _size;
    }

    public void setFillColor(Color c) {
        _fillColor = c;
    }

    public void setFrameColor(Color c) {
        _frameColor = c;
    }

    public static int getDefaultSize() {
        return DEFAULT_SIZE;
    }

    /**
     * Stores the arrow tip to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        dw.writeInt(_size);
        dw.writeInt(_halfSize);


        //         dw.writeDouble(fAngle);
        //         dw.writeDouble(fOuterRadius);
        //         dw.writeDouble(fInnerRadius);
        //         dw.writeBoolean(fFilled);  
    }

    /**
     * Reads the arrow tip from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        _size = dr.readInt();
        _halfSize = dr.readInt();


        //         if (dr.getVersion() >= 5) {
        //             fAngle = dr.readDouble();
        //             fOuterRadius = dr.readDouble();
        //             fInnerRadius = dr.readDouble();
        //             fFilled = dr.readBoolean();
        //        }
    }
}