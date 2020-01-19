/*
 * @(#)LineConnection.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureChangeEvent;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.ChangeConnectionEndHandle;
import CH.ifa.draw.standard.ChangeConnectionStartHandle;
import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.MergedFigureEnumerator;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import java.awt.Point;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Vector;


/**
 * A LineConnection is a standard implementation of the
 * ConnectionFigure interface. The interface is implemented with PolyLineFigure.
 * @see ConnectionFigure
 */
public class LineConnection extends PolyLineFigure implements ConnectionFigure {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = 6883731614578414801L;

    /**
     * The connector where the start of the line is
     * connected to.
     * @serial
     **/
    protected Connector fStart = null;

    /**
     * The connector where the end of the line is
     * connected to.
     * @serial
     **/
    protected Connector fEnd = null;
    @SuppressWarnings("unused")
    private int lineConnectionSerializedDataVersion = 1;

    /**
     * Constructs a LineConnection. A connection figure has
     * an arrow decoration at the start and end.
     */
    public LineConnection() {
        super(4);
        setStartDecoration();
        setEndDecoration();
    }

    public LineConnection(LineDecoration start, LineDecoration end,
                          String lineStyle) {
        super(2);
        setStartDecoration(start);
        setEndDecoration(end);
        setLineStyle(lineStyle);
    }

    /**
     * Tests whether a figure can be a connection target.
     * ConnectionFigures cannot be connected and return false.
     */
    public boolean canConnect() {
        return false;
    }

    /**
     * Ensures that a connection is updated if the connection
     * was moved.
     */
    protected void basicMoveBy(int dx, int dy) {
        (fPoints.firstElement()).translate(-dx, -dy);
        (fPoints.lastElement()).translate(-dx, -dy);
        super.basicMoveBy(dx, dy);

        updateConnection(); // make sure that we are still connected
    }

    /**
     * Sets the start figure of the connection.
     */
    public void connectStart(Connector start) {
        fStart = start;
        startFigure().addFigureChangeListener(this);
        // fuw:
        if (fStart != null && fEnd != null) {
            handleConnect(startFigure(), endFigure());
        }

        if (guilogger.isTraceEnabled()) {
            guilogger.trace("LineConnection: set start of " + this + " to "
                            + this.startFigure());
        }
    }

    /**
     * Sets the end figure of the connection.
     */
    public void connectEnd(Connector end) {
        fEnd = end;
        endFigure().addFigureChangeListener(this);
        handleConnect(startFigure(), endFigure());

        if (guilogger.isTraceEnabled()) {
            guilogger.trace("LineConnection: set end of " + this + " to "
                            + this.endFigure());
        }
    }

    /**
     * Disconnects the start figure.
     */
    public void disconnectStart() {
        handleDisconnect(startFigure(), endFigure()); // fuw
        startFigure().removeFigureChangeListener(this);
        fStart = null;
    }

    /**
     * Disconnects the end figure.
     */
    public void disconnectEnd() {
        handleDisconnect(startFigure(), endFigure());
        endFigure().removeFigureChangeListener(this);
        fEnd = null;
    }

    /**
     * Tests whether a connection connects the same figures
     * as another ConnectionFigure.
     */
    public boolean connectsSame(ConnectionFigure other) {
        return other.start() == start() && other.end() == end();
    }

    /**
     * Handlesthe disconnection of a connection.
     * Override this method to handle this event.
     * @param start
     * @param end
     */
    protected void handleDisconnect(Figure start, Figure end) {
    }

    /**
     * Handles the connection of a connection.
     * Override this method to handle this event.
     * @param start
     * @param end
     */
    protected void handleConnect(Figure start, Figure end) {
    }

    /**
     * Gets the start figure of the connection.
     */
    public Figure startFigure() {
        if (start() != null) {
            return start().owner();
        }
        return null;
    }

    /**
     * Gets the end figure of the connection.
     */
    public Figure endFigure() {
        if (end() != null) {
            return end().owner();
        }
        return null;
    }

    /**
     * Gets the start figure of the connection.
     */
    public Connector start() {
        return fStart;
    }

    /**
     * Gets the end figure of the connection.
     */
    public Connector end() {
        return fEnd;
    }

    /**
     * Tests whether two figures can be connected.
     */
    public boolean canConnect(Figure start, Figure end) {
        return true;
    }

    /**
     * Sets the start point.
     */
    public void startPoint(int x, int y) {
        willChange();


        // We have to call super methods since the point-
        // modifying methods of this class would try to update
        // the connection and call this method again.
        if (fPoints.size() == 0) {
            super.addPoint(x, y);
        } else {
            super.setPointAt(new Point(x, y), 0);
        }

        // changed(); not needed since the called methods do it 
    }

    public void startPoint(Point p) {
        startPoint(p.x, p.y);
    }

    /**
     * Sets the end point.
     */
    public void endPoint(int x, int y) {
        willChange();


        // We have to call super methods because the point-
        // modifying methods of this class would try to update
        // the connection and call this method again.
        if (fPoints.size() < 2) {
            super.addPoint(x, y);
        } else {
            super.setPointAt(new Point(x, y), fPoints.size() - 1);
        }

        // changed(); not needed since the called methods do it 
    }

    public void endPoint(Point p) {
        endPoint(p.x, p.y);
    }

    /**
     * Gets the start point.
     */
    public Point startPoint() {
        Point p = fPoints.firstElement();
        return new Point(p.x, p.y);
    }

    /**
     * Gets the end point.
     */
    public Point endPoint() {
        Point p = fPoints.lastElement();
        return new Point(p.x, p.y);
    }

    /**
     * Gets the handles of the figure. It returns the normal
     * PolyLineHandles but adds ChangeConnectionHandles at the
     * start and end.
     */
    public Vector<Handle> handles() {
        int n = fPoints.size();
        Vector<Handle> handles = new Vector<Handle>(2 * n);
        handles.addElement(new ChangeConnectionStartHandle(this));
        addIntermediateHandles(handles);
        handles.addElement(new ChangeConnectionEndHandle(this));
        return handles;
    }

    /**
     * Sets the point and updates the connection.
     */
    public void setPointAt(Point p, int i) {
        super.setPointAt(p, i);
        layoutConnection();
    }

    /**
     * Inserts the point and updates the connection.
     */
    public void insertPointAt(Point p, int i) {
        super.insertPointAt(p, i);
        layoutConnection();
    }

    /**
     * Removes the point and updates the connection.
     */
    public void removePointAt(int i) {
        super.removePointAt(i);
        layoutConnection();
    }

    /**
     * Updates the connection.
     */
    public void updateConnection() {
        if (fStart != null) {
            Point start = fStart.findStart(this);
            startPoint(start.x, start.y);
        }
        if (fEnd != null) {
            Point end = fEnd.findEnd(this);
            endPoint(end.x, end.y);
        }
    }

    /**
     * Lays out the connection. This is called when the connection
     * itself changes. By default the connection is recalculated
     */
    public void layoutConnection() {
        updateConnection();
    }

    public void figureChanged(FigureChangeEvent e) {
        updateConnection();
    }

    public void figureHandlesChanged(FigureChangeEvent e) {
    }

    public void figureRemoved(FigureChangeEvent e) {
        if (listener() != null) {
            listener().figureRequestRemove(new FigureChangeEvent(this));
        }
    }

    public void figureRequestRemove(FigureChangeEvent e) {
    }

    public void figureInvalidated(FigureChangeEvent e) {
    }

    public void figureRequestUpdate(FigureChangeEvent e) {
    }

    public void release() {
        super.release();
        handleDisconnect(startFigure(), endFigure());
        if (fStart != null) {
            startFigure().removeFigureChangeListener(this);
        }
        if (fEnd != null) {
            endFigure().removeFigureChangeListener(this);
        }
    }

    /**
     * Returns the figures with dependencies of superclasses
     * plus the two figures to which this line is connected
     * to. Unlike the parent/child symmetry the connector
     * figures don't report this connection as a dependency.
     * So it is possible to duplicate the figures without
     * duplicating all its connections.
     **/
    public FigureEnumeration getFiguresWithDependencies() {
        FigureEnumeration superDep = super.getFiguresWithDependencies();
        Vector<Figure> myDep = new Vector<Figure>(2);
        myDep.addElement(start().owner());
        myDep.addElement(end().owner());
        return new MergedFigureEnumerator(superDep, new FigureEnumerator(myDep));
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeStorable(fStart);
        dw.writeStorable(fEnd);
    }

    public void read(StorableInput dr) throws IOException {
        super.read(dr);
        Connector start = (Connector) dr.readStorable();
        if (start != null) {
            connectStart(start);
        }
        Connector end = (Connector) dr.readStorable();
        if (end != null) {
            connectEnd(end);
        }
        if (start != null && end != null) {
            updateConnection();
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except reconnecting itself to the <code>fStart
     * </code>and <code>fEnd</code> connectors again.
     **/
    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException {
        s.defaultReadObject();

        if (fStart != null) {
            connectStart(fStart);
        }
        if (fEnd != null) {
            connectEnd(fEnd);
        }
    }
}