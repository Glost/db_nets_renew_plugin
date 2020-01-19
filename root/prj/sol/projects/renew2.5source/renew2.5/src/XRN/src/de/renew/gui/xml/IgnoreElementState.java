package de.renew.gui.xml;

import org.xml.sax.Attributes;


class IgnoreElementState implements ParseState {

    /**
     * The parse state that should be reinstantiated after this
     * state is completed.
     **/
    ParseState orig;

    IgnoreElementState(ParseState orig) {
        this.orig = orig;
    }

    public ParseState startElement(String name, Attributes atts) {
        return new IgnoreElementState(this);
    }

    public ParseState endElement(String name) {
        return orig;
    }

    public ParseState characters(char[] ch, int start, int length) {
        // Ignore;
        return this;
    }
}