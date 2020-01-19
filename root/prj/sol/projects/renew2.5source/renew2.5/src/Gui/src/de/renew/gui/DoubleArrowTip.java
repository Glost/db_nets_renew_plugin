/*
 * @(#)DoubleArrowTip.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.ArrowTip;

import java.awt.Shape;
import java.awt.geom.GeneralPath;


/**
 * A double arrow tip line decoration.
 * @see CH.ifa.draw.figures.PolyLineFigure
 */
public class DoubleArrowTip extends ArrowTip {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -34591123673823638L;
    @SuppressWarnings("unused")
    private int doubleArrowTipSerializedDataVersion = 1;

    public DoubleArrowTip() {
    }

    /**
     * Constructs an arrow tip with the given angle and radius.
     */
    public DoubleArrowTip(double angle, double outerRadius, double innerRadius,
                          boolean filled) {
        super(angle, outerRadius, innerRadius, filled);
    }

    protected Shape outline(int x, int y, double direction) {
        GeneralPath shape = new GeneralPath();

        shape.moveTo(x, y);
        addPointRelative(shape, x, y, fOuterRadius, direction - fAngle);
        int xx = x + (int) (fInnerRadius * Math.cos(direction));
        int yy = y - (int) (fInnerRadius * Math.sin(direction));
        shape.lineTo(xx, yy);
        addPointRelative(shape, xx, yy, fOuterRadius, direction - fAngle);
        addPointRelative(shape, xx, yy, fInnerRadius, direction);
        addPointRelative(shape, xx, yy, fOuterRadius, direction + fAngle);
        shape.lineTo(xx, yy);
        addPointRelative(shape, x, y, fOuterRadius, direction + fAngle);
        shape.closePath();

        // shape.addPoint(x,y); // Closing the polygon (TEG 97-04-23)
        // closing is done automatically !!! (FUW 98-04-20)
        return shape;
    }
}