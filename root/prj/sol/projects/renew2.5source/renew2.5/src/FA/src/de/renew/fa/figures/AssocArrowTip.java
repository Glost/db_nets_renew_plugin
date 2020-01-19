/*
 * @(#)AssocArrowTip.java
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.figures.ArrowTip;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;


/**
 * This File was duplicated from de.renew.gui.fs to dismantle the dependency to
 * fs.
 *
 * An assoc arrow tip line decoration.
 *
 * @see CH.ifa.draw.figures.PolyLineFigure
 * @see de.renew.gui.fs.IsaConnection
 */
public class AssocArrowTip extends ArrowTip {
    static final long serialVersionUID = -8219226631738943404L;

    public AssocArrowTip() {
        //        super(0.60,12,10,true);
        super(0.4, 11, 11, true);
    }

    /**
     * Draws the arrow tip in the direction specified by the given two points.
     */
    @Override
    public void draw(Graphics g, int x1, int y1, int x2, int y2,
                     Color fillColor, Color lineColor) {
        // TBD: reuse the Polygon object
        Polygon p = (Polygon) outline(x1, y1, x2, y2);

        //g.setColor(Color.black);
        g.setColor(lineColor);
        int[] xpoints = new int[3];
        int[] ypoints = new int[3];
        xpoints[0] = p.xpoints[1];
        ypoints[0] = p.ypoints[1];
        xpoints[1] = p.xpoints[0];
        ypoints[1] = p.ypoints[0];
        xpoints[2] = p.xpoints[3];
        ypoints[2] = p.ypoints[3];
        g.drawPolyline(xpoints, ypoints, p.npoints - 1);
    }
}