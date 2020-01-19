package de.renew.gui.pnml.creator;

import de.renew.gui.NodeFigure;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;


public class NodeCreator extends ObjectCreator {
    public NodeCreator(String tag) {
        super(tag);
    }

    protected void doCreateGraphic(GraphicCreator creator) {
        Point center = getFigure().center();
        creator.addPosition(center.x, center.y);
        Rectangle dimension = ((NodeFigure) getFigure()).displayBox();
        int y = dimension.height;
        int x = dimension.width;
        creator.addDimension(x, y);
        Color c;
        c = (Color) ((NodeFigure) getFigure()).getAttribute("FillColor");
        creator.addFill(c);
        c = (Color) ((NodeFigure) getFigure()).getAttribute("FrameColor");
        creator.addLineColor(c);
        creator.addLine();
    }
}