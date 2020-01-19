package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.renew.gui.pnml.converter.GraphicConverter;

import java.awt.Color;
import java.awt.Point;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * parse all Graphicitems
 */
public class GraphicParser extends ElementParser {
    // Attribute
    private NodeList positions;
    private NodeList dimension;
    private Element offSet;
    private Element fill;
    private Element line;

    // Konstruktor
    public GraphicParser(Element graphic) {
        super(graphic);
    }

    // Methoden
    protected void doParse() {
        if (getElement() != null) {
            positions = getElement().getElementsByTagName("position");
            dimension = getElement().getElementsByTagName("dimension");
            NodeList nodeList;
            nodeList = getElement().getElementsByTagName("offset");
            if (nodeList.getLength() > 0) {
                offSet = (Element) nodeList.item(0);
            }
            nodeList = getElement().getElementsByTagName("fill");
            if (nodeList.getLength() > 0) {
                fill = (Element) nodeList.item(0);
            }
            nodeList = getElement().getElementsByTagName("line");
            if (nodeList.getLength() > 0) {
                line = (Element) nodeList.item(0);
            }
        }
    }

    public Iterator<Point> positions() {
        List<Point> liste = new LinkedList<Point>();
        if (positions != null) {
            for (int pos = 0; pos < positions.getLength(); pos++) {
                Element position = (Element) positions.item(pos);
                int x = Integer.parseInt(position.getAttribute("x"));
                int y = Integer.parseInt(position.getAttribute("y"));
                liste.add(new Point(x, y));
            }
        }
        return liste.iterator();
    }

    public boolean hasPosition() {
        boolean result = false;
        if (positions != null) {
            if (positions.getLength() > 0) {
                result = true;
            }
        }
        return result;
    }

    /**
         * @return a new Point of the new Dimension saved for the element
         */
    public Point getDimension() {
        Point result = new Point();
        Element dim = (Element) this.dimension.item(0);
        result.x = Integer.parseInt(dim.getAttribute("x"));
        result.y = Integer.parseInt(dim.getAttribute("y"));
        return result;
    }

    /**
         * @return true if there is a dimension saved for that element. False otherwise
         */
    public boolean hasDimension() {
        boolean result = false;
        if (dimension != null) {
            if (dimension.getLength() > 0) {
                result = true;
            }
        }
        return result;
    }

    /**
      * Returns the x offset of the element. 0 if not set
      * @return the X offset of the element
             */
    public int offsetX() {
        int result = 0;
        if (offSet != null) {
            result = Integer.parseInt(offSet.getAttribute("x"));
        }
        return result;
    }

    /**
     * Returns the y offset of the element. 0 if not set
              * @return the Y offset of the element
                     */
    public int offsetY() {
        int result = 0;
        if (offSet != null) {
            result = Integer.parseInt(offSet.getAttribute("y"));
        }
        return result;
    }

    /**
     * @return true if the element has the fill attribute set
     */
    public boolean hasFill() {
        if (fill == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns the fill color set for this element
     * <p>
     * This method requires that hasFill() is true.
     * </p>
     * @return the fill color set
     */
    public Color getFill() {
        String textColor = fill.getAttribute("color");
        Color result = GraphicConverter.instance().parseCSS2Color(textColor);
        return result;
    }

    /**
     * @return true if the line attribute has been set
     */
    public boolean hasLine() {
        if (line == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * The line attribute needs to be set.
     * <p>
     * This method requires that hasLine() is true.
     * </p>
     * @return true if the color attribute has been set for the line attribute
     */
    public boolean hasLineColor() {
        if (line != null && !line.getAttribute("color").equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the line color set for this element
     * <p>
     * This method requires that hasLineColor() is true.
     * </p>
     * @return the line color set
     */
    public Color getLineColor() {
        String textColor = line.getAttribute("color");
        Color result = GraphicConverter.instance().parseCSS2Color(textColor);
        return result;
    }

    /**
     * The line attribute needs to be set.
     * <p>
     * This method requires that hasLine() is true.
     * </p>
     * @return true if the style attribute has been set for the line attribute
     */
    public boolean hasLineStyle() {
        if (line != null && !line.getAttribute("style").equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the line style set for this element
     * <p>
     * This method requires that hasLineStyle() is true.
     * </p>
     * @return the line styleS
     */
    public String getLineStyle() {
        String style = line.getAttribute("style");
        return GraphicConverter.instance().parsePNMLLineStyle(style);
    }
}