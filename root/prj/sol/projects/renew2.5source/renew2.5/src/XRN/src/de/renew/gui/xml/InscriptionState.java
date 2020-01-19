package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNTextFigure;


class InscriptionState implements ParseState {
    ParseState env;
    TextFigure figure;
    CPNDrawing drawing;
    StringBuffer text;
    boolean isName;

    InscriptionState(ParseState env, TextFigure figure, CPNDrawing drawing,
                     Attributes atts, boolean isName) {
        this.env = env;
        this.figure = figure;
        this.drawing = drawing;
        this.isName = isName;

        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            processAttribute(atts.getQName(i), atts.getValue(i));
        }

        if (!isName) {
            // ID should be set. Adding is now possible.
            drawing.add(figure);
        }
    }

    void processAttribute(String name, String value) {
        if ("id".equals(name)) {
            figure.setID(NodeParseState.parseID(value));
        } else if ("type".equals(name)) {
            if (figure instanceof CPNTextFigure) {
                if ("comment".equals(value)) {
                    figure.setAttribute("TextType",
                                        new Integer(CPNTextFigure.LABEL));
                    return;
                } else if ("name".equals(value)) {
                    figure.setAttribute("TextType",
                                        new Integer(CPNTextFigure.NAME));
                    return;
                }
            }


            // Remember attribute for reexport.
            // At the moment, no detailed evaluation is done.
            figure.setAttribute("XRNA" + name, value);
        } else {
            // Remember attribute for reexport.
            figure.setAttribute("XRNA" + name, value);
        }
    }

    public ParseState startElement(String name, Attributes atts) {
        if ("text".equals(name)) {
            return new TextParseState(this, figure);
        } else if ("graphics".equals(name)) {
            return new GraphicsParseState(this, figure);
        } else {
            return new IgnoreElementState(this);
        }
    }

    public ParseState endElement(String name) {
        if (isName) {
            drawing.setName(figure.getText());
        }

        // That's it.
        return env;
    }

    public ParseState characters(char[] ch, int start, int length) {
        // Ignore.
        return this;
    }
}