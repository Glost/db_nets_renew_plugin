/*
 * @(#)ArcConnection.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Figure;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNode;


public class ArcConnection extends LineConnection implements InscribableFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -7959501008698525009L;
    public static final ArcConnection NormalArc = new ArcConnection(ShadowArc.ordinary);
    public static final ArcConnection TestArc = new ArcConnection(ShadowArc.test);
    public static final ArcConnection ReserveArc = new ArcConnection(ShadowArc.both);
    @SuppressWarnings("unused")
    private int arcConnectionSerializedDataVersion = 1;

    /**
     * The shadow of this arc connection.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    private transient ShadowArc shadow = null;

    public ArcConnection() {
    }

    /** Creates a new ArcConnection of a specific type.
     * @param arcType The type is defined according to
     *                the constants in
     *                {@link de.renew.shadow.ShadowArc}.
     */
    public ArcConnection(int arcType) {
        ArrowTip start = null;
        ArrowTip end = null;
        if (arcType == ShadowArc.both) {
            start = new ArrowTip();
        }
        if (arcType != ShadowArc.test) {
            end = new ArrowTip();
        }
        setStartDecoration(start);
        setEndDecoration(end);
    }

    public int getArcType() {
        int arrowMode = ((Integer) getAttribute("ArrowMode")).intValue();
        if (arrowMode == ARROW_TIP_NONE) {
            return ShadowArc.test;
        }
        if (arrowMode == ARROW_TIP_BOTH) {
            return ShadowArc.both;
        }
        return ShadowArc.ordinary;
    }

    public boolean canConnect(Figure start, Figure end) {
        return (start instanceof TransitionNodeFigure
               && end instanceof PlaceNodeFigure
               || start instanceof PlaceNodeFigure
               && end instanceof TransitionNodeFigure);
    }

    public boolean getTraceMode() {
        Object value = getAttribute("TraceMode");
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return true;
    }

    /**
     * Sets an attribute of the figure. ArcConnection understands
     * all attributes of its superclass and adjusts its semantics
     * in accordance to the value of "ArrowMode".
     * The attribute "ArrowTip" is ignored because it would lead
     * to a divergence of appearance and semantics.
     **/
    public void setAttribute(String name, Object value) {
        if (name.equals("ArrowTip")) {
            // Ignore. Not yet implemented.
        } else {
            super.setAttribute(name, value);
        }
    }

    public void release() {
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowArc(startShadow(), endShadow(), getArcType());
        shadow.context = this;
        shadow.setID(this.getID());
        shadow.setTrace(getTraceMode());
        return shadow;


        // No longer calling the syntax check. This is done
        // after editing the text and in no other place should a
        // syntax check occur.
    }

    public boolean isReverse() {
        return ((Integer) getAttribute("ArrowMode")).intValue() == ARROW_TIP_START;
    }

    private ShadowNode anyShadow(boolean end) {
        ShadowHolder holder = (ShadowHolder) (end ? endFigure() : startFigure());
        return (ShadowNode) holder.getShadow();
    }

    ShadowNode startShadow() {
        return anyShadow(isReverse());
    }

    ShadowNode endShadow() {
        return anyShadow(!isReverse());
    }

    public ShadowNetElement getShadow() {
        return shadow;
    }
}