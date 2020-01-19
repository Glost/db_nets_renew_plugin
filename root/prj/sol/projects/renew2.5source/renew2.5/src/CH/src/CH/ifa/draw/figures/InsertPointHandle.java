/*
 * @(#)PolyLineHandle.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.util.Geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * A handle for a node on the polyline.
 */
public class InsertPointHandle extends PolyLineHandle {

    /**
     * Constructs a poly line handle.
     * @param owner   the owning polyline figure.
     * @param segment the index of the first node of the
     *                segment associated with this handle.
     */
    public InsertPointHandle(PolyLineable owner, int segment) {
        super(owner, new PolyLineSegmentLocator(segment), segment + 1);
    }

    public void invokeStart(int x, int y, DrawingView view) {
        myOwner().insertPointAt(locate(), getIndex());
        view.selectionInvalidateHandles();
        super.invokeStart(x, y, view);
    }

    public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
        // switch off "delete" function on double-click
        invokeStart(x, y, view);
    }

    /**
     * Draws this handle.
     */
    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.white);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(Color.black);
        g.drawOval(r.x, r.y, r.width, r.height);

        // draw "+" into circle:
        Point mid = Geom.center(r);
        g.drawLine(r.x, mid.y, r.x + r.width, mid.y);
        g.drawLine(mid.x, r.y, mid.x, r.y + r.height);
    }
}