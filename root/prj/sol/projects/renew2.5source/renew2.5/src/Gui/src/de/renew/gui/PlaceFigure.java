/*
 * @(#)PlaceFigure.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.EllipseFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.remote.ObjectAccessor;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.awt.Dimension;
import java.awt.Graphics;

import java.io.IOException;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;


public class PlaceFigure extends EllipseFigure implements PlaceNodeFigure,
                                                          FigureWithHighlight,
                                                          InscribableFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -7878886241236946512L;
    public static final int HIGHLIGHT = 0;
    public static final int CARDINALITY = 1;
    public static final int TOKENS = 2;
    public static final int EXPANDED_TOKENS = 3;
    @SuppressWarnings("unused")
    private int placeFigureSerializedDataVersion = 1;

    /**
     * This figure will be highlighted in a instance
     * drawing in the same manner as the place figure
     * will be highlighted. May be <code>null</code>.
     * @serial
     **/
    private Figure hilightFig = null;

    /**
     * The shadow of this place figure.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    protected transient ShadowPlace shadow = null;

    public PlaceFigure() {
        super();
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new ArcConnectionHandle(this));
        return handles;
    }

    public void draw(Graphics g) {
        super.draw(g);
        if (isVisible() && getAttribute(Breakpoint.ATTRIBUTENAME) != null) {
            BreakpointDecoration.draw(g, displayBox());
        }
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    public int getMarkingAppearance() {
        Object value = getAttribute("MarkingAppearance");
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return CARDINALITY;
    }

    public void release() {
        setHighlightFigure(null);
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    public ShadowNetElement buildShadow(ShadowNet net) {
        if (shadow != null) {
            shadow.discard();
        }
        shadow = new ShadowPlace(net);
        shadow.context = this;
        shadow.setID(this.getID());
        shadow.setTrace(getTraceMode());
        // logger.debug("place shadow for "+this+" created!");
        return shadow;
    }

    public ShadowNetElement getShadow() {
        return shadow;
    }

    public static Dimension defaultDimension() {
        return new Dimension(20, 20);
    }

    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }

    public Figure getHighlightFigure() {
        return hilightFig;
    }

    // This method returns this palce figure. It is overridden
    // by virtual place figures, which want to represent another
    // figure ultimately.
    public PlaceFigure getSemanticPlaceFigure() {
        return this;
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


    /**
     * Creates an instance figure for this figure.
     * @param drawing The instance drawing to create the figure for.
     * @param netElements The net elements.
     * @return The new instance figure.
     */
    public PlaceInstanceFigure createInstanceFigure(CPNInstanceDrawing drawing,
                                                    Hashtable<Serializable, ObjectAccessor> netElements) {
        return new PlaceInstanceFigure(drawing, this, netElements);
    }

    /**
     * Stores the Figure to a StorableOutput.
     */
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeStorable(hilightFig);
    }

    /**
     * Reads the Figure from a StorableInput.
     */
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        if (dr.getVersion() >= 3) {
            setHighlightFigure((Figure) dr.readStorable());
        }
    }
}