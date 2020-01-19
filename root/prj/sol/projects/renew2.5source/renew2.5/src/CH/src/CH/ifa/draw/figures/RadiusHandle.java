/*
 * @(#)RadiusHandle.java 5.1
 *
 */
package CH.ifa.draw.figures;

import org.freehep.graphicsio.VectorGraphicsIO;

import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.standard.AbstractHandle;

import CH.ifa.draw.util.Geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A Handle to manipulate the radius of a round corner rectangle.
 */
class RadiusHandle extends AbstractHandle {
    private static final int OFFSET = 3;
    private Point fRadius;
    private RoundRectangleFigure fOwner;

    public RadiusHandle(RoundRectangleFigure owner) {
        super(owner);
        fOwner = owner;
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        fRadius = fOwner.getArc();
        fRadius.x = fRadius.x / 2;
        fRadius.y = fRadius.y / 2;
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY,
                           DrawingView view) {
        int dx = x - anchorX;
        int dy = y - anchorY;
        Rectangle r = fOwner.displayBox();
        int rx = Geom.range(0, r.width, 2 * (fRadius.x + dx));
        int ry = Geom.range(0, r.height, 2 * (fRadius.y + dy));
        fOwner.setArc(rx, ry);
    }

    public Point locate() {
        Point radius = fOwner.getArc();
        Rectangle r = fOwner.displayBox();
        return new Point(r.x + radius.x / 2 + OFFSET,
                         r.y + radius.y / 2 + OFFSET);
    }

    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.yellow);
        //we need to draw vector graphics differently for svg and java
        if (g instanceof VectorGraphicsIO) {
            g.fillOval(r.x + 1, r.y + 1, r.width - 1, r.height - 1);
        } else {
            g.fillOval(r.x, r.y, r.width, r.height);
        }
        g.setColor(Color.black);
        g.drawOval(r.x, r.y, r.width, r.height);
    }
}