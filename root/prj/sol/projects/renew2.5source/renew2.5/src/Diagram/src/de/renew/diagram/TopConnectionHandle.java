/*
 * @(#)MessageConnectionHandle.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A handle to connect Tasks with Tasks and Cond /split
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class TopConnectionHandle extends ConnectionHandle {

    /**
     * Constructs a handle for the given owner
     */
    public TopConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(), new LifeLineConnection());
    }

    protected TaskFigure createFigure() {
        TaskFigure figure = new TaskFigure();
        Dimension d = TaskFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));
        figure.setFillColor(java.awt.Color.white);
        return figure;
    }

    protected TaskFigure createPFigure() {
        TaskFigure figure = new TaskFigure();
        Dimension d = TaskFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));
        figure.setFillColor(java.awt.Color.white);
        return figure;
    }

    /**
     * OVERRIDDEN IN SUBCLASS
     * @param figure
     * @param view
     *
     * @author Eva Mueller
     * @date Dec 3, 2010
     * @version 0.1
     */
    protected void addInscriptions(Figure figure, DrawingView view) {
        // Overridden in subclasses.
    }

    /**
     * Connects the figures if the mouse is released over another
     * Transition/Place figure;
     * otherwise, the respective figure is created!
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target == null) {
            Figure owner = owner();
            if (owner.findFigureInside(x, y) == null) {
                Figure endFigure;
                if (owner instanceof TaskFigure) {
                    // logger.debug("Creating Transition!");
                    endFigure = createFigure();
                } else {
                    // logger.debug("Creating Place!");
                    endFigure = createPFigure();
                }
                Rectangle rect = endFigure.displayBox();
                int w2 = rect.width / 2;
                int h2 = rect.height / 2;
                endFigure.displayBox(new Point(x - w2, y - h2),
                                     new Point(x - w2 + rect.width,
                                               y - h2 + rect.height));

                view.add(endFigure);

                addInscriptions(endFigure, view);
            }
        }
        super.invokeEnd(x, y, anchorX, anchorY, view);
    }
}