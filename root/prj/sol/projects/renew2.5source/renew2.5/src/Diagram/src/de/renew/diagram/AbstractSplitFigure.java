/*
 * @(#)AbstractSplitFigure.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.IOException;

import java.util.Vector;


public class AbstractSplitFigure extends TailFigure implements ISplitFigure {
    private FigureDecoration _decoration;
    protected Class<?> _decorationClass = FigureDecoration.class;

    public AbstractSplitFigure() {
        super();

    }

    public Rectangle displayBox() {
        return new Rectangle(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                             fDisplayBox.height);
    }

    protected void basicMoveBy(int x, int y) {
        fDisplayBox.translate(x, y);
    }

    /**
     * Changes the display box of a figure. Clients usually
     * call this method. It changes the display box
     * and announces the corresponding change.
     * @param origin the new origin
     * @param corner the new corner
     * @see #displayBox
     */
    public void displayBox(Point origin, Point corner) {
        willChange();
        basicDisplayBox(new Point(origin.x, origin.y),
                        new Point(corner.x - origin.x,
                                  (int) (defaultDimension().getHeight())));
        changed();
    }

    public void basicDisplayBox(Point origin, Point corner) {
        fDisplayBox = new Rectangle(origin.x, origin.y, corner.x, corner.y);
    }

    public void internalDraw(Graphics g) {
        super.internalDraw(g);
        decorate(g);
    }

    public void setDecoration(FigureDecoration fd) {
        _decoration = fd;
    }

    public FigureDecoration getDecoration() {
        return _decoration;
    }

    public boolean hasDecoration() {
        return true;
    }

    private void decorate(Graphics g) {
        if (_decoration != null) {
            int x = fDisplayBox.x + fDisplayBox.width / 2;
            int y = fDisplayBox.y + fDisplayBox.height / 2;


            //Point p4 = (Point) fPoints.elementAt(fPoints.size() - 1);
            _decoration.draw(g, x, y, getFillColor(), getFrameColor());
        }
    }

    public Connector connectorAt(int x, int y) {
        Rectangle r = displayBox();
        int left = r.x;
        int right = r.x + r.width;
        int length = right - left;

        int forth = (length / 4);
        if (x < left + forth) {
            return new LeftBottomConnector(this);
        } else if (x > right - forth) {
            return new RightBottomConnector(this);
        }

        return new TopConnector(this);
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new MessageConnectionHandle(this));
        return handles;
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    public void release() {
        super.release();

    }

    public static Dimension defaultDimension() {
        return new Dimension(55, 8);
    }

    /**
     * Returns all figures with dependencies of the superclass
     * plus an optional hilight figure.
     **/
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);

        //myDep.addElement(getHighlightFigure());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    public void write(StorableOutput dw) {
        super.write(dw);


        dw.writeStorable(_decoration);
        dw.writeString(_decorationClass.getName());
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        _decoration = (FigureDecoration) dr.readStorable();
        dr.readString();
    }
}