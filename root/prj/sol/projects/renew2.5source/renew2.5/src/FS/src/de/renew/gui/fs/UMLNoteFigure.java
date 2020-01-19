package de.renew.gui.fs;

import CH.ifa.draw.contrib.ChopPolygonConnector;
import CH.ifa.draw.contrib.OutlineFigure;

import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

import java.util.Vector;


public class UMLNoteFigure extends TextFigure implements OutlineFigure {
    private static final LineConnection anchor = new LineConnection(null, null,
                                                                    PolyLineFigure.LINE_STYLE_DOTTED);

    public UMLNoteFigure() {
        super();
        setFrameColor(Color.black);
        setFillColor(Color.white);
    }

    /**
     * Added constructor to choose a different style setting the
     * line shape default on bspline and letting the caller choose the
     * color of frame, text and connecting arc. (all the same)
     * -sami- 2003-10-06
     *
     * @param _frameColor
     */
    public UMLNoteFigure(Color _frameColor) {
        super();
        setFrameColor(_frameColor);
        setFillColor(Color.white);
        setAttribute("TextColor", _frameColor);
        anchor.setFrameColor(_frameColor);
        anchor.setAttribute("LineShape",
                            new Integer(PolyLineFigure.BSPLINE_SHAPE));
        anchor.setAttribute("BSplineSegments",
                            new Integer(CH.ifa.draw.util.BSpline.DEFSEGMENTS));
        anchor.setAttribute("BSplineDegree",
                            new Integer(CH.ifa.draw.util.BSpline.DEFDEGREE));
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new ConnectionHandle(this, RelativeLocator.center(),
                                                anchor));
        return handles;
    }

    public void drawBackground(Graphics g) {
        Polygon fPoly = outline(false, g);
        GeneralPath shape = new GeneralPath();
        int i = 0;
        int[] x = fPoly.xpoints;
        int[] y = fPoly.ypoints;
        int max = fPoly.npoints;
        shape.moveTo(x[i], y[i]);
        while (i < max) {
            shape.lineTo(x[i], y[i]);
            i++;
        }
        shape.closePath();
        ((Graphics2D) g).fill(shape);

    }

    public void drawFrame(Graphics g) {
        Polygon fPoly = outline(true, g);
        GeneralPath shape = new GeneralPath();
        int i = 0;
        int[] x = fPoly.xpoints;
        int[] y = fPoly.ypoints;
        int max = fPoly.npoints;
        shape.moveTo(x[i], y[i]);
        while (i < max) {
            shape.lineTo(x[i], y[i]);
            i++;
        }
        shape.closePath();
        ((Graphics2D) g).draw(shape);
    }

    public Rectangle displayBox() {
        Rectangle box = super.displayBox();
        return new Rectangle(box.x - 5, box.y - 5, box.width + 10,
                             box.height + 10);
    }

    protected Dimension getLineDimension(int i, Graphics g) {
        Dimension dim = super.getLineDimension(i, g);
        if (i == 0) {
            dim.width += dogsEarSize(g);
        }
        return dim;
    }

    /**
     *
     * @param g
     *  The current Graphics object. Could be null.
     * @return
     *  The size of the dog-ear (Eselsohr) at the top right of the UMLNoteFigure
     *  specified by the Font of the first line and the Graphics if not null.
     */
    private int dogsEarSize(Graphics g) {
        //NOTICEsignature
        return getMetrics(getLineFont(0), g).getHeight() + 5;
    }

    public Polygon outline() {
        return outline(false, null);
    }

    /**
     *
     * @param g Could be null.
     */
    public Polygon outline(boolean line, Graphics g) {
        // should be cached...
        Rectangle box = displayBox();
        Polygon outline = new Polygon();
        int dogsEarSize = dogsEarSize(g);
        outline.addPoint(box.x, box.y);
        outline.addPoint(box.x + box.width - dogsEarSize, box.y);
        if (line) {
            outline.addPoint(box.x + box.width - dogsEarSize,
                             box.y + dogsEarSize);
            outline.addPoint(box.x + box.width - dogsEarSize, box.y);
        }
        outline.addPoint(box.x + box.width, box.y + dogsEarSize);
        if (line) {
            outline.addPoint(box.x + box.width - dogsEarSize,
                             box.y + dogsEarSize);
            outline.addPoint(box.x + box.width, box.y + dogsEarSize);
        }
        outline.addPoint(box.x + box.width, box.y + box.height);
        outline.addPoint(box.x, box.y + box.height);
        return outline;
    }

    /**
     * Checks if a point is inside the figure's displayBox.
     */
    public boolean containsPoint(int x, int y) {
        if (isVisible()) {
            return outline().contains(x, y);
        }
        return false;
    }

    public Connector connectorAt(int x, int y) {
        return new ChopPolygonConnector(this);
    }
}