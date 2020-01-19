package de.renew.diagram.peer;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import de.renew.diagram.TailFigure;

import java.awt.Point;

import java.util.Vector;


public interface IDrawingPeer {
    public abstract Vector<Figure> getFigures();

    public abstract Point getLocation();

    public abstract void setLocation(Point point);

    public abstract Vector<Point> getNextLocations();

    // abstract public DiagramTextFigure inscription();
    public abstract Vector<Figure> getStartFigures();

    public abstract Vector<Figure> getEndFigures();

    /**
     * All figures in this peer will be drawn then selected and moved to their
     * position. Then a connection between the preceding and these figures will
     * be created.
     *
     * @param view The view to draw the figures in.
     */
    public abstract void drawFigures(DrawingView view);

    public abstract void connectFigures(DrawingView view);

    public abstract Point getNextLocation(Connector con);

    public abstract String toString();

    public abstract TailFigure getOwner();
}