package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.ArcConnection;
import de.renew.gui.DoubleArcConnection;
import de.renew.gui.HollowDoubleArcConnection;
import de.renew.gui.InhibitorConnection;

import de.renew.shadow.ShadowArc;

import java.awt.Color;
import java.awt.Point;

import java.util.Iterator;
import java.util.Map;


/**
 * parse Arcs
 */
public class ArcParser extends ObjectParser {
    private Map<String, Integer> _keys;
    private String _sourceString;
    private String _targetString;
    private Drawing _net;

    /**Create an XMLParser that parses PNML Arcs
     * @param arc XML element to parse
     * @param keys used to map between PNML and Renew keys
     * @param net to find connections
     */
    public ArcParser(Element arc, Map<String, Integer> keys, Drawing net) {
        super(arc);
        setKeys(keys);
        setNet(net);
    }

    protected Map<String, Integer> keys() {
        return _keys;
    }

    protected void setKeys(Map<String, Integer> map) {
        _keys = map;
    }

    protected String sourceString() {
        return _sourceString;
    }

    protected void setSourceString(String source) {
        _sourceString = source;
    }

    protected String targetString() {
        return _targetString;
    }

    protected void setTargetString(String target) {
        _targetString = target;
    }

    protected Drawing net() {
        return _net;
    }

    protected void setNet(Drawing net) {
        _net = net;
    }

    protected Color getLineColor() {
        Color result = null;
        if (getGraphic().hasLineColor()) {
            result = getGraphic().getLineColor();
        }
        return result;
    }

    protected String getLineStyle() {
        String result = "";
        if (getGraphic().hasLineStyle()) {
            result = getGraphic().getLineStyle();
        }
        return result;
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectParser#createObject()
         */
    protected FigureWithID createObject() {
        ArcConnection result = new ArcConnection(ShadowArc.ordinary);
        Iterator<AttributeParser> attributes = getAttributes().iterator();
        while (attributes.hasNext()) {
            AttributeParser parser = attributes.next();
            if (parser.attributeName().equals("ArcType")) {
                String type = parser.attributeValue();
                if (type.equals("inhibitor")) {
                    result = new InhibitorConnection();
                } else if (type.equals("multi-ordinary")) {
                    result = new DoubleArcConnection();
                } else if (type.equals("clear")) {
                    result = new HollowDoubleArcConnection();
                } else if (type.equals("both")) {
                    result = new ArcConnection(ShadowArc.both);
                } else if (type.equals("test")) {
                    result = new ArcConnection(ShadowArc.test);
                } else {
                    result = new ArcConnection(ShadowArc.ordinary);
                }
            }
        }
        result.startPoint(0, 0);
        result.endPoint(0, 0);
        return result;
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectParser#doParse()
         */
    protected void parseFigure() {
        String source = getElement().getAttribute("source");
        String target = getElement().getAttribute("target");
        NodeList toolSpecs = getElement().getElementsByTagName("toolspecific");
        if (toolSpecs.getLength() > 0) {
            for (int pos = 0; pos < toolSpecs.getLength(); pos++) {
                Element toolSpec = (Element) toolSpecs.item(pos);
                if (toolSpec.getAttribute("tool").equals("renew")) {
                    if (toolSpec.hasAttribute("source")) {
                        source = toolSpec.getAttribute("source");
                    }
                    if (toolSpec.hasAttribute("target")) {
                        target = toolSpec.getAttribute("target");
                    }
                }
            }
        }
        setSourceString(source);
        setTargetString(target);
    }

    protected Connector findConnector(Drawing net, int id) {
        Connector result = null;
        FigureEnumeration figures = net.figures();
        FigureWithID figure = null;
        while (figures.hasMoreElements()) {
            Figure element = figures.nextElement();
            if (element instanceof FigureWithID) {
                figure = (FigureWithID) element;
                if (figure.getID() == id) {
                    break;
                }
            }
        }
        if (figure != null) {
            Point center = figure.center();
            result = figure.connectorAt(center.x, center.y);
        }
        return result;
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectParser#doInitFigure()
         */
    protected void doInitFigure() {
        int target;
        int source;
        ArcConnection arc = (ArcConnection) figure();
        if (keys().containsKey(sourceString())) {
            source = (keys().get(sourceString())).intValue();
        } else {
            source = Integer.parseInt(sourceString());
        }
        if (keys().containsKey(targetString())) {
            target = (keys().get(targetString())).intValue();
        } else {
            target = Integer.parseInt(targetString());
        }
        arc.connectStart(findConnector(net(), source));
        arc.connectEnd(findConnector(net(), target));
        arc.updateConnection();
        super.doInitFigure();
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ObjectParser#initGraphic()
         */
    protected void initGraphic() {
        ArcConnection arc = (ArcConnection) figure();
        int pos = 1;
        Iterator<Point> positionen = getGraphic().positions();
        while (positionen.hasNext()) {
            Point element = positionen.next();
            arc.insertPointAt(element, pos++);
        }
        if (getLineColor() != null) {
            figure().setAttribute("FrameColor", getLineColor());
        }
        figure().setAttribute("LineStyle", getLineStyle());
    }
}