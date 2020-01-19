package de.renew.gui.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


interface ParseState {
    public ParseState startElement(String name, Attributes atts)
            throws SAXException;

    public ParseState endElement(String name) throws SAXException;

    public ParseState characters(char[] ch, int start, int length)
            throws SAXException;
}