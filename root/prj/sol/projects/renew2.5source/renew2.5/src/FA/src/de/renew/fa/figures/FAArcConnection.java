package de.renew.fa.figures;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.LineDecoration;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.fa.FAInstanceDrawing;

import de.renew.faformalism.shadow.ShadowFAArc;
import de.renew.faformalism.shadow.ShadowFAState;

import de.renew.gui.CPNInstanceDrawing;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.FigureWithHighlight;
import de.renew.gui.InscribableFigure;
import de.renew.gui.ShadowHolder;

import de.renew.remote.ObjectAccessor;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.io.Serializable;

import java.util.Hashtable;


/**
 * An arc, aka transition, connecting two states.
 *
 * @author cabac
 * @author moeller
 */
public class FAArcConnection extends LineConnection implements InscribableFigure,
                                                               FigureWithHighlight {
    static final long serialVersionUID = 1968826680828590162L;
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAArcConnection.class);

    /**
     * The shadow of this FAArcConnection
     */
    private transient ShadowFAArc shadow = null;

    /**
     * This figure will be highlighted in a instance
     * drawing in the same manner as the transition
     * will be highlighted. May be <code>null</code>.
     * @serial
     **/
    private Figure hilightFig = null;

    /**
     * Creates a new FAArcConnection.
     */
    public FAArcConnection() {
        super(null, new ArrowTip(), AttributeFigure.LINE_STYLE_NORMAL);
    }

    /**
     * Creates a new FAArcConnection with
     * specific decorations at start and end;
     * @param start                 the start LineDecoration
     * @param end                 the end LineDecoration
     * @param lineStyle  the style of the line
     */
    public FAArcConnection(LineDecoration start, LineDecoration end,
                           String lineStyle) {
        super(start, end, lineStyle);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.gui.ShadowHolder#buildShadow(de.renew.shadow.ShadowNet)
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        logger.debug("buildShadow(ShadowNet) called by " + this);
        if (shadow != null) {
            shadow.discard();
        }
        shadow = new ShadowFAArc(startShadow(), endShadow());
        shadow.context = this;
        shadow.setID(this.getID());
        shadow.setTrace(getTraceMode());
        logger.debug("built " + shadow);
        return shadow;
    }

    private ShadowFAState startShadow() {
        return (ShadowFAState) ((ShadowHolder) startFigure()).getShadow();
    }

    private ShadowFAState endShadow() {
        return (ShadowFAState) ((ShadowHolder) endFigure()).getShadow();
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
    public FAArcInstanceConnection createInstanceFigure(CPNInstanceDrawing drawing,
                                                        Hashtable<Serializable, ObjectAccessor> netElements) {
        //TODO: implement FAArcInstanceFigure
        return new FAArcInstanceConnection((FAInstanceDrawing) drawing, this,
                                           netElements);
    }

    /*
     * Checks if two figures can be connected by this connection.
     *
     * @see CH.ifa.draw.figures.LineConnection#canConnect()
     */
    @Override
    public boolean canConnect(Figure start, Figure end) {
        if (start instanceof FAStateFigure && end instanceof FAStateFigure) {
            return true;
        }
        return false;
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    @Override
    public void setHighlightFigure(Figure fig) {
        hilightFig = fig;
    }

    @Override
    public Figure getHighlightFigure() {
        return hilightFig;
    }

    /**
     * Retrieves the arcs name
     * @return name of arc
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
                    return cln + "(" + textFig.getText() + ")";
                }
            }
        }

        return cln + " (" + getID() + ")";
    }

    @Override
    public String toString() {
        return getName();
    }
}