package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.CPNTextFigure;

import java.awt.Color;


abstract class NodeParseState implements ParseState {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(NodeParseState.class);
    NetParseState env;
    AttributeFigure figure;

    NodeParseState(NetParseState env, Attributes atts) {
        this.env = env;
        figure = createFigure();

        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            processAttribute(atts.getQName(i), atts.getValue(i));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(NodeParseState.class.getSimpleName() + ": add figure "
                         + figure.getClass().getSimpleName());
        }
        env.drawing.add(figure);
    }

    abstract AttributeFigure createFigure();

    void attachErrorFigure(String msg) {
        TextFigure textFigure = new TextFigure();
        textFigure.setAttribute("FillColor", Color.red);
        textFigure.setText(msg);
        textFigure.setParent(figure);
        env.drawing.add(textFigure);
    }

    public static int parseID(String value) {
        return Integer.parseInt(value.substring(1));
    }

    void processAttribute(String name, String value) {
        if ("id".equals(name)) {
            figure.setID(parseID(value));
        } else if ("type".equals(name)) {
            if (!"ordinary".equals(value)) {
                attachErrorFigure("Unknown type: " + value);
            }
        } else {
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