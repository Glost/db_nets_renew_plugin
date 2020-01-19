package de.renew.gui.pnml.creator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.renew.gui.CPNDrawing;
import de.renew.gui.pnml.converter.Converter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class PNMLCreator {
    private static Document _doc;
    private PrintWriter _writer;

    public PNMLCreator() {
    }

    protected Document getDoc() {
        return _doc;
    }

    protected void setDoc(Document doc) {
        _doc = doc;
    }

    protected PrintWriter writer() {
        return _writer;
    }

    protected void setWriter(PrintWriter writer) {
        _writer = writer;
    }

    protected void makeDoc() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        DocumentType dt = db.getDOMImplementation()
                            .createDocumentType("PNMLFile", null, null);
        String nettype = Converter.instance().getType();
        Document doc = db.getDOMImplementation()
                         .createDocument(nettype, "PNMLFile", dt);
        setDoc(doc);
    }

    public void write(OutputStream stream, CPNDrawing drawing)
            throws Exception {
        makeDoc();
        setWriter(new PrintWriter(new OutputStreamWriter(stream, "UTF-8")));
        writeImpl(drawing);
        makeXML();
    }

    public void write(OutputStream stream, CPNDrawing[] drawings)
            throws Exception {
        makeDoc();
        setWriter(new PrintWriter(new OutputStreamWriter(stream, "UTF-8")));
        for (int pos = 0; pos < drawings.length; pos++) {
            writeImpl(drawings[pos]);
        }
        makeXML();
    }

    private void writeImpl(CPNDrawing drawing) {
        Element eRoot = getDoc().getDocumentElement();
        NetCreator netCreator = new NetCreator();
        eRoot.appendChild(netCreator.createElement(drawing));
    }

    private String tab(int length) {
        String result = "";
        for (int x = 1; x <= length; x++) {
            result = result + "   ";
        }
        return result;
    }

    private void makeNode(Node node, int tabCount) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            makeElementNode((Element) node, tabCount);
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            makeTextNode((Text) node, tabCount);
        }
    }

    private void makeElementNode(Element node, int tabCount) {
        String tag = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        writer().print(tab(tabCount) + "<" + tag);
        for (int pos = 0; pos < attributes.getLength(); pos++) {
            Attr attribute = (Attr) attributes.item(pos);
            writer().print(" " + attribute.getName() + "=");
            writer().print(attribute.getValue());
        }
        NodeList chields = node.getChildNodes();
        if (chields.getLength() == 0) {
            writer().println("/>");
        } else {
            writer().println(">");
            for (int pos = 0; pos < chields.getLength(); pos++) {
                Node chield = chields.item(pos);
                makeNode(chield, tabCount + 1);
            }
            writer().println(tab(tabCount) + "</" + tag + ">");
        }
    }

    private void makeTextNode(Text node, int tabCount) {
        writer().print(tab(tabCount) + "<" + "text" + ">");
        writer().print(node.getData());
        writer().println("</" + "text" + ">");
    }

    public void makeXML() {
        NodeList nets = getDoc().getDocumentElement().getChildNodes();
        String nettype = Converter.instance().getType();
        writer().println("<pnml xmlns=\"" + nettype + "\">");

        int tabCount = 1;
        for (int pos = 0; pos < nets.getLength(); pos++) {
            makeNode(nets.item(pos), tabCount);
        }
        writer().println("</pnml>");
        writer().flush();

    }

    public static Element createElement(String tag) {
        return _doc.createElement(tag);
    }

    public static Text createTextNode(String tag) {
        return _doc.createTextNode(tag);
    }
}