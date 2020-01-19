package de.renew.gui.fs;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Figure;

import de.renew.formalism.fs.ShadowConcept;

import de.renew.gui.ShadowHolder;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;


public abstract class ConceptConnection extends LineConnection
        implements ShadowHolder {

    /**
     * The shadow of this concept connection.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    private transient ShadowNetElement shadow = null;

    protected ConceptConnection(ArrowTip arrowTip) {
        fArrowTipClass = arrowTip.getClass();
        setStartDecoration(null);
        setEndDecoration(arrowTip);
    }

    /** Build a shadow in the given shadow net.
      *  This shadow is stored as well as returned.
      */
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = createShadow(getStartConcept(), getEndConcept());
        shadow.context = this;
        return shadow;
    }

    protected abstract ShadowNetElement createShadow(ShadowConcept from,
                                                     ShadowConcept to);

    /** Get the associated shadow, if any.
     */
    public ShadowNetElement getShadow() {
        return shadow;
    }

    protected ShadowConcept getStartConcept() {
        return getConcept(startFigure());
    }

    protected ShadowConcept getEndConcept() {
        return getConcept(endFigure());
    }

    private static ShadowConcept getConcept(Figure figure) {
        if (canConnect(figure)) {
            return (ShadowConcept) ((ShadowHolder) figure).getShadow();
        } else {
            return null;
        }
    }

    private static boolean canConnect(Figure figure) {
        return figure instanceof ConceptFigure;
    }

    public boolean canConnect(Figure start, Figure end) {
        return canConnect(start) && canConnect(end);
    }

    public void release() {
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    public void setAttribute(String name, Object value) {
        if (!name.equals("ArrowMode")) {
            super.setAttribute(name, value);
        }
    }
}