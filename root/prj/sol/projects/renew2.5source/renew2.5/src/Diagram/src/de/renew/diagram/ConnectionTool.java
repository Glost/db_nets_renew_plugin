/*
 * @(#)ConnectionTool.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.UndoableTool;

import CH.ifa.draw.util.Geom;

import de.renew.diagram.drawing.DiagramDrawing;

import java.awt.Point;
import java.awt.event.MouseEvent;

import java.util.Enumeration;


// code copied from CH.ifa.draw.standard.


/**
 * A tool that can be used to connect figures, to split
 * connections, and to join two segments of a connection.
 * ConnectionTools turns the visibility of the Connectors
 * on when it enters a figure.
 * The connection object to be created is specified by a prototype.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld029.htm>Prototype</a></b><br>
 * ConnectionTools creates the connection by cloning a prototype.
 * <hr>
 *
 * @see ConnectionFigure
 * @see Object#clone
 */
public class ConnectionTool extends UndoableTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConnectionTool.class);

    /**
     * the anchor point of the interaction
     */
    private Connector fStartConnector;
    private Connector fEndConnector;
    private Connector fConnectorTarget = null;
    private Figure fTarget = null;

    /**
     * the currently created figure
     */
    private ConnectionFigure fConnection = null;

    /**
     * the currently manipulated connection point
     */
    private int fSplitPoint;

    /**
     * the currently edited connection
     */
    private ConnectionFigure fEditedConnection = null;

    /**
     * the prototypical figure that is used to create new
     * connections.
     */
    private ConnectionFigure fPrototype;

    public ConnectionTool(DrawingEditor editor, ConnectionFigure prototype) {
        super(editor);
        fPrototype = prototype;
    }

    /**
     * Handles mouse move events in the drawing view.
     */
    public void mouseMove(MouseEvent e, int x, int y) {
        trackConnectors(e, x, y);
    }

    /**
     * Manipulates connections in a context dependent way. If the
     * mouse down hits a figure start a new connection. If the mousedown
     * hits a connection split a segment or join two segments.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        int ex = e.getX();
        int ey = e.getY();
        ConnectionFigure connection = findConnection(ex, ey, drawing());
        if (connection != null) {
            if (!connection.joinSegments(ex, ey)) {
                fSplitPoint = connection.splitSegment(ex, ey);
                fEditedConnection = connection;
            } else {
                fEditedConnection = null;
            }
            changesMade();
        } else {
            fTarget = findConnectionStart(ex, ey, drawing());
            //logger.debug("Target: "+fTarget);
            if (fTarget != null) {
                fStartConnector = findConnector(ex, ey, fTarget);
                //logger.debug("Startconnector: "+fStartConnector);
                if (fStartConnector != null) {
                    Point p = new Point(ex, ey);
                    fConnection = createConnection();


                    //logger.debug("Connection: "+fConnection);
                    fConnection.startPoint(p.x, ey);
                    fConnection.endPoint(p.x, ey);
                    view().add(fConnection);
                    changesMade();
                }
            }
        }
    }

    /**
     * Adjust the created connection or split segment.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        Point p = new Point(e.getX(), e.getY());
        if (fConnection != null) {
            trackConnectors(e, x, y);
            if (fConnectorTarget != null) {
                p = Geom.center(fConnectorTarget.displayBox());
            }
            fConnection.endPoint(p.x, p.y);
        } else if (fEditedConnection != null) {
            Point pp = new Point(x, y);
            fEditedConnection.setPointAt(pp, fSplitPoint);
        }
    }

    /**
     * Connects the figures if the mouse is released over another
     * figure.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        Figure c = null;
        if (fStartConnector != null) {
            c = findTarget(e.getX(), e.getY(), drawing());
        }

        if (c != null) {
            fEndConnector = findConnector(e.getX(), e.getY(), c);
            if (fEndConnector != null) {
                fConnection.connectStart(fStartConnector);
                fConnection.connectEnd(fEndConnector);
                fConnection.updateConnection();
            }
        } else if (fConnection != null) {
            view().remove(fConnection);
            noChangesMade();
        }

        fConnection = null;
        fStartConnector = fEndConnector = null;
        editor().toolDone();
    }

    public void deactivate() {
        super.deactivate();
        if (fTarget != null) {
            fTarget.connectorVisibility(false);
        }
    }

    /**
     * Creates the ConnectionFigure. By default the figure prototype is
     * cloned.
     */
    protected ConnectionFigure createConnection() {
        return (ConnectionFigure) ((Figure) fPrototype).clone();
    }

    /**
     * Finds a connectable figure target.
     */
    protected Figure findSource(int x, int y, Drawing drawing) {
        return findConnectableFigure(x, y, drawing);
    }

    /**
     * Finds a connectable figure target.
     */
    protected Figure findTarget(int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(x, y, drawing);
        Figure start = fStartConnector.owner();

        if (target != null && fConnection != null && target.canConnect()
                    && !target.includes(start)
                    && fConnection.canConnect(start, target)) {
            return target;
        }
        return null;
    }

    /**
     * Finds an existing connection figure.
     */
    protected ConnectionFigure findConnection(int x, int y, Drawing drawing) {
        Enumeration<Figure> k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextElement();
            figure = figure.findFigureInside(x, y);
            if (figure != null && (figure instanceof ConnectionFigure)) {
                return (ConnectionFigure) figure;
            }
        }
        return null;
    }

    /**
     * Gets the currently created figure
     */
    protected ConnectionFigure createdFigure() {
        return fConnection;
    }

    protected void trackConnectors(MouseEvent e, int x, int y) {
        if (fConnection == null) {
            //logger.debug("fConnection is null!!!");
            return;
        }

        Figure c = null;

        if (fStartConnector == null) {
            c = findSource(x, y, drawing());
        } else {
            c = findTarget(x, y, drawing());
        }

        // track the figure containing the mouse
        if (c != fTarget) {
            if (fTarget != null) {
                fTarget.connectorVisibility(false);
            }
            fTarget = c;
            if (fTarget != null) {
                fTarget.connectorVisibility(true);
            }
        }

        Connector cc = null;
        if (c != null) {
            cc = findConnector(e.getX(), e.getY(), c);
        }
        if (cc != fConnectorTarget) {
            fConnectorTarget = cc;
        }

        view().checkDamage();
    }

    protected Connector findConnector(int x, int y, Figure f) {
        return f.connectorAt(x, y);
    }

    /**
     * Finds a connection start figure.
     */
    protected Figure findConnectionStart(int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(x, y, drawing);
        if ((target != null) && target.canConnect()) {
            return target;
        }
        return null;
    }

    private Figure findConnectableFigure(int x, int y, Drawing drawing) {
        FigureEnumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextFigure();
            if ((fConnection == null || !figure.includes(fConnection))
                        && figure.canConnect()) {
                if (figure.containsPoint(x, y)) {
                    return figure;
                }
            }
        }
        return null;
    }

    protected Connector getStartConnector() {
        return fStartConnector;
    }

    protected Connector getEndConnector() {
        return fEndConnector;
    }

    protected Connector getTarget() {
        return fConnectorTarget;
    }

    public ConnectionFigure getFConnection() {
        return fConnection;
    }

    public Connector getFEndConnector() {
        return fEndConnector;
    }

    public Connector getFStartConnector() {
        return fStartConnector;
    }

    public void setFEndConnector(Connector connector) {
        fEndConnector = connector;
    }

    public void setFConnection(ConnectionFigure figure) {
        fConnection = figure;
    }

    public void setFStartConnector(Connector connector) {
        fStartConnector = connector;
    }

    public boolean generatePeerInstantaneously(DrawingView view) {
        Drawing drawing = view.drawing();
        if (!(drawing instanceof DiagramDrawing)) {
            return false;
        }

        return ((DiagramDrawing) drawing).generatePeerInstantaneously;

    }
}