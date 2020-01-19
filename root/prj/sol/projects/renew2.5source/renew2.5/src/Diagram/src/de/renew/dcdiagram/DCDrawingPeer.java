package de.renew.dcdiagram;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.StandardDrawingView;

import de.renew.diagram.DiagramTextFigure;
import de.renew.diagram.IDiagramElement;
import de.renew.diagram.PaletteCreatorPlugin;
import de.renew.diagram.TailFigure;
import de.renew.diagram.peer.DrawingPeer;
import de.renew.diagram.peer.NCLoader;

import java.awt.Point;

import java.util.Vector;


public class DCDrawingPeer extends DrawingPeer {
    protected Vector<Figure> dcfiguresV;
    protected Vector<Figure> startFigures;
    protected Vector<Figure> endFigures;
    protected Vector<Point> nextLocations;
    protected Point location;
    static final int XOFFSET = 300;
    static final int YOFFSET = 200;
    private String name;
    private boolean isStartFigure;
    private TailFigure owner;

    public DCDrawingPeer(Point location, String fileName, TailFigure owner,
                         IDiagramElement originator) {
        //super(owner, location, fileName);
        this.location = location;
        this.name = fileName;
        this.owner = owner;
        if (location.equals(new Point(200, 300))
                    || location.equals(new Point(50, 100))) {
            this.isStartFigure = true;
        } else {
            this.isStartFigure = false;
        }
        nextLocations = new Vector<Point>();

        NCLoader io = PaletteCreatorPlugin.getNCLoader();
        dcfiguresV = new Vector<Figure>();
        FigureEnumeration enumeration = io.getfigures(fileName);
        while (enumeration != null && enumeration.hasMoreElements()) {
            dcfiguresV.add(enumeration.nextElement());
        }
        startFigures = findStartConnectionTransitions(dcfiguresV);
        endFigures = findEndConnectionPlaces(dcfiguresV);


        /*
        if (owner instanceof DCTaskFigure) {
            if (logger.isDebugEnabled()) {
                logger.debug("\n===============================================");
                logger.debug("DCTaskFigure");
                logger.debug("===============================================\n");
            }

            if ("dc-call.rnw".equals(name)) {

            }
        }
        */
        nextLocations.add(new Point(location.x + XOFFSET, location.y));
    }

    @Override
    public void connectFigures(DrawingView view) {
        if (!isStartFigure) {
            super.connectFigures(view);
        }
    }

    @Override
    public void drawFigures(DrawingView view) {
        view.addAll(dcfiguresV);
        view.addToSelectionAll(dcfiguresV);
        StandardDrawingView.moveFigures(dcfiguresV, getLocation().x,
                                        getLocation().y);
        view.clearSelection();
        view.checkDamage();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Vector<Point> getNextLocations() {
        return nextLocations;
    }

    @Override
    public Vector<Figure> getStartFigures() {
        return startFigures;
    }

    @Override
    public Vector<Figure> getEndFigures() {
        return endFigures;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public TailFigure getOwner() {
        return owner;
    }
}