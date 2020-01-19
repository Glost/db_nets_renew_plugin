/*
 * @(#)StartDecoration.java
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.Geom;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.IOException;


/**
 *
 *
 */
public class StartEndDecoration implements FigureDecoration {
    private static final int DEFAULT_SIZE = 20;
    static final long serialVersionUID = -4435677979638813780L;

    public static int getDefaultSize() {
        return DEFAULT_SIZE;
    }

    protected int _halfSize;
    protected int _size;

    public StartEndDecoration() {
    }

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
            g.fillArc(r.x, r.y, (r.width - 1), r.height - 1, 120, 120);
            g.fillArc(r.x, r.y, (r.width), r.height, 60, -120);
            if (!ColorMap.isTransparent(fillColor)) {
                g.setColor(fillColor);
            } else {
                // FIXME: We should erase the unneeded parts instead of
                //        painting them white
                g.setColor(Color.WHITE);
            }
            g.fillRect(getXabs(r) + r.width / 4, getYabs(r) + 2,
                       new Double(r.width / 2).intValue(),
                       new Double(getYprime(r) * 2).intValue() - 1);
        }
    }

    //NOTICEsignature
    public void drawAlternative(Graphics g, Rectangle r, Color fillColor,
                                Color lineColor) {
        if (!ColorMap.isTransparent(lineColor)) {
            g.setColor(lineColor);
            int size = 5;
            int[] xPoints = new int[size];
            int[] yPoints = new int[size];
            xPoints[0] = r.x;
            yPoints[0] = r.y;
            Point p = Geom.ovalAngleToPoint(r, 3.141 * 5 / 4);
            p.translate(-1, -1);
            xPoints[1] = p.x;
            yPoints[1] = p.y;

            xPoints[2] = p.x - 8;
            yPoints[2] = p.y + 2;

            xPoints[3] = p.x + 2;
            yPoints[3] = p.y - 8;
            xPoints[4] = p.x;
            yPoints[4] = p.y;

            g.drawPolyline(xPoints, yPoints, size);
            g.drawOval(r.x + 3, r.y + 3, r.width - 6, r.height - 6);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.fa.FigureDecoration#equals(de.renew.fa.FigureDecoration)
     */
    @Override
    public boolean equals(FigureDecoration fd) {
        return fd instanceof StartEndDecoration;
    }

    private int getXabs(Rectangle r) {
        //float a = r.width / 2;
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
     * Reads the arrow tip from a StorableInput.
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
     * Stores the arrow tip to a StorableOutput.
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