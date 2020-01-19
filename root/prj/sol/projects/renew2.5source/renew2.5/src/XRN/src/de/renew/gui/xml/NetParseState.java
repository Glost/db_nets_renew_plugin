package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.CPNDrawing;
import de.renew.gui.DeclarationFigure;


class NetParseState implements ParseState {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetParseState.class);
    CPNDrawing drawing;

    NetParseState(CPNDrawing drawing, Attributes atts) {
        this.drawing = drawing;

        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            if ("name".equals(atts.getQName(i))) {
                drawing.setName(atts.getValue(i));
            }
        }
    }

    public ParseState startElement(String name, Attributes atts) {
        if (logger.isDebugEnabled()) {
            logger.debug(NetParseState.class.getSimpleName()
                         + ": found XML tag: " + name);
        }
        try {
            if ("transition".equals(name)) {
                return new TransitionParseState(this, atts);
            } else if ("arc".equals(name)) {
                return new ArcParseState(this, atts);
            } else if ("place".equals(name)) {
                return new PlaceParseState(this, atts);
            } else if ("annotation".equals(name)) {
                TextFigure textFigure = new DeclarationFigure();
                return new InscriptionState(this, textFigure, drawing, atts,
                                            "name".equals(atts.getValue("type")));
            } else {
                return new IgnoreElementState(this);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    public ParseState endElement(String name) {
        // This ends the only net that may be included in the XML file.
        return null;
    }

    public ParseState characters(char[] ch, int start, int length) {
        // Ignore.
        return this;
    }
}