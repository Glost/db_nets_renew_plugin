/*
 * Sat Mar  1 09:06:09 1997  Doug Lea  (dl at gee)
 * Based on RadiusHandle
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.standard.AbstractHandle;

import CH.ifa.draw.util.Geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * A Handle to scale and rotate a PolygonFigure
 */
class PolygonScaleHandle extends AbstractHandle {
    private Point fOrigin = null;
    private Point fCurrent = null;
    private Polygon fOrigPoly = null;

    public PolygonScaleHandle(PolygonFigure owner) {
        super(owner);
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        fOrigPoly = ((PolygonFigure) (owner())).getPolygon();
        fOrigin = getOrigin();
        fCurrent = new Point(fOrigin.x, fOrigin.y);
    }

    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        int dx = x - anchorX;
        int dy = y - anchorY;
        boolean scale = true;
        boolean rotate = true;
        fCurrent = new Point(fOrigin.x + dx, fOrigin.y + dy);
        if (e.isControlDown()) {
            rotate = false;
        } else if (e.isShiftDown()) {
            scale = false;
        }
        ((PolygonFigure) (owner())).scaleRotate(fOrigin, fOrigPoly, fCurrent,
                                                scale, rotate);
        view.selectionInvalidateHandles();
    }

    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        super.invokeEnd(x, y, anchorX, anchorY, view);
        fOrigPoly = null;
        fOrigin = null;
        fCurrent = null;
    }

    public Point locate() {
        if (fCurrent != null) {
            return fCurrent;
        } else {
            return getOrigin();
        }
    }

    Point getOrigin() { // find a nice place to put handle
        Point outer = ((PolygonFigure) (owner())).outermostPoint();
        Point ctr = ((PolygonFigure) (owner())).center();
        double len = Geom.length(outer.x, outer.y, ctr.x, ctr.y);
        if (len == 0) { // best we can do?
            return new Point(outer.x - HANDLESIZE / 2, outer.y + HANDLESIZE / 2);
        }

        double u = HANDLESIZE / len;
        if (u > 1.0) { // best we can do?
            return new Point((outer.x * 3 + ctr.x) / 4,
                             (outer.y * 3 + ctr.y) / 4);
        } else {
            return new Point((int) (outer.x * (1.0 - u) + ctr.x * u),
                             (int) (outer.y * (1.0 - u) + ctr.y * u));
        }
    }

    public void draw(Graphics g) {
        Rectangle r = displayBox();

        g.setColor(Color.yellow);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(Color.black);
        g.drawOval(r.x, r.y, r.width, r.height);


        //for debugging ...

        /*
        Point ctr = ((PolygonFigure) (owner())).center();
        g.setColor(Color.blue);
        g.fillOval(ctr.x, ctr.y, r.width, r.height);

        g.setColor(Color.black);
        g.drawOval(ctr.x, ctr.y, r.width, r.height);
        */
    }
}