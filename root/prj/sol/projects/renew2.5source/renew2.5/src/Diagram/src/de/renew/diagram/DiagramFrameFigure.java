/*
 * Created on Apr 14, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.figures.RoundRectangleFigure;

import java.awt.Point;


/**
 * @author Lawrence Cabac
 */
public class DiagramFrameFigure extends RoundRectangleFigure
        implements IDiagramElement {
    public DiagramFrameFigure() {
        super();
    }

    public DiagramFrameFigure(Point origin, Point corner) {
        super(origin, corner);
    }
}