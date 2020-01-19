package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;


/**
 * parse Atrributes
 */
public class AttributeParser extends ElementParser {
    private String _text;

    public AttributeParser(Element attribut) {
        super(attribut);
    }

    protected String getText() {
        return _text;
    }

    protected void setText(String text) {
        _text = text;
    }

    protected void doParse() {
        NodeList texte = getElement().getElementsByTagName("text");
        if (texte.getLength() == 0) {
            texte = getElement().getElementsByTagName("value");
        }
        for (int pos = 0; pos < texte.getLength(); pos++) {
            Element textEle = (Element) texte.item(pos);
            Text text = (Text) textEle.getFirstChild();
            setText(text.getData());
        }
    }

    public String attributeName() {
        String result = "";
        NetConverter con = Converter.instance().getNetConverter();
        result = con.convertAttributNameToRenewName(getElement());
        return result;
    }

    public String attributeValue() {
        String result = "";
        Node parent = getElement().getParentNode();
        NetConverter con = Converter.instance().getNetConverter();
        result = con.convertAttributValueToRenewValue(getText(),
                                                      attributeName(),
                                                      parent.getNodeName());
        return result;
    }
}