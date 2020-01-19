package de.renew.gui.xml;

import org.xml.sax.Attributes;

import CH.ifa.draw.figures.AttributeFigure;

import de.renew.gui.PlaceFigure;


class PlaceParseState extends NodeParseState {
    PlaceParseState(NetParseState env, Attributes atts) {
        super(env, atts);
    }

    AttributeFigure createFigure() {
        return new PlaceFigure();
    }
}