/*
 * @(#)PlaceFigure.java 5.1
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.EllipseFigure;
import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.fa.FAInstanceDrawing;

import de.renew.faformalism.shadow.ShadowFAState;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.FigureWithHighlight;
import de.renew.gui.InscribableFigure;
import de.renew.gui.NodeFigure;

import de.renew.remote.ObjectAccessor;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.awt.Dimension;
import java.awt.Graphics;

import java.io.IOException;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;


public class FAStateFigure extends EllipseFigure implements InscribableFigure,
                                                            FigureWithHighlight,
                                                            NodeFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAStateFigure.class);
    private static final FAArcConnection anchor = new FAArcConnection(null,
                                                                      null,
                                                                      PolyLineFigure.LINE_STYLE_NORMAL);

    // State types
    public static final int NULL = 0;
    public static final int START = 1;
    public static final int END = 2;
    public static final int STARTEND = 3;

    // Marking appearance types
    public static final int HIGHLIGHT = 0;
    public static final int CARDINALITY = 1;
    public static final int TOKENS = 2;
    public static final int EXPANDED_TOKENS = 3;
    /*
     * Serialization support.
     */
    static final long serialVersionUID = 6652986258188946097L;

    /*
     * The shadow element of this FAState.
     * It is null because it will be created,
     * when needed.
     */
    private transient ShadowFAState shadow = null;

    /**
     * The decoration giving the FAStateFigure graphical properties of a
     * start, end, startend or simple state.
     */
    private FigureDecoration _decoration = new NullDecoration();

    /**
     * This figure will be highlighted in a instance drawing in the same manner
     * as the place figure will be highlighted. May be <code>null</code>.
     *
     * @serial
     */
    private Figure hilightFig = null;

    public FAStateFigure() {
        super();
        setDecoration(new NullDecoration());
        anchor.setFrameColor(getFrameColor());
        anchor.setAttribute("LineShape",
                            new Integer(PolyLineFigure.BSPLINE_SHAPE));
        anchor.setAttribute("BSplineSegments",
                            new Integer(CH.ifa.draw.util.BSpline.DEFSEGMENTS));
        anchor.setEndDecoration(new ArrowTip());
    }

    public boolean isStartstate() {
        return _decoration instanceof StartDecoration
               || _decoration instanceof StartEndDecoration;
    }

    // ------------------------------ Shadow and simulation processing ------------

    // public void release() {
    // setHighlightFigure(null);
    // super.release();
    // if (shadow != null) {
    // shadow.discard();
    // }
    // }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.gui.ShadowHolder#buildShadow(de.renew.shadow.ShadowNet)
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        logger.debug("buildShadow(Shadownet) called by " + this);
        if (shadow != null) {
            shadow.discard();
        }
        shadow = new ShadowFAState(net);
        shadow.context = this;
        shadow.stateType = getStateType();
        // Set the figure ID to the shadow element to access this figure
        shadow.setID(this.getID());
        shadow.setTrace(getTraceMode());
        //TODO: remove
        logger.debug("built " + shadow);
        return shadow;
    }

    /**
     * Retrieves the type of the state.
     * @return
     */
    public int getStateType() {
        // state type is determined by the FigureDecorations type
        return _decoration instanceof NullDecoration ? NULL
                                                     : (_decoration instanceof StartDecoration
                                                        ? START
                                                        : (_decoration instanceof EndDecoration
                                                           ? END : STARTEND));
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.gui.ShadowHolder#getShadow()
     */
    @Override
    public ShadowNetElement getShadow() {
        return shadow;
    }

    /**
     * Creates an instance figure for this figure.
     *
     * @param drawing The instance drawing to create the figure for.
     * @param netElements The net elements.
     * @return The new instance figure.
     */
    public FAStateInstanceFigure createInstanceFigure(FAInstanceDrawing drawing,
                                                      Hashtable<Serializable, ObjectAccessor> netElements) {
        logger.debug("createInstanceFigure(..) called for " + this);
        return new FAStateInstanceFigure(drawing, this, netElements);
    }

    // ------------------------------ Graphics processing -------------------------  
    public static Dimension defaultDimension() {
        return new Dimension(40, 40);
    }

    private void decorate(Graphics g) {
        if (_decoration != null) {
            _decoration.draw(g, displayBox(), getFillColor(), getFrameColor());
        }
    }

    public FigureDecoration getDecoration() {
        return _decoration;
    }

    @Override
    public void internalDraw(Graphics g) {
        super.internalDraw(g);
        decorate(g);
    }

    public void setDecoration(FigureDecoration decoration) {
        if (decoration == null) {
            _decoration = new NullDecoration();
            return;
        }
        this._decoration = decoration;
    }

    @Override
    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }


    // ------------------------------ Other stuff --------------------------------- 
    @Override
    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new FAConnectionHandle(this,
                                                  RelativeLocator.center(),
                                                  anchor));
        return handles;
    }

    /**
     * Returns all figures with dependencies of the superclass plus an optional
     * hilight figure.
     */
    @Override
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(1);
        myDep.addElement(getHighlightFigure());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    @Override
    public Figure getHighlightFigure() {
        return hilightFig;
    }

    public int getMarkingAppearance() {
        Object value = getAttribute("MarkingAppearance");
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        return HIGHLIGHT;
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    /**
     * Reads the Figure from a StorableInput.
     */
    @Override
    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        if (dr.getVersion() >= 3) {
            setHighlightFigure((Figure) dr.readStorable());

        }
        _decoration = (FigureDecoration) dr.readStorable();
        dr.readString();
    }

    /**
     * Stores the Figure to a StorableOutput.
     */
    @Override
    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeStorable(hilightFig);

        dw.writeStorable(_decoration);
        if (_decoration != null) {
            dw.writeString(_decoration.getClass().getName());
        } else {
            dw.writeString(NullDecoration.class.getName());
        }
    }

    /**
     * Retrieves the states name
     * @return Name of state
     */
    public String getName() {
        String cln = getClass().getName();
        int ind = cln.lastIndexOf('.') + 1;
        if (ind > 0) {
            cln = cln.substring(ind);
        }

        FigureEnumeration children = children();
        while (children.hasMoreElements()) {
            Figure child = children.nextElement();
            if (child instanceof FATextFigure) {
                FATextFigure textFig = (FATextFigure) child;
                if (textFig.getType() == CPNTextFigure.NAME) {
                    return cln + " \"" + textFig.getText() + "\"";
                }
            }
        }

        return cln + " (" + getID() + ")";
    }

    @Override
    public void release() {
        setHighlightFigure(null);
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}