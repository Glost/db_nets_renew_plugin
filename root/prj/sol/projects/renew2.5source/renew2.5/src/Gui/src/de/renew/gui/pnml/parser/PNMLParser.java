package de.renew.gui.pnml.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import CH.ifa.draw.framework.Drawing;

import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Use Singleton Patterm
 */
public class PNMLParser {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PNMLParser.class);
    private static PNMLParser _instance;

    public static PNMLParser instance() {
        if (_instance == null) {
            _instance = new PNMLParser();
        }
        return _instance;
    }

    private PNMLParser() {
    }

    public Drawing[] parse(InputStream pnmlStream) {
        Drawing[] result = null;
        Vector<Drawing> liste = new Vector<Drawing>();
        try {
            // Erstellen des DOM Parsers
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            //factory.setValidating(true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Erstellen des DOM Baums
            Document doc = builder.parse(pnmlStream);

            NodeList nets = doc.getElementsByTagName("net");
            for (int pos = 0; pos < nets.getLength(); pos++) {
                Element net = (Element) nets.item(pos);
                NetParser netParser = new NetParser(net);
                netParser.parse();
                liste.add(netParser.getNet());
            }
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage(), e);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        result = new Drawing[liste.size()];
        for (int pos = 0; pos < result.length; pos++) {
            result[pos] = liste.get(pos);
        }
        return result;
    }
}