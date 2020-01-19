package de.renew.gui.xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.renew.gui.CPNDrawing;

import de.renew.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;


public class XRNParser extends DefaultHandler {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(XRNParser.class);
    private CPNDrawing result;
    private ParseState state;

    XRNParser() {
        result = new CPNDrawing();
        state = null;
    }

    public static CPNDrawing parse(InputStream stream)
            throws SAXException, IOException {
        InputSource inputSource = new InputSource(stream);
        XMLReader parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (FactoryConfigurationError e) {
            throw new RuntimeException(e);
        }
        XRNParser handler = new XRNParser();
        parser.setContentHandler(handler);
        parser.setEntityResolver(handler);
        parser.parse(inputSource);
        return handler.result;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId.startsWith("http://www.renew.de/")
                    || systemId.startsWith("http://www.informatik.uni-hamburg.de/TGI/renew/")) {
            String dtd = StringUtil.getExtendedFilename(systemId);
            System.out.println("[XRNParser] dtd is " + dtd);
            InputStream stream = getClass().getResourceAsStream(dtd);
            if (logger.isDebugEnabled()) {
                logger.debug(XRNParser.class.getSimpleName()
                             + ": DTD input stream: "
                             + getClass().getResource(dtd) + stream);
            }
            if (stream == null) {
                return null;
            } else {
                return new InputSource(stream);
            }
        } else {
            return null;
        }
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes atts)
    // old sax 1 (?) interface
    // public void startElement(String name, Attributes atts)
            throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug(XRNParser.class.getSimpleName()
                         + ": startElement Name: " + name + " " + "localName: "
                         + localName + " Atts: " + atts);
        }
        if (state != null) {
            state = state.startElement(name, atts);
        } else if ("net".equals(name)) {
            state = new NetParseState(result, atts);
        } else {
            state = new IgnoreElementState(null);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
    // old sax 1 (?) interface
    // public void endElement(String name)
            throws SAXException {
        if (state != null) {
            state = state.endElement(name);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (state != null) {
            state = state.characters(ch, start, length);
        }
    }
}