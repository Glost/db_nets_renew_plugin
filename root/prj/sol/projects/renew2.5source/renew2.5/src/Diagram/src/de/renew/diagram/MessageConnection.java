/*
 * @(#)MessageConnection.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.AssocArrowTip;
import de.renew.gui.InscribableFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.awt.Point;


// Added AssocArrowTip to the package 
//import de.renew.gui.fs.AssocArrowTip;
public class MessageConnection extends LineConnection implements IDiagramElement,
                                                                 InscribableFigure {
    public MessageConnection() {
        super();
        ArrowTip start = null;
        ArrowTip end = new AssocArrowTip();

        setStartDecoration(start);
        setEndDecoration(end);
        setAttribute("ArrowTip", AssocArrowTip.class.getName());

    }

    /** Creates a new MessageConnection of a specific type.
     * @param arcType The type is defined according to
     *                the constants in
     *                {@link de.renew.shadow.ShadowArc}.
     */
    public MessageConnection(int arcType) {
        ArrowTip start = null;
        ArrowTip end = null;


        //setLineStyle(LINE_STYLE_DASHED);
        //start = new ArrowTip();
        end = new AssocArrowTip();

        setStartDecoration(start);
        setEndDecoration(end);
        setAttribute("ArrowTip", AssocArrowTip.class.getName());


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
               || start instanceof TaskFigure && end instanceof HSplitFigure
               || start instanceof HSplitFigure && end instanceof TaskFigure
               || start instanceof HSplitFigure && end instanceof HSplitFigure);
    }

    //    public boolean getTraceMode() {
    //        Object value = getAttribute("TraceMode");
    //        if (value instanceof Boolean) {
    //            return ((Boolean) value).booleanValue();
    //        }
    //        return true;
    //    }
    public boolean isReverse() {
        return ((Integer) getAttribute("ArrowMode")).intValue() == ARROW_TIP_START;
    }

    /**
     *
     */
    public void updatePeerNames() {
        Figure start = startFigure();
        if (start instanceof TaskFigure) {
            ((TaskFigure) start).addPeerName("out");
        }
        Figure end = endFigure();
        if (end instanceof TaskFigure) {
            ((TaskFigure) end).addPeerName("in");
        }
    }

    /**
     *
     */
    public void notifyConnectorOwners() {
        Figure start = (startFigure());
        if (start instanceof TaskFigure) {
            ((TaskFigure) start).addMessage(this);
        }
        Figure end = endFigure();
        if (end instanceof TaskFigure) {
            ((TaskFigure) end).addMessage(this);
        }
    }

    public Connector getConnectedConnector(Figure fig) {
        Connector connector = start();
        if (!connector.owner().equals(fig)) {
            connector = end();
        }

        return connector;
    }

    public Point getConnectedPoint(Figure fig) {
        Figure figure = startFigure();
        Point point = startPoint();
        if (!figure.equals(fig)) {
            point = endPoint();
        }
        return point;
    }

    public String getMessageText() {
        String text = null;
        FigureEnumeration enumeration = children();
        while (enumeration.hasMoreElements()) {
            Figure textFigure = enumeration.nextElement();
            if (textFigure instanceof DiagramTextFigure) {
                DiagramTextFigure dtf = (DiagramTextFigure) textFigure;
                text = dtf.getRealText();
            }
        }
        return text;

    }

    /* (non-Javadoc)
     * @see de.renew.gui.ShadowHolder#buildShadow(de.renew.shadow.ShadowNet)
     */
    public ShadowNetElement buildShadow(ShadowNet net) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.ShadowHolder#getShadow()
     */
    public ShadowNetElement getShadow() {
        // TODO Auto-generated method stub
        return null;
    }
}