/*
 * @(#)VirtualPlaceFigure.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeAdapter;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import java.io.IOException;

import java.util.Vector;


public class VirtualPlaceFigure extends PlaceFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -7877776241236946511L;
    @SuppressWarnings("unused")
    private int virtualPlaceFigureSerializedDataVersion = 1;

    /**
     * The place figure which is referenced by this virtual
     * place figure.
     * @serial
     **/
    private PlaceFigure place = null;

    public VirtualPlaceFigure() { // No-Arg-Constructor for loading only!
        super();
    }

    public VirtualPlaceFigure(PlaceFigure place) {
        super();
        setPlace(place);
    }

    private void setPlace(PlaceFigure place) {
        this.place = place;
        adapt();
        final FigureChangeEvent removeEvent = new FigureChangeEvent(this, null);
        place.addFigureChangeListener(new FigureChangeAdapter() {
                public void figureChanged(FigureChangeEvent e) {
                    adapt();
                }

                public void figureRemoved(FigureChangeEvent e) {
                    if (listener() != null) {
                        listener().figureRequestRemove(removeEvent);
                    }
                }
            });
    }

    // This method returns the place whose marking is ultimately
    // supposed to be displayed.
    public PlaceFigure getSemanticPlaceFigure() {
        return place.getSemanticPlaceFigure();
    }

    private void adapt() {
        super.setAttribute("FillColor", place.getFillColor());
        super.setAttribute("FrameColor", place.getFrameColor());
    }

    public void drawFrame(Graphics g) {
        super.drawFrame(g);
        Rectangle r = displayBox();
        Shape s = new Ellipse2D.Double(r.x + 2, r.y + 2, r.width - 4,
                                       r.height - 4);
        ((Graphics2D) g).draw(s);
    }

    public ShadowNetElement buildShadow(ShadowNet net) {
        return null;
    }

    public ShadowNetElement getShadow() {
        return place.getShadow();
    }

    public void setAttribute(String attr, Object value) {
        if ("FillColor".equals(attr) || "FrameColor".equals(attr)) {
            place.setAttribute(attr, value);
        } else {
            super.setAttribute(attr, value);
        }
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (alternate) {
            return super.inspect(view, true);
        } else {
            view.clearSelection();
            view.addToSelection(place);
            return true;
        }
    }

    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(place);
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    /**
      * Stores the Figure to a StorableOutput.
      */
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeStorable(place);
    }

    /**
     * Reads the Figure from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        setPlace((PlaceFigure) dr.readStorable());
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring the connection from this virtual
     * place to the original place.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setPlace(place);
    }
}