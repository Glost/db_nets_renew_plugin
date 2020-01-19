/*
 *
 *
 */
package de.renew.diagram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;


/**
 *
 *
 */
public class XORDecoration extends SplitDecoration {
    public XORDecoration() {
        setSize(20);
        setFillColor(Color.white);
        setFrameColor(Color.black);
    }

    /**
     * Draws the Decoration. The two points describe a rectangle, which is
     * the bounding box of the diamond.
     */
    public void draw(Graphics g, int x, int y, Color fillColor, Color lineColor) {
        super.draw(g, x, y, fillColor, lineColor);
        int size = getSize();
        int halfSize = size / 2;

        ((Graphics2D) g).draw(xline(x - halfSize, y - halfSize, size, size));
    }

    public Shape xline(int x1, int y1, int x2, int y2) {
        return xline(new Rectangle(x1, y1, x2, y2));
    }

    public Shape xline(Rectangle r) {
        GeneralPath p = new GeneralPath();
        int h4 = r.height / 4;
        int h34 = r.height * 3 / 4;
        int w4 = r.width / 4;
        int w34 = r.width * 3 / 4;
        int a = r.x + w4;
        int b = r.x + w34;
        int u = r.y + h4;
        int v = r.y + h34;


        p.moveTo(a, v);
        p.lineTo(b, u);
        p.lineTo(r.x + r.width / 2, r.y + r.height / 2);
        p.lineTo(a, u);
        p.lineTo(b, v);
        p.lineTo(r.x + r.width / 2, r.y + r.height / 2);
        p.closePath();
        return p;
    }
}