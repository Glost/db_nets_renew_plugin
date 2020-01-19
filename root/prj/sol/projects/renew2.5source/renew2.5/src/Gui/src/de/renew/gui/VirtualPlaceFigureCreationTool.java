/*
 * @(#)VirtualPlaceFigureCreationTool.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.CreationTool;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * A more efficient version of the generic Trans creation
 * tool that is not based on cloning.
 */
public class VirtualPlaceFigureCreationTool extends CreationTool {

    /**
     * the currently created figure
     */
    private VirtualPlaceFigure virtualPlace = null;

    public VirtualPlaceFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    /**
     * Creates a new VirtualPlaceFigure.
     */
    protected Figure createFigure() {
        return new VirtualPlaceFigure(null);
    }

    /**
     * Handles mouse move events in the drawing view.
     */
    public void mouseMove(MouseEvent e, int x, int y) {
        // highlight PlaceFigures?
    }

    /**
     * If the mouse down hits a PlaceFigure create a new VirtualPlaceFigure.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        int ex = e.getX();
        int ey = e.getY();
        Figure fTarget = drawing().findFigure(ex, ey);
        if (fTarget instanceof PlaceFigure) {
            PlaceFigure semanticPlaceFigure = (PlaceFigure) fTarget;

            // take the original place as semantic place figure if fTarget is a VirtualPlaceFigure     
            while (semanticPlaceFigure instanceof VirtualPlaceFigure) {
                semanticPlaceFigure = semanticPlaceFigure.getSemanticPlaceFigure();
            }

            virtualPlace = new VirtualPlaceFigure(semanticPlaceFigure);
            view().add(virtualPlace);

            FigureEnumeration children = ((PlaceFigure) fTarget).children();
            Rectangle r = fTarget.displayBox();

            // clone all name inscription from fTarget which may be a VirtualPlaceFigure
            while (children.hasMoreElements()) {
                Figure nextFigure = children.nextFigure();
                if (nextFigure instanceof CPNTextFigure) {
                    CPNTextFigure textFig = (CPNTextFigure) nextFigure;
                    if (textFig.getType() == CPNTextFigure.NAME) {
                        CPNTextFigure virtualPlaceName = new CPNTextFigure(CPNTextFigure.NAME);
                        virtualPlaceName.setText(textFig.getText());
                        view().add(virtualPlaceName);
                        virtualPlaceName.setParent(virtualPlace);
                        // calculate text offset
                        int xDistance = textFig.displayBox().x - r.x;
                        int yDistance = textFig.displayBox().y - r.y;

                        virtualPlaceName.moveBy(xDistance, yDistance);
                    }
                }
            }
            virtualPlace.displayBox(new Rectangle(r.x, r.y, r.width, r.height));
            changesMade();
        }
    }

    /**
     * Adjust the created connection or split segment.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        if (virtualPlace != null) {
            Rectangle r = virtualPlace.displayBox();
            virtualPlace.displayBox(new Rectangle(e.getX(), e.getY(), r.width,
                                                  r.height));
        }
    }

    /**
     * Drop new VirtualPlaceFigure.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        virtualPlace = null;
        editor().toolDone();
    }

    public void deactivate() {
        super.deactivate();
        virtualPlace = null;
    }
}