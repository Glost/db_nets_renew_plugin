/*
 * @(#)ArrowTip.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import java.io.IOException;


/**
 * An arrow tip line decoration.
 * @see PolyLineFigure
 */
public class ArrowTip implements LineDecoration {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -3459171428373823638L;
    protected double fAngle; // pointiness of arrow
    protected double fOuterRadius;
    protected double fInnerRadius;
    protected boolean fFilled;
    @SuppressWarnings("unused")
    private int arrowTipSerializedDataVersion = 1;

    /**
     * Constructs an default arrow tip with the angle 0.40, outer and inner radius 8 and filled.
     */
    public ArrowTip() {
        fAngle = 0.40; //0.35;
        fOuterRadius = 8; //15;
        fInnerRadius = 8; //12;
        fFilled = true;
    }

    /**
     * Constructs an arrow tip with the given angle and radius.
     *
     * @param angle the angle of the arrow tip.
     * @param outerRadius the outer radius of the arrow tip.
     * @param innerRadius the inner radius of the arrow tip.
     * @param filled if the arrow tip is filled or not.
     */
    public ArrowTip(double angle, double outerRadius, double innerRadius,
                    boolean filled) {
        fAngle = angle;
        fOuterRadius = outerRadius;
        fInnerRadius = innerRadius;
        fFilled = filled;
    }

    /**
     * Draws the arrow tip in the direction specified by the given two
     * points.
     */
    public void draw(Graphics g, int x1, int y1, int x2, int y2,
                     Color fillColor, Color lineColor) {
        // TBD: reuse the Polygon object
        Shape p = outline(x1, y1, x2, y2);

        // if filled, fill with lineColor.
        if (fFilled) {
            fillColor = lineColor;
        }

        if (!ColorMap.isTransparent(fillColor)) {
            g.setColor(fillColor);
            ((Graphics2D) g).fill(p);
        }


        g.setColor(lineColor);
        ((Graphics2D) g).draw(p);

    }

    /**
     * Calculates the outline of an arrow tip in the direction specified by the two points (x1,y1) and (x2,y2).
     *
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @return the outline the arrow tip.
     */
    public Shape outline(int x1, int y1, int x2, int y2) {
        double dir = Math.PI / 2 - Math.atan2(x2 - x1, y1 - y2);
        return outline(x1, y1, dir);
    }

    protected Shape outline(int x, int y, double direction) {
        GeneralPath shape = new GeneralPath();

        shape.moveTo(x, y);
        addPointRelative(shape, x, y, fOuterRadius, direction - fAngle);
        addPointRelative(shape, x, y, fInnerRadius, direction);
        addPointRelative(shape, x, y, fOuterRadius, direction + fAngle);
        shape.closePath();

        return shape;
    }

    public void addPointRelative(GeneralPath shape, int x, int y,
                                 double radius, double angle) {
        shape.lineTo(x + (int) (radius * Math.cos(angle)),
                     y - (int) (radius * Math.sin(angle)));
    }

    /**
     * Stores the arrow tip to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        dw.writeDouble(fAngle);
        dw.writeDouble(fOuterRadius);
        dw.writeDouble(fInnerRadius);
        dw.writeBoolean(fFilled);
    }

    /**
     * Reads the arrow tip from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        if (dr.getVersion() >= 5) {
            fAngle = dr.readDouble();
            fOuterRadius = dr.readDouble();
            fInnerRadius = dr.readDouble();
            fFilled = dr.readBoolean();
        }
    }
}