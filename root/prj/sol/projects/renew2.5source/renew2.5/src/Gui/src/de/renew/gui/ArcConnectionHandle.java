/*
 * @(#)ArcConnectionHandle.java 5.1
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * A handle to connect Places to Transitions and vice versa.
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class ArcConnectionHandle extends ConnectionHandle {

    /**
     * Constructs a handle for the given owner
     */
    public ArcConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(), ArcConnection.NormalArc);
    }

    protected PlaceFigure createPlaceFigure() {
        PlaceFigure figure = new PlaceFigure();
        Dimension d = PlaceFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));

        return figure;
    }

    protected TransitionFigure createTransitionFigure() {
        TransitionFigure figure = new TransitionFigure();
        Dimension d = TransitionFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));
        return figure;
    }

    /**
     * OVERRIDEN IN SUBCLASS
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
    public void invokeEnd(MouseEvent e, int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target == null) {
            Figure owner = owner();
            if (e.isControlDown()) {
                Point snap = snap(x, y);
                x = snap.x;
                y = snap.y;
            }
            if (owner.findFigureInside(x, y) == null) {
                Figure endFigure;

                if (owner instanceof PlaceFigure) {
                    //logger.debug("Creating Transition!");
                    endFigure = createTransitionFigure();
                } else {
                    //logger.debug("Creating Place!");
                    endFigure = createPlaceFigure();
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

    @Override
    protected Point snap(int x, int y) {
        Point ownerCenter = owner().center();
        Point targetPoint = new Point(x, y);

        // a vector from ownerCenter to targetPoint 
        Point arcVector = new Point(targetPoint.x - ownerCenter.x,
                                    targetPoint.y - ownerCenter.y);

        // polar coordinates
        double r = arcVector.distance(0, 0); // length
        double theta = Math.atan2(arcVector.y, arcVector.x); // angle
        theta = (theta + (Math.PI * 2)) % (Math.PI * 2); // no negative angles  

        int segmentCount = 8;
        double segmentSize = (2 * Math.PI) / segmentCount;

        // rotate to the middle of the segment that contains the arc  
        double newTheta = (theta + segmentSize / 2) % (2 * Math.PI);
        newTheta = newTheta - (newTheta % segmentSize);

        // Euclidean coordinates
        int snapX = (int) (ownerCenter.x + r * Math.cos(newTheta));
        int snapY = (int) (ownerCenter.y + r * Math.sin(newTheta));
        Point snappedPoint = new Point(snapX, snapY);

        return snappedPoint;
    }
}