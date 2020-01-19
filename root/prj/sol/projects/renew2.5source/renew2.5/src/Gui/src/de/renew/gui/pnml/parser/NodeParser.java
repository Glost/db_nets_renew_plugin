package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;

import java.awt.Color;
import java.awt.Point;


/**
 * parse and create Nodes
 */
public abstract class NodeParser extends ObjectParser {
    public NodeParser(Element node) {
        super(node);
    }

    protected Point getLocation() {
        Point result = new Point();
        if (getGraphic().hasPosition()) {
            result = getGraphic().positions().next();
        }
        return result;
    }

    protected Point getDimension() {
        Point result = null;
        if (getGraphic().hasDimension()) {
            result = getGraphic().getDimension();
        }
        return result;
    }

    protected Color getFillColor() {
        Color result = null;
        if (getGraphic().hasFill()) {
            result = getGraphic().getFill();
        }
        return result;
    }

    protected Color getLineColor() {
        Color result = null;
        if (getGraphic().hasLineColor()) {
            result = getGraphic().getLineColor();
        }
        return result;
    }

    protected void setFigureDimension(Point p1, Point p2) {
        figure().displayBox(p1, p2);
    }

    protected void initGraphic() {
        Point topLeft = new Point(0, 0);
        Point bottomRight;
        if (getDimension() != null) {
            bottomRight = getDimension();
        } else {
            bottomRight = new Point(20, 20);
        }
        if (getFillColor() != null) {
            figure().setAttribute("FillColor", getFillColor());
        }
        if (getLineColor() != null) {
            figure().setAttribute("FrameColor", getLineColor());
        }
        setFigureDimension(topLeft, bottomRight);
        moveObject(getLocation().x - (bottomRight.x / 2),
                   getLocation().y - (bottomRight.y / 2));

    }
}