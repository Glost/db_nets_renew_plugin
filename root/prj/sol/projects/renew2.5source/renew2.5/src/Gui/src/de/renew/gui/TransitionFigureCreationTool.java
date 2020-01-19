/*
 * @(#)TransitionFigureCreationTool.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.CreationTool;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A more efficient version of the generic creation
 * tool that is not based on cloning.
 */
public class TransitionFigureCreationTool extends CreationTool {
    public TransitionFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    /**
     * Creates a new TransitionFigure.
     */
    protected Figure createFigure() {
        return new TransitionFigure();
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();
        if (created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            Dimension d = TransitionFigure.defaultDimension();
            int w2 = d.width / 2;
            int h2 = d.height / 2;
            created.displayBox(new Point(loc.x - w2, loc.y - h2),
                               new Point(loc.x - w2 + d.width,
                                         loc.y - h2 + d.height));
        }
        super.mouseUp(e, x, y);
    }
}