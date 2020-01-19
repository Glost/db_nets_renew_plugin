package de.renew.gui.fs;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.ConnectionHandle;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.CPNTextFigure;

import java.awt.Point;


public class AssociationHandle extends ConnectionHandle {
    private int line;
    private String name;
    private String type;
    private ConceptFigure owner;
    private boolean isCollection;

    public AssociationHandle(ConceptFigure owner, int line, String name,
                             String type, boolean isCollection,
                             Locator locator, ConnectionFigure prototype) {
        super(owner, locator, prototype);
        this.owner = owner;
        this.line = line;
        this.name = name;
        this.type = type;
        this.isCollection = isCollection;
    }

    protected ConnectionFigure createConnection() {
        return new AssocConnection();
    }

    /**
     * Connects the figures if the mouse is released over another
     * ConceptFigure;
     * otherwise, the respective figure is created!
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        // Does this click aim at the source?
        if (findConnectableFigure(x, y, view.drawing()) != owner) {
            Connector target = findConnectionTarget(x, y, view.drawing());
            if (target == null) {
                if (owner.findFigureInside(x, y) == null) {
                    ConceptFigure endFigure = new ConceptFigure();

                    endFigure.moveBy(x, y);
                    endFigure.setText(type);

                    view.add(endFigure);
                }
            }

            String[] lines = owner.getLines();
            StringBuffer newText = new StringBuffer();
            for (int i = 0; i < lines.length; i++) {
                if (i != line) {
                    if (i > 0) {
                        newText.append('\n');
                    }
                    newText.append(lines[i]);
                }
            }
            owner.setText(newText.toString());

            CPNTextFigure nameFigure = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
            nameFigure.setAttribute("FillColor", ColorMap.BACKGROUND);
            nameFigure.setText(name);
            nameFigure.setParent((AssocConnection) getConnection());
            view.drawing().add(nameFigure);

            CPNTextFigure starFigure = null;
            if (isCollection) {
                starFigure = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
                starFigure.setAttribute("FillColor", ColorMap.NONE);
                starFigure.setText("*");
                starFigure.setParent((AssocConnection) getConnection());
                view.drawing().add(starFigure);
                Point p1 = starFigure.center();
                starFigure.moveBy((x - p1.x) / 2, (y - p1.y) / 2);
            }
        }

        super.invokeEnd(x, y, anchorX, anchorY, view);

        view.selectionInvalidateHandles();
    }
}