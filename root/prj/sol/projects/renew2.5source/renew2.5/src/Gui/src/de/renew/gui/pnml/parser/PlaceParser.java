package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;

import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.PlaceFigure;


/**
 * parse and create Places
 */
public class PlaceParser extends NodeParser {
    public PlaceParser(Element node) {
        super(node);

    }

    public FigureWithID createObject() {
        PlaceFigure result = new PlaceFigure();
        return result;
    }
}