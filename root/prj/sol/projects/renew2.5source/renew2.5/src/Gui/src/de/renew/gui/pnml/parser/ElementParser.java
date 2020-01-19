package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;


/**
 * parse all PNML Elements(PetriNets, Objects, Labels)
 */
public abstract class ElementParser {
    private Element _element;

    public ElementParser(Element element) {
        setElement(element);
    }

    public Element getElement() {
        return _element;
    }

    protected void setElement(Element element) {
        _element = element;
    }

    /**
     * Parse a PNML Element; use Template Pattern
     */
    public void parse() {
        doParse();
    }

    protected abstract void doParse();
}