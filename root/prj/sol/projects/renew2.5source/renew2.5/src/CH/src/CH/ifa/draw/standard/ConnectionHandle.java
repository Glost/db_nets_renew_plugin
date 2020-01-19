/*
 * @(#)ConnectionHandle.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.util.Geom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


/**
 * A handle to connect figures.
 * The connection object to be created is specified by a prototype.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld029.htm>Prototype</a></b><br>
 * ConnectionHandle creates the connection by cloning a prototype.
 * <hr>
 *
 * @see ConnectionFigure
 * @see Object#clone
 */
public class ConnectionHandle extends LocatorHandle {

    /**
     * the currently created connection
     */
    private ConnectionFigure fConnection;

    /**
     * the prototype of the connection to be created
     */
    private ConnectionFigure fPrototype;

    /**
     * the current target
     */
    private Figure fTarget = null;

    /**
     * Constructs a handle with the given owner, locator, and connection prototype
     */
    public ConnectionHandle(Figure owner, Locator l, ConnectionFigure prototype) {
        super(owner, l);
        fPrototype = prototype;
    }

    /**
     * Creates the connection
     */
    public void invokeStart(int x, int y, DrawingView view) {
        super.invokeStart(x, y, view);
        fConnection = createConnection();
        Point p = locate();
        fConnection.startPoint(p.x, p.y);
        fConnection.endPoint(p.x, p.y);
        view.drawing().add(fConnection);
    }

    protected ConnectionFigure getConnection() {
        return fConnection;
    }

    /**
     * Tracks the connection.
     */
    public void invokeStep(MouseEvent e, int x, int y, int anchorX,
                           int anchorY, DrawingView view) {
        Point p = new Point(x, y);
        Figure f = findConnectableFigure(x, y, view.drawing());

        // track the figure containing the mouse
        if (f != fTarget) {
            if (fTarget != null) {
                fTarget.connectorVisibility(false);
            }
            fTarget = f;
            if (fTarget != null) {
                fTarget.connectorVisibility(true);
            }
        }

        Connector target = findConnectionTarget(p.x, p.y, view.drawing());
        if (target != null) {
            p = Geom.center(target.displayBox());
        } else if (e.isControlDown()) {
            p = snap(p.x, p.y);
        }
        fConnection.endPoint(p.x, p.y);
    }

    protected Point snap(int x, int y) {
        return new Point(x, y);
    }

    /**
     * Connects the figures if the mouse is released over another
     * figure.
     */
    public void invokeEnd(int x, int y, int anchorX, int anchorY,
                          DrawingView view) {
        Connector target = findConnectionTarget(x, y, view.drawing());
        if (target != null) {
            fConnection.connectStart(startConnector());
            fConnection.connectEnd(target);
            fConnection.updateConnection();
            view.clearSelection();
            view.addToSelection(target.owner());
        } else {
            view.drawing().remove(fConnection);
            noChangesMade();
        }
        fConnection = null;
        if (fTarget != null) {
            fTarget.connectorVisibility(false);
            fTarget = null;
        }
        super.invokeEnd(x, y, anchorX, anchorY, view);
    }

    private Connector startConnector() {
        Point p = locate();
        return owner().connectorAt(p.x, p.y);
    }

    /**
     * Creates the ConnectionFigure. By default the figure prototype is
     * cloned.
     */
    protected ConnectionFigure createConnection() {
        // OK: I do not know why, but the following cast to Figure
        // avoids a strange error message under Java 1.1:
        //   Reference to clone is ambiguous.
        //   It is defined in java.lang.Object clone() and
        //   java.lang.Object clone().
        return (ConnectionFigure) ((Figure) fPrototype).clone();
    }

    /**
     * Finds a connection end figure.
     */
    protected Connector findConnectionTarget(int x, int y, Drawing drawing) {
        Figure target = findConnectableFigure(x, y, drawing);
        if ((target != null) && target.canConnect()
                    && !target.includes(owner())
                    && fConnection.canConnect(owner(), target)) {
            return findConnector(x, y, target);
        }
        return null;
    }

    protected Figure findConnectableFigure(int x, int y, Drawing drawing) {
        FigureEnumeration k = drawing.figuresReverse();
        while (k.hasMoreElements()) {
            Figure figure = k.nextFigure();
            if (!figure.includes(fConnection) && figure.canConnect()) {
                if (figure.containsPoint(x, y)) {
                    return figure;
                }
            }
        }
        return null;
    }

    protected Connector findConnector(int x, int y, Figure f) {
        return f.connectorAt(x, y);
    }

    /**
     * Draws the connection handle, by default the outline of a
     * blue circle.
     */
    public void draw(Graphics g) {
        Rectangle r = displayBox();
        g.setColor(Color.blue);
        g.drawOval(r.x, r.y, r.width, r.height);
    }
}