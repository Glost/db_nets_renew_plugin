package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.TextFigure;


class TextParseState implements ParseState {
    ParseState env;
    TextFigure figure;
    StringBuffer text;

    TextParseState(ParseState env, TextFigure figure) {
        this.env = env;
        this.figure = figure;
        text = new StringBuffer();
    }

    public ParseState startElement(String name, Attributes atts) {
        // There should be no nested elements.
        return new IgnoreElementState(this);
    }

    public ParseState endElement(String name) {
        figure.setText(text.toString());
        // That's it.
        return env;
    }

    public ParseState characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
        return this;
    }
}