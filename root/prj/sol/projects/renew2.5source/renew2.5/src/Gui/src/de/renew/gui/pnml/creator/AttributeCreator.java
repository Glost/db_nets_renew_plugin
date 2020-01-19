package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;
import org.w3c.dom.Text;


public class AttributeCreator {
    private Element _element;

    public AttributeCreator(String type) {
        setElement(PNMLCreator.createElement(type));
    }

    protected Element getElement() {
        return _element;
    }

    protected void setElement(Element element) {
        _element = element;
    }

    protected Element createElement(String value) {
        Text text = PNMLCreator.createTextNode("text");
        text.setData(value);
        getElement().appendChild(text);
        return getElement();
    }
}