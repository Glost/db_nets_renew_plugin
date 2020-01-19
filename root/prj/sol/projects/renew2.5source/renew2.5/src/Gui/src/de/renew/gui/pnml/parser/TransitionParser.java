package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;

import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.TransitionFigure;


/**
 * parse and create Transitions
 */
public class TransitionParser extends NodeParser {
    public TransitionParser(Element node) {
        super(node);

    }

    public FigureWithID createObject() {
        TransitionFigure result = new TransitionFigure();
        return result;
    }
}