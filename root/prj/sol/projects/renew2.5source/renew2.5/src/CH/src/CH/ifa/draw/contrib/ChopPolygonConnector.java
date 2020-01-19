/*
 * Copyright (c) 1996, 1997 Erich Gamma
 * All Rights Reserved
 */
package CH.ifa.draw.contrib;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ChopBoxConnector;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;


/**
 * A ChopPolygonConnector locates a connection point by
 * chopping the connection at the polygon boundary.
 */
public class ChopPolygonConnector extends ChopBoxConnector {
    /*
     * Serialization support.
     */
    private static final long serialVersionUID = -156024908227796826L;

    /**
     * Creates a ChopPolygonConnector.
     */
    public ChopPolygonConnector() {
    }

    /**
     * Creates a ChopPolygonConnector for the given owner.
     * @param owner the owner of the ChopPolygonConnector
     */
    public ChopPolygonConnector(Figure owner) {
        super(owner);
    }

    protected Point chop(Figure target, Rectangle source) {
        Polygon polygon = ((OutlineFigure) target).outline();

        return PolygonFigure.chop(polygon, Geom.center(source));
    }
}