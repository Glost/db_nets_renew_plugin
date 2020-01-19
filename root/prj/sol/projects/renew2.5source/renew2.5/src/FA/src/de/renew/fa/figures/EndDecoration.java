/*
 * @(#)StartDecoration.java
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import java.io.IOException;


/**
 *
 *
 */
public class EndDecoration implements FigureDecoration {
    private static final int DEFAULT_SIZE = 20;
    static final long serialVersionUID = 6389353696652739208L;

    public static int getDefaultSize() {
        return DEFAULT_SIZE;
    }

    protected int _halfSize;
    protected int _size;

    // protected int size;
    //protected int halfSize;
    public EndDecoration() {
    }

    /**
     * Draws the Decoration. The two points describe a rectangle, which is the
     * bounding box of the diamond.
     */
    @Override
    public void draw(Graphics g, Rectangle r, Color fillColor, Color lineColor) {
        if (FADrawMode.getInstance().getMode() == FADrawMode.STANDARD) {
            drawStandard(g, r, fillColor, lineColor);
        } else {
            //NOTICEsignature
            drawAlternative(g, r, fillColor, lineColor);
        }
    }

    /**
     * Draws the Decoration. The two points describe a rectangle, which is the
     * bounding box of the diamond.
     */
    public void drawStandard(Graphics g, Rectangle r, Color fillColor,
                             Color lineColor) {
        if (!ColorMap.isTransparent(lineColor)) {
            g.setColor(lineColor);
            g.fillArc(r.x, r.y, r.width, r.height, 60, -120);
            if (!ColorMap.isTransparent(fillColor)) {
                g.setColor(fillColor);
            } else {
                // FIXME: We should erase the unneeded parts instead of
                //        painting them white
                g.setColor(Color.WHITE);
            }
            g.fillRect(getXabs(r) + r.width / 4 + 1, getYabs(r) + 1,
                       new Double(r.width / 2).intValue(),
                       new Double(getYprime(r) * 2).intValue());
        }
    }

    //NOTICEsignature
    public void drawAlternative(Graphics g, Rectangle r, Color fillColor,
                                Color lineColor) {
        if (!ColorMap.isTransparent(lineColor)) {
////            g.setColor(lineColor);            
////            g.drawOval(r.x + 3,r.y + 3,r.width - 7,r.height - 7);
//            Rectangle r = displayBox();
            Shape s = new Ellipse2D.Double(r.x + 3, r.y + 3, r.width - 6,
                                           r.height - 6);
            ((Graphics2D) g).draw(s);


        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.FigureDecoration#equals(de.renew.fa.FigureDecoration)
     */
    @Override
    public boolean equals(FigureDecoration fd) {
        return fd instanceof EndDecoration;
    }

    private int getXabs(Rectangle r) {
        return r.x;
    }

    private int getYabs(Rectangle r) {
        //float a = r.width / 2;
        float b = r.height / 2;

        return new Double(r.y + b - getYprime(r)).intValue();
    }

    private double getYprime(Rectangle r) {
        //float a = r.width / 2;
        float b = r.height / 2;
        return Math.sqrt(((1 - (.25)) * b * b));
    }

    /**
     * Reads the decoration from a StorableInput.
     */
    @Override
    public void read(StorableInput dr) throws IOException {
        //        _size = dr.readInt();
        //        _halfSize = dr.readInt();
        //         if (dr.getVersion() >= 5) {
        //             fAngle = dr.readDouble();
        //             fOuterRadius = dr.readDouble();
        //             fInnerRadius = dr.readDouble();
        //             fFilled = dr.readBoolean();
        //        }
    }

    /**
     * Stores the decoration to a StorableOutput.
     */
    @Override
    public void write(StorableOutput dw) {
        //        dw.writeInt(_size);
        //        dw.writeInt(_halfSize);
        //         dw.writeDouble(fAngle);
        //         dw.writeDouble(fOuterRadius);
        //         dw.writeDouble(fInnerRadius);
        //         dw.writeBoolean(fFilled);
    }
}