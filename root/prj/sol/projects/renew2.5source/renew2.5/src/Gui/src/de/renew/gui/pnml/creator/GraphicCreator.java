package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;

import de.renew.gui.pnml.converter.GraphicConverter;

import java.awt.Color;


public class GraphicCreator {
    private Element _element;
    private Element line;

    public GraphicCreator() {
        setElement(PNMLCreator.createElement("graphics"));
    }

    protected Element getElement() {
        return _element;
    }

    protected void setElement(Element element) {
        _element = element;
    }

    public Element createGraphic() {
        return getElement();
    }

    public void addPosition(int x, int y) {
        Element position = PNMLCreator.createElement("position");
        position.setAttribute("x", "\"" + String.valueOf(x) + "\"");
        position.setAttribute("y", "\"" + String.valueOf(y) + "\"");
        getElement().appendChild(position);
    }

    public void addOffset(int x, int y) {
        Element offset = PNMLCreator.createElement("offset");
        offset.setAttribute("x", "\"" + String.valueOf(x) + "\"");
        offset.setAttribute("y", "\"" + String.valueOf(y) + "\"");
        getElement().appendChild(offset);
    }

    public void addDimension(int x, int y) {
        Element dimension = PNMLCreator.createElement("dimension");
        dimension.setAttribute("x", "\"" + String.valueOf(x) + "\"");
        dimension.setAttribute("y", "\"" + String.valueOf(y) + "\"");
        getElement().appendChild(dimension);
    }

    public void addFill(Color c) {
        Element fill = PNMLCreator.createElement("fill");
        fill.setAttribute("color",
                          "\"rgb(" + c.getRed() + "," + c.getGreen() + ","
                          + c.getBlue() + ")\"");
        getElement().appendChild(fill);
    }

    public void addLineColor(Color c) {
        getLineElement()
            .setAttribute("color",
                          "\"rgb(" + c.getRed() + "," + c.getGreen() + ","
                          + c.getBlue() + ")\"");
    }

    public void addLineStyle(String style) {
        getLineElement()
            .setAttribute("style",
                          "\""
                          + GraphicConverter.instance()
                                            .parseRenewLineStyle(style) + "\"");
    }

    public void addLine() {
        getElement().appendChild(getLineElement());
    }

    private Element getLineElement() {
        if (line == null) {
            line = PNMLCreator.createElement("line");
        }
        return line;
    }
}