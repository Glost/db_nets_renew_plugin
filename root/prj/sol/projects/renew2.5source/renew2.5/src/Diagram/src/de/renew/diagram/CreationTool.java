/*
 * @(#)CreationTool.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.HJDError;

import CH.ifa.draw.standard.UndoableTool;

import de.renew.diagram.drawing.DiagramDrawing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * This code is copied from CH.ifa.draw.standard and extended
 * to access functionality that was formerly private.
 * A tool to create new figures. The figure to be
 * created is specified by a prototype.
 *
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld029.htm>Prototype</a></b><br>
 * CreationTool creates new figures by cloning a prototype.
 * <hr>
 * @see Figure
 * @see Object#clone
 */
public class CreationTool extends UndoableTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CreationTool.class);

    /**
     * The figure that is returned by the drawing
     * i.e. the figure that is drawn, if any
     * otherwise null.
     */
    private Figure fDrawnFigure = null;

    /**
    * the anchor point of the interaction
    */
    private Point fAnchorPoint;

    /**
     * the currently created figure
     */
    private Figure fCreatedFigure;

    /**
     * the prototypical figure that is used to create new figures.
     */
    private Figure fPrototype;

    /**
     * Initializes a CreationTool with the given prototype.
     */
    public CreationTool(DrawingEditor editor, Figure prototype) {
        super(editor);
        fPrototype = prototype;
    }

    /**
     * Constructs a CreationTool without a prototype.
     * This is for subclassers overriding createFigure.
     */
    protected CreationTool(DrawingEditor editor) {
        super(editor);
        fPrototype = null;
    }

    protected Figure getPrototype() {
        return fPrototype;
    }

    /**
     * Sets the cross hair cursor.
     */
    public void activate() {
        super.activate();
        view().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        fAnchorPoint = null;
        fCreatedFigure = null;
    }

    /**
     * Creates a new figure by cloning the prototype.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        fAnchorPoint = new Point(x, y);
        fCreatedFigure = createFigure();
        fCreatedFigure.displayBox(fAnchorPoint, fAnchorPoint);
        fDrawnFigure = view().add(fCreatedFigure);
        changesMade();
    }

    /**
     * Creates a new figure by cloning the prototype.
     */
    protected Figure createFigure() {
        if (fPrototype == null) {
            throw new HJDError("No prototype defined");
        }
        return (Figure) fPrototype.clone();
    }

    /**
     * Adjusts the extent of the created figure
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        fCreatedFigure.displayBox(fAnchorPoint, new Point(x, y));
    }

    /**
     * Checks if the created figure is empty. If it is, the figure
     * is removed from the drawing.
     * @see Figure#isEmpty
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        if (fCreatedFigure.isEmpty()) {
            drawing().remove(fCreatedFigure);
            noChangesMade();
        }
        fCreatedFigure = null;
        editor().toolDone();
    }

    /**
     * Gets the currently created figure
     */
    protected Figure createdFigure() {
        return fCreatedFigure;
    }

    public Figure getDrawnFigure() {
        return fDrawnFigure;
    }

    //NOTICEsignature
    protected Connector determineStartConnector(Figure parent, int x, int y) {
        Connector connector = null;
        if (parent instanceof VSplitFigure) {
            if (x < parent.center().x) {
                connector = parent.connectorAt(parent.displayBox().x,
                                               parent.center().y);
            } else {
                connector = parent.connectorAt(parent.displayBox().x
                                               + parent.displayBox().width,
                                               parent.center().y);
            }
        } else {
            connector = parent.connectorAt(parent.center().x,
                                           parent.displayBox().y
                                           + parent.displayBox().height);
        }

        return connector;
    }

    /**
     * snap the position for a DiagramFigure so that the LifeLine is vertical
     */
    protected void snapToFit(Figure figure, Figure parent, Connector connector) {
        if (!(parent instanceof RoleDescriptorFigure)) {
            int offset = 0;
            if (parent instanceof TaskFigure) {
                offset = parent.displayBox().width / 2;
            } else if (parent instanceof VSplitFigure) {
                if (connector instanceof RightConnector) {
                    offset = parent.displayBox().width;
                }
            } else if (parent instanceof VJoinFigure) {
                offset = parent.displayBox().width / 2;
            }

            figure.moveBy(parent.displayBox().x + offset
                          - figure.displayBox().x
                          - figure.displayBox().width / 2, 0);
        }
    }

    protected Figure findParent(Figure figure) {
        logger.debug("1Figure  " + figure);
        TailFigure fig = (TailFigure) figure;
        Locator loc = getLocator();
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

                //logger.debug("DiagramDrawing second "+ generatePeerInstantaneously);
                if (generatePeerInstantaneously()) {
                    (fig).generatePeers();
                }
                logger.debug("Figure added as Parent " + dParent);
                return dParent;
            }
        }
        return null;
    }

    protected Locator getLocator() {
        Drawing drawing = view().drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return null;
        }

        DiagramDrawing ddrawing = (DiagramDrawing) drawing;
        return ddrawing.getLocator();

    }

    protected boolean generatePeerInstantaneously() {
        Drawing drawing = view().drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return false;
        }

        return ((DiagramDrawing) drawing).generatePeerInstantaneously;

    }
}