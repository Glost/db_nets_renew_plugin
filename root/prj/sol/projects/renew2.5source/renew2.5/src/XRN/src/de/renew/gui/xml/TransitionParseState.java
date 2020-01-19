package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.AttributeFigure;

import de.renew.gui.TransitionFigure;


class TransitionParseState extends NodeParseState {
    TransitionParseState(NetParseState env, Attributes atts) {
        super(env, atts);
    }

    AttributeFigure createFigure() {
        return new TransitionFigure();
    }
}