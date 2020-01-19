/*
 * @(#)SplitFigure.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Vector;


public class SplitFigure extends DiagramFigure {

    /**
     * This figure will be highlighted in a instance
     * drawing in the same manner as the transition
     * will be highlighted. May be <code>null</code>.
     * @serial
     **/
    private Figure hilightFig = null;

    public SplitFigure() {
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

    //    public boolean getTraceMode() {
    //        Object value = getAttribute("TraceMode");
    //        if (value instanceof Boolean) {
    //            return ((Boolean) value).booleanValue();
    //        }
    //        return true;
    //    }
    public void release() {
        super.release();

    }

    public static Dimension defaultDimension() {
        return new Dimension(55, 8);
    }

    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }

    public Figure getHighlightFigure() {
        return hilightFig;
    }

    /**
     * Returns all figures with dependencies of the superclass
     * plus an optional hilight figure.
     **/
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(getHighlightFigure());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }


    //    //-- store / load ----------------------------------------------
    //    public void write(StorableOutput dw) {
    //        super.write(dw);
    //        dw.writeInt(fDisplayBox.x);
    //        dw.writeInt(fDisplayBox.y);
    //        dw.writeInt(fDisplayBox.width);
    //        dw.writeInt(fDisplayBox.height);
    //    }
    //
    //    public void read(StorableInput dr) throws IOException {
    //        super.read(dr);
    //        fDisplayBox = new Rectangle(dr.readInt(), dr.readInt(), dr.readInt(), 
    //                                    dr.readInt());
    //    }
}