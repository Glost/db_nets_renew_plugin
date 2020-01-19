package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;


public abstract class ElementCreator {
    private Element _element;
    private Object _object;

    public ElementCreator(String tag) {
        setElement(PNMLCreator.createElement(tag));
    }

    protected Element getElement() {
        return _element;
    }

    protected void setElement(Element element) {
        _element = element;
    }

    protected Object getObject() {
        return _object;
    }

    protected void setObject(Object object) {
        _object = object;
    }

    protected void doCreateElement() {
    }

    public Element createElement(Object object) {
        //if (objectClass().isInstance(object)) {
        setObject(object);
        doCreateElement();
        //}
        return getElement();
    }
}