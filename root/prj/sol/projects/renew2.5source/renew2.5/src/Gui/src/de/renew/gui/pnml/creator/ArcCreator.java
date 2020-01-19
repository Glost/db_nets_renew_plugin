package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;

import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.ArcConnection;
import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;

import de.renew.shadow.ShadowArc;

import java.awt.Color;
import java.awt.Point;


public class ArcCreator extends ObjectCreator {
    private int virStartId = -1;
    private int virEndId = -1;

    /**Create an XMLCreator that parses Renew arcs
     */
    public ArcCreator() {
        super("arc");
    }

    public void setVirStartId(int id) {
        virStartId = id;
    }

    public void setVirEndId(int id) {
        virEndId = id;
    }

    protected int getVirStartId() {
        int result = virStartId;
        return result;
    }

    protected int getVirEndId() {
        int result = virEndId;
        return result;
    }

    /**
     * The ArcConnection returned is the figure saved with this object cast to an ArcConnection
     * @return an ArcConnection
     */
    protected ArcConnection getArc() {
        return (ArcConnection) getFigure();
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectCreator#doCreateObject()
         */
    protected void doCreateObject() {
        int source = ((FigureWithID) getArc().startFigure()).getID();
        int target = ((FigureWithID) getArc().endFigure()).getID();
        getElement().setAttribute("source", "\"" + String.valueOf(source)
                                  + "\"");
        getElement().setAttribute("target", "\"" + String.valueOf(target)
                                  + "\"");
        Element toolSpecific = PNMLCreator.createElement("toolspecific");
        toolSpecific.setAttribute("tool", "\"renew\"");
        toolSpecific.setAttribute("version", "\"2.0\"");

        if (getVirStartId() != -1) {
            toolSpecific.setAttribute("source", "\"" + getVirStartId() + "\"");
        }
        if (getVirEndId() != -1) {
            toolSpecific.setAttribute("target", "\"" + getVirEndId() + "\"");
        }
        if (getVirStartId() != -1 || getVirEndId() != -1) {
            getElement().appendChild(toolSpecific);
        }
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectCreator#doCreateAttribute()
         */
    protected void doCreateAttribute() {
        super.doCreateAttribute();
        String attributeName = "ArcType";
        String attributeValue = "ordinary"; // default value
        switch (getArc().getArcType()) {
        case ShadowArc.ordinary:
            attributeValue = "ordinary";
            break;
        case ShadowArc.both:
            attributeValue = "both";
            break;
        case ShadowArc.inhibitor:
            attributeValue = "inhibitor";
            break;
        case ShadowArc.test:
            attributeValue = "test";
            break;
        case ShadowArc.doubleHollow:
            attributeValue = "clear";
            break;
        case ShadowArc.doubleOrdinary:
            attributeValue = "multi-ordinary";
            break;
        default:
            break;
        }
        NetConverter con = Converter.instance().getNetConverter();
        String pnmltype = con.convertRenewNameToAttributeName(attributeName,
                                                              "arc");
        AttributeCreator creator = new AttributeCreator(pnmltype);
        getElement()
            .appendChild(creator.createElement(con
            .convertRenewValueToAttributeValue(attributeValue, pnmltype, "arc")));
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectCreator#doCreateGraphic(de.renew.gui.pnml.GraphicCreator)
         */
    protected void doCreateGraphic(GraphicCreator creator) {
        int count = getArc().pointCount();
        for (int pos = 1; pos < count - 1; pos++) {
            Point p = getArc().pointAt(pos);
            creator.addPosition(p.x, p.y);
        }
        Color c = (Color) (getFigure()).getAttribute("FrameColor");
        creator.addLineColor(c);
        String s = (String) (getFigure()).getAttribute("LineStyle");
        creator.addLineStyle(s);
        creator.addLine();
    }
}