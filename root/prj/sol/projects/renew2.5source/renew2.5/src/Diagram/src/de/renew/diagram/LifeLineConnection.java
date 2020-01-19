/*
 * @(#)LifeLineConnection.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Figure;


public class LifeLineConnection extends LineConnection
        implements IDiagramElement {
    /*
     * Serialization support.
     */


    //    public static final LifeLineConnection NormalArc = new LifeLineConnection(
    //                                                          ShadowArc.ordinary);
    //    public static final LifeLineConnection TestArc = new LifeLineConnection(
    //                                                        ShadowArc.test);
    //    public static final LifeLineConnection ReserveArc = new LifeLineConnection(
    //                                                           ShadowArc.both);
    //    private int messageConnectionSerializedDataVersion = 1;
    public LifeLineConnection() {
    }

    /** Creates a new LifeLineConnection of a specific type.
     * @param arcType The type is defined according to
     *                the constants in
     *                {@link de.renew.shadow.ShadowArc}.
     */
    public LifeLineConnection(int arcType) {
        ArrowTip start = null;
        ArrowTip end = null;
        setLineStyle(LINE_STYLE_DOTTED);


        //end = new ArrowTip();
        setStartDecoration(start);
        setEndDecoration(end);
    }

    //    public int getArcType() {
    //        int arrowMode = ((Integer) getAttribute("ArrowMode")).intValue();
    //        if (arrowMode == ARROW_TIP_NONE) {
    //            return ShadowArc.test;
    //        }
    //        if (arrowMode == ARROW_TIP_BOTH) {
    //            return ShadowArc.both;
    //        }
    //        return ShadowArc.ordinary;
    //    }
    public boolean canConnect(Figure start, Figure end) {
        return (start instanceof TaskFigure && end instanceof TaskFigure
               || start instanceof RoleDescriptorFigure
               && end instanceof TaskFigure
               || start instanceof TaskFigure
               && end instanceof RoleDescriptorFigure
               || start instanceof TaskFigure && end instanceof ISplitFigure
               || start instanceof ISplitFigure && end instanceof TaskFigure
               || start instanceof ISplitFigure && end instanceof ISplitFigure
               || start instanceof RoleDescriptorFigure
               && end instanceof ISplitFigure
               || start instanceof ISplitFigure
               && end instanceof RoleDescriptorFigure
               || start instanceof TaskFigure && end instanceof SplitFigure
               || start instanceof SplitFigure && end instanceof TaskFigure
               || start instanceof SplitFigure && end instanceof SplitFigure
               || start instanceof RoleDescriptorFigure
               && end instanceof SplitFigure
               || start instanceof SplitFigure
               && end instanceof RoleDescriptorFigure);
    }

    /**
     *
     */
    public void notifyConnectorOwners() {
        ((TailFigure) startFigure()).addConnection(this);
        ((TailFigure) endFigure()).addConnection(this);
    }

    /**
     * @param tailable - one of the TailFigures of this connection.
     * @return the opposite/other TailFigure on this connection.
     */
    public TailFigure otherEnd(TailFigure tailable) {
        TailFigure fig = (TailFigure) this.endFigure();
        if (fig.equals(tailable)) {
            fig = (TailFigure) this.startFigure();
        }

        return fig;
    }

    //    public boolean getTraceMode() {
    //        Object value = getAttribute("TraceMode");
    //        if (value instanceof Boolean) {
    //            return ((Boolean) value).booleanValue();
    //        }
    //        return true;
    //    }
}