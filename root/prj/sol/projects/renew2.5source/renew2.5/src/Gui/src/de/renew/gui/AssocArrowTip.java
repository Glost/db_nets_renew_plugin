/*
 * @(#)AssocArrowTip.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.ArrowTip;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;


/**
 * An assoc arrow tip line decoration.
 * @see CH.ifa.draw.figures.PolyLineFigure
 * @see de.renew.gui.fs.IsaConnection
 */
public class AssocArrowTip extends ArrowTip {
    public AssocArrowTip() {
        //        super(0.60,12,10,true);
        super(0.4, 11, 11, true);
    }

    /**
     * Draws the arrow tip in the direction specified by the given two
     * points.
     */
    public void draw(Graphics g, int x1, int y1, int x2, int y2,
                     Color fillColor, Color lineColor) {
        // TBD: reuse the Polygon object
        double direction = Math.PI / 2 - Math.atan2(x2 - x1, y1 - y2);
        GeneralPath shape = new GeneralPath();

        //Code duplication from ArrowTip method addPointRelative to find starting point
        shape.moveTo(x1 + (int) (fOuterRadius * Math.cos(direction - fAngle)),
                     y1 - (int) (fOuterRadius * Math.sin(direction - fAngle)));
        //Tip of arrow
        shape.lineTo(x1, y1);
        //The other end of the arrow
        addPointRelative(shape, x1, y1, fOuterRadius, direction + fAngle);

        g.setColor(lineColor);
        ((Graphics2D) g).draw(shape);
    }
}