package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.PlaceFigure;
import de.renew.gui.VirtualPlaceFigure;


/**
 * @author volker
 * erstellt am Apr 30, 2004
 *
 */
public class VirtualplaceParser extends NodeParser {
    private Drawing _net;
    private int parentId;

    public VirtualplaceParser(Element node, Drawing net) {
        super(node);
        setNet(net);
    }

    public FigureWithID createObject() {
        FigureEnumeration figures = net().figures();
        FigureWithID figure = null;
        while (figures.hasMoreElements()) {
            Figure element = figures.nextFigure();
            if (element instanceof FigureWithID) {
                figure = (FigureWithID) element;
                if (figure.getID() == parentId) {
                    break;
                }
            }
        }
        VirtualPlaceFigure result = new VirtualPlaceFigure((PlaceFigure) figure);
        return result;
    }

    protected void parseFigure() {
        parentId = Integer.parseInt(getElement()
                                        .getAttribute("semanticPlaceFigure"));
    }

    protected Drawing net() {
        return _net;
    }

    protected void setNet(Drawing net) {
        _net = net;
    }
}