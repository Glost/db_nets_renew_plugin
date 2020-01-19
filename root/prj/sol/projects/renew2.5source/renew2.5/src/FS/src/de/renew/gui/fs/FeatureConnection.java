package de.renew.gui.fs;

import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Figure;

import de.renew.gui.AssocArrowTip;
import de.renew.gui.InscribableFigure;
import de.renew.gui.PlaceNodeFigure;
import de.renew.gui.TransitionNodeFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;


public class FeatureConnection extends LineConnection
        implements InscribableFigure {
    public final static FeatureConnection NormalArc = new FeatureConnection();

    /**
     * The shadow of this feature connection.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    private transient ShadowNetElement shadow = null;

    public FeatureConnection() {
        fArrowTipClass = AssocArrowTip.class;
        setStartDecoration(null);
        setEndDecoration(new AssocArrowTip());
    }

    /** Build a shadow in the given shadow net.
      *  This shadow is stored as well as returned.
      */
    public ShadowNetElement buildShadow(ShadowNet net) {
        //      shadow = createShadow(getStartFSNode(),getEndFSNode());
        //      shadow.context = this;
        return shadow;
    }

    //protected ShadowNetElement createShadow(ShadowFSNode from, ShadowFSNode to);


    /** Get the associated shadow, if any.
     */
    public ShadowNetElement getShadow() {
        return shadow;
    }

    //  protected ShadowFSNode getStartFSNode() {
    //      return getFSNode(startFigure());
    //  }
    //  protected ShadowFSNode getEndConcept() {
    //      return getFSNode(endFigure());
    //  }
    //  private static ShadowFSNode getFSNode(Figure figure) {
    //      if (canConnect(figure)) {
    //	return (ShadowFSNode)((ShadowHolder)figure).getShadow();
    //    } else {
    //	return null;
    //    }
    //  }
    private static boolean canConnect(Figure figure) {
        return figure instanceof PlaceNodeFigure
               || figure instanceof TransitionNodeFigure
               || figure instanceof FSFigure;
    }

    public boolean canConnect(Figure start, Figure end) {
        return canConnect(start) && canConnect(end);
    }

    public void release() {
        super.release();


        //    if (shadow!=null)
        //      shadow.discard();
    }

    public void setAttribute(String name, Object value) {
        if (!name.equals("ArrowMode")) {
            super.setAttribute(name, value);
        }
    }
}