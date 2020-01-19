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
import java.awt.event.MouseEvent;


/**
 * A Handle to manipulate the start/end angle of an arc or pie ellipse
 * segment.
 * <p>
 * </p>
 * Created: 13 Jul 2008
 * @author Michael Duvigneau
 */
class PieAngleHandle extends AbstractHandle {
    private double angle;
    private PieFigure pieFig;
    private int angleKind;

    public PieAngleHandle(PieFigure owner, int angleKind) {
        super(owner);
        pieFig = owner;
        this.angleKind = angleKind;
    }

    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        angle = pieFig.getAngle(angleKind);
    }

    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        Rectangle r = pieFig.displayBox();
        Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
        Point from = new Point(x, y);
        double angle_rad;
        if (from.equals(center)) {
            angle_rad = Math.PI;
        } else {
            angle_rad = Geom.pointToAngle(r, from);
        }
        angle = PieFigure.normalizeAngle(-Math.toDegrees(angle_rad));
        if (e.isControlDown()) {
            // With Ctrl-Key, jump to divisions by 15 degrees.
            angle = (Math.round(angle / 15) * 15) % 360;
        }
        pieFig.setAngle(angleKind, angle);
    }

    public Point locate() {
        Rectangle r = pieFig.displayBox();
        angle = pieFig.getAngle(angleKind);
        double angle_rad = -Math.toRadians(angle);
        return Geom.ovalAngleToPoint(r, angle_rad);
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