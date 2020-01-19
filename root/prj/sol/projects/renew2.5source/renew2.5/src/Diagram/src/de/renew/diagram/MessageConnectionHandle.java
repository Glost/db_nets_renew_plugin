/*
 * @(#)MessageConnectionHandle.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
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

import java.util.Iterator;
import java.util.Vector;


/**
 * @author Lawrence Cabac
 *
 * A handle to connect Tasks with Tasks and Cond / split
 *
 * @see CH.ifa.draw.framework.ConnectionFigure
 */
public class MessageConnectionHandle extends ConnectionHandle {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MessageConnectionHandle.class);

    /**
     * Constructs a handle for the given owner
     */
    public MessageConnectionHandle(Figure owner) {
        super(owner, RelativeLocator.center(), new MessageConnection());
    }

    public MessageConnectionHandle(Figure owner, RelativeLocator loc,
                                   ConnectionFigure prototype) {
        super(owner, loc, prototype);
    }

    protected TaskFigure createFigure() {
        TaskFigure figure = new TaskFigure();
        Dimension d = TaskFigure.defaultDimension();
        figure.displayBox(new Point(0, 0), new Point(d.width, d.height));
        figure.setFillColor(java.awt.Color.white);
        return figure;
    }

    /**
     * OVERRIDDEN in SUBCLASS
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
     *
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Figure figure = null;
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target == null) {
            Figure owner = owner();
            if (owner.findFigureInside(x, y) == null) {
                Figure endFigure;
                endFigure = createFigure();
                Rectangle rect = endFigure.displayBox();

                endFigure.displayBox(new Point(x - rect.width / 2, y),
                                     new Point(x + rect.width / 2,
                                               y + rect.height));
                figure = view.add(endFigure);
                addInscriptions(endFigure, view);
            }
        }
        if (figure != null) {
            y += figure.displayBox().height / 2;
        }
        super.invokeEnd(x, y, anchorX, anchorY, view);

        if (figure != null) {
            // check if there is a dParent for this TaskFigure
            Figure parentFigure = findParent(figure, view);

            // if there is no parent to be found  remove the figure and return
            if (parentFigure == null) {
                view.remove(figure);
                return;
            }


            LifeLineConnection lifeLine = new LifeLineConnection(1);
            Figure fig = figure;
            Vector<Figure> parents = ((TailFigure) fig).getDParents();
            Iterator<Figure> pit = parents.iterator();
            while (pit.hasNext()) { // there should be only one dParent for a TaskFigure
                Figure parent = pit.next();
                Connector startConnector = determineStartConnector(parent, x, y);
                Connector endConnector = fig.connectorAt(fig.displayBox().x + 2,
                                                         fig.displayBox().y + 2);
                lifeLine.startPoint(parent.center());
                lifeLine.endPoint(fig.center());
                lifeLine.connectStart(startConnector);
                lifeLine.connectEnd(endConnector);
                view.add(lifeLine);
                snapToFit(figure, parent, startConnector);
                //((TailFigure)figure).addDParentConnector(startConnector);
                logger.debug("Lebenslinie " + figure + " " + lifeLine + " "
                             + startConnector);

                lifeLine.updateConnection();

            }
        }
        if (target != null) {
            figure = target.owner();
        }

        DiagramFigure start = (DiagramFigure) owner();
        if (start instanceof TaskFigure) {
            TaskFigure task = (TaskFigure) start;
            logger.debug("start (Place) " + task);
            task.addPeerName("out");
        }
        DiagramFigure end = (DiagramFigure) figure;
        if (end instanceof TaskFigure) {
            TaskFigure task = (TaskFigure) end;
            task.addPeerName("in");
        }
    }

    /**
      * Snap the position of a DiagramFigure so that the LifeLine is vertical.
      */
    protected void snapToFit(Figure figure, Figure parent, Connector connector) {
        int offset = 0;
        if (!(parent instanceof RoleDescriptorFigure)) {
            if (parent instanceof TaskFigure) {
                offset = parent.displayBox().width / 2;
            } else if (parent instanceof VSplitFigure) {
                if (connector instanceof RightConnector) {
                    offset = parent.displayBox().width;
                }
            }
            figure.moveBy(parent.displayBox().x + offset
                          - figure.displayBox().x
                          - figure.displayBox().width / 2, 0);
        }
    }

    /**
     * Determines the connector for a given parent figure and a given location
     * which is the mouse click position.
     * @param parent the diagram parent figure of a figure
     * @param x the position of the mouse click
     * @param y the position of the mouse click
     */
    protected Connector determineStartConnector(Figure parent, int x, int y) {
        Connector connector = null;
        if (parent instanceof VSplitFigure) {
            if (x < parent.center().x) {
                // left connector of the  vertical split figure
                connector = parent.connectorAt(parent.displayBox().x,
                                               parent.center().y);
            } else {
                //  right connector of the  vertical split figure
                connector = parent.connectorAt(parent.displayBox().x
                                               + parent.displayBox().width,
                                               parent.center().y);
            }
        } else {
            // find the connector at the bottom
            connector = parent.connectorAt(parent.center().x,
                                           parent.displayBox().y
                                           + parent.displayBox().height);
        }

        return connector;
    }

    private Figure findParent(Figure figure, DrawingView view) {
        logger.debug("1Figure  " + figure);
        TailFigure fig = (TailFigure) figure;
        Locator loc = getLocator(view);
        if (loc != null) {
            Figure dParent = loc.getElementAtPosition(fig.center().x,
                                                      fig.displayBox().y);
            logger.debug("2Figure Parent is " + dParent);
            if (dParent != null) {
                //loc.add(fig);
                RoleDescriptorFigure head = ((TailFigure) dParent).getDHead();
                if (head != null) {
                    head.addToTail(figure);
                    fig.setDHead(head);
                }
                TailFigure parent = (TailFigure) dParent;
                fig.addDParent(parent);
                parent.addDChild(fig);

                if (dParent instanceof RoleDescriptorFigure) {
                    fig.addPeerName("start");
                }

                // logger.debug("DiagramDrawing second "+ generatePeerInstantaneously);
                if (generatePeerInstantaneously(view)) {
                    (fig).generatePeers();
                }
                logger.debug("Figure added as Parent " + dParent);
                return dParent;
            }
        }
        return null;
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