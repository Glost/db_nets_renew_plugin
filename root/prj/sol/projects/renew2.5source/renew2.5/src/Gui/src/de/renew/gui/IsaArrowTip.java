/*
 * @(#)IsaArrowTip.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.ArrowTip;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;


/**
 * An isa arrow tip line decoration.
 * @see CH.ifa.draw.figures.PolyLineFigure
 * @see de.renew.gui.fs.IsaConnection
 */
public class IsaArrowTip extends ArrowTip {

    /**
     * Constructs an isa arrow tip with the angle 0.60, outer radius 15 and
     * not filled.
     */
    public IsaArrowTip() {
        this(0.60, 15, false);
    }

    /**
     * Constructs an isa arrow tip with the given angle and radius.
     *
     * @param angle the angle of the arrow tip.
     * @param outerRadius the outer radius of the arrow tip.
     * @param filled if the arrow tip is filled or not.
     */
    public IsaArrowTip(double angle, double outerRadius, boolean filled) {
        super(angle, outerRadius, 0, filled);
    }

    /**
     * Draws the arrow tip in the direction specified by the given two
     * points.
     */
    @Override
    public void draw(Graphics g, int x1, int y1, int x2, int y2,
                     Color fillColor, Color lineColor) {
        // TBD: reuse the Polygon object
        Shape p = outline(x1, y1, x2, y2);

        Graphics2D g2 = (Graphics2D) g;
        //g.setColor(Color.white);
        g.setColor(fillColor);
        g2.fill(p);


        //g.setColor(Color.black);
        g.setColor(lineColor);
        g2.draw(p);
    }

    @Override
    protected Shape outline(int x, int y, double direction) {
        GeneralPath shape = new GeneralPath();

        shape.moveTo(x, y);
        addPointRelative(shape, x, y, fOuterRadius, direction - fAngle);
        // skip the middle point to get the desired triangle instead of a 
        // quadrangle
//        addPointRelative(shape, x, y, fInnerRadius, direction);
        addPointRelative(shape, x, y, fOuterRadius, direction + fAngle);
        shape.closePath();

        return shape;
    }
}