package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.DoubleArcConnection;
import de.renew.gui.HollowDoubleArcConnection;
import de.renew.gui.InhibitorConnection;

import de.renew.shadow.ShadowArc;

import java.awt.Color;
import java.awt.Point;

import java.util.Hashtable;


class ArcParseState implements ParseState {
    NetParseState env;
    ArcConnection figure;

    ArcParseState(NetParseState env, Attributes atts) {
        this.env = env;

        Hashtable<String, String> table = new Hashtable<String, String>();

        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            table.put(atts.getQName(i), atts.getValue(i));
        }

        // Create the arc depending on the type field.
        String type = table.get("type");
        boolean typeSet = true;
        if (type == null) {
            type = "ordinary";
        }
        if ("ordinary".equals(type)) {
            figure = new ArcConnection(ShadowArc.ordinary);
        } else if ("double".equals(type)) {
            figure = new ArcConnection(ShadowArc.both);
        } else if ("test".equals(type)) {
            figure = new ArcConnection(ShadowArc.test);
        } else if ("multi-ordinary".equals(type)) {
            figure = new DoubleArcConnection();
        } else if ("inhibitor".equals(type)) {
            figure = new InhibitorConnection();
        } else if ("clear".equals(type)) {
            figure = new HollowDoubleArcConnection();
        } else {
            typeSet = false;
            figure = new ArcConnection();
        }


        // Set some preliminary end points.
        figure.startPoint(0, 0);
        figure.endPoint(0, 0);

        if (!typeSet) {
            attachErrorFigure("Unknown type: " + type);
        }


        // Make sure that the nodes are correctly attached.
        figure.connectStart(findConnector(table.get("source")));
        figure.connectEnd(findConnector(table.get("target")));
        figure.updateConnection();

        for (int i = 0; i < n; i++) {
            processAttribute(atts.getQName(i), atts.getValue(i));
        }

        env.drawing.add(figure);
    }

    Connector findConnector(String value) {
        int id = NodeParseState.parseID(value);
        Figure node = env.drawing.getFigureWithID(id);
        Point center = node.center();
        return node.connectorAt(center.x, center.y);
    }

    void attachErrorFigure(String msg) {
        TextFigure textFigure = new TextFigure();
        textFigure.setAttribute("FillColor", Color.red);
        textFigure.setText(msg);
        textFigure.setParent(figure);
        env.drawing.add(textFigure);
    }

    void processAttribute(String name, String value) {
        if ("id".equals(name)) {
            figure.setID(NodeParseState.parseID(value));
        } else if (!"source".equals(name) && !"target".equals(name)
                           && !"type".equals(name)) {
            // Remember attribute for reexport.
            figure.setAttribute("XRNA" + name, value);
        }
    }

    public ParseState startElement(String name, Attributes atts) {
        if ("annotation".equals(name)) {
            TextFigure textFigure = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
            textFigure.setParent(figure);
            return new InscriptionState(this, textFigure, env.drawing, atts,
                                        false);
        } else if ("graphics".equals(name)) {
            return new GraphicsParseState(this, figure);
        } else {
            return new IgnoreElementState(this);
        }
    }

    public ParseState endElement(String name) {
        // That's it.
        return env;
    }

    public ParseState characters(char[] ch, int start, int length) {
        // Ignore.
        return this;
    }
}