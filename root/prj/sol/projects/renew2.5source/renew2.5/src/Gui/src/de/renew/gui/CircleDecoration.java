/*
 * @(#)CircleDecoration.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.LineDecoration;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;

import java.io.IOException;


/**
 * A circle line decoration.
 * @see CH.ifa.draw.figures.PolyLineFigure
 */
public class CircleDecoration implements LineDecoration {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -3459870923049638L;
    private double fRadius;
    private boolean fFilled;
    @SuppressWarnings("unused")
    private int circleDecorationSerializedDataVersion = 1;

    public CircleDecoration() {
        this(4, true);
    }

    public CircleDecoration(int radius) {
        this(radius, true);
    }

    public CircleDecoration(boolean filled) {
        this(4, filled);
    }

    public CircleDecoration(int radius, boolean filled) {
        fRadius = radius;
        fFilled = filled;
    }

    /**
     * Draws the circle in the direction specified by the given two
     * points.
     */
    public void draw(Graphics g, int x1, int y1, int x2, int y2,
                     Color fillColor, Color lineColor) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double rr = dx * dx + dy * dy;
        if (rr != 0) {
            // distinct points
            double r = Math.sqrt(rr);
            x1 += (int) (dx * fRadius / r);
            y1 += (int) (dy * fRadius / r);
        }

        // if filled, fill with lineColor.
        if (fFilled) {
            fillColor = lineColor;
        }

        if (!ColorMap.isTransparent(fillColor)) {
            g.setColor(fillColor);
            g.fillOval((int) (x1 - fRadius), (int) (y1 - fRadius),
                       (int) (2 * fRadius), (int) (2 * fRadius));
        }

        // if not filled, outline with lineColor.
        if (!fFilled && !ColorMap.isTransparent(lineColor)) {
            g.setColor(lineColor);
            g.drawOval((int) (x1 - fRadius), (int) (y1 - fRadius),
                       (int) (2 * fRadius), (int) (2 * fRadius));
        }
    }

    /**
     * Stores the arrow tip to a StorableOutput.
     */
    public void write(StorableOutput dw) {
    }

    /**
     * Reads the arrow tip from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
    }
}