/*
 * @(#)MessageConnectionHandle.java 5.1
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * @author Lawrence Cabac
 *
 * A handle to connect Tasks with Tasks and Cond / split
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class FAConnectionHandle extends ConnectionHandle {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAConnectionHandle.class);

    /**
     * Constructs a handle for the given owner.
     * <p>
     * The handle is located in the center and has an
     * FAArcConnection as a prototype.
     * </p>
     */
    public FAConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(),
              new FAArcConnection(null, new ArrowTip(),
                                  AttributeFigure.LINE_STYLE_NORMAL));
    }

    /**
     * Constructs a handle for the given owner
     */
    public FAConnectionHandle(Figure owner, Locator rl, LineConnection lc) {
        super(owner, rl, lc);
    }

    protected void addInscriptions(Figure figure, DrawingView view) {
        // Overridden in subclasses.
    }

    protected FAStateFigure createFigure() {
        FAStateFigure figure = new FAStateFigure();
        Dimension d = FAStateFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));
        figure.setFillColor(java.awt.Color.white);
        logger.debug("createFigure() called and created " + figure);
        return figure;
    }

    /**
     * Connects the figures if the mouse is released over another
     * Transition/Place figure; otherwise, the respective figure is created!
     *
     */
    @Override
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target == null) {
            Figure owner = owner();
            if (owner.findFigureInside(x, y) == null) {
                Figure endFigure;
                endFigure = createFigure();
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