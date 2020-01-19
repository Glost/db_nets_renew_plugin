/*
 * @(#)LifeLineConnectionHandle.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import de.renew.diagram.drawing.DiagramDrawing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;


/**
 * A handle to connect RoleDescriptorFigures with TaskFigures
 *  and maybe also later Tasks with Tasks and Cond /split
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class LifeLineConnectionHandle extends ConnectionHandle {

    /**
     * Constructs a handle for the given owner
     */
    public LifeLineConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(), new LifeLineConnection(1));
    }

    public LifeLineConnectionHandle(Figure owner,
                                    CH.ifa.draw.framework.Locator loc) {
        super(owner, loc, new LifeLineConnection(1));

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
     * OVERRIDDEN in SUBCLASS
     * @param figure UNUSED
     * @param view UNUSED
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
                endFigure.displayBox(new Point(x - w2, y),
                                     new Point(x - w2 + rect.width,
                                               y + rect.height));
                //fix
                Locator loc = getLocator(view);
                if (loc != null) {
                    TailFigure parent = (TailFigure) owner;
                    RoleDescriptorFigure head = parent.getDHead();
                    TailFigure fig = (TailFigure) endFigure;
                    head.addToTail(endFigure);
                    fig.setDHead(head);
                    fig.addDParent(parent);
                    parent.addDChild(fig);
                    //loc.add(fig);
                    if (parent instanceof RoleDescriptorFigure) {
                        fig.addPeerName("start");
                    }

                    // logger.debug("DiagramDrawing second "+ generatePeerInstantaneously);
                    if (generatePeerInstantaneously(view)) {
                        (fig).generatePeers();
                    }
                    view.add(endFigure);

                    addInscriptions(endFigure, view);
                    fig.addDParentConnector(target);
                }
            }
        }
        super.invokeEnd(x, y, anchorX, anchorY, view);

    }

    private Locator getLocator(DrawingView view) {
        Drawing drawing = view.drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return null;
        }

        DiagramDrawing ddrawing = (DiagramDrawing) drawing;
        return ddrawing.getLocator();

    }

    private boolean generatePeerInstantaneously(DrawingView view) {
        Drawing drawing = view.drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return false;
        }

        return ((DiagramDrawing) drawing).generatePeerInstantaneously;

    }
}