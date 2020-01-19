package de.renew.gui;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import java.awt.Dimension;
import java.awt.Point;


public class HorizontalCompositeFigure extends SimpleCompositeFigure {

    /**
     * Pass-through-constructor for subclasses. Does nothing.
     **/
    protected HorizontalCompositeFigure() {
    }

    /**
     * Creates a new composite figure with one contained
     * figure. The composite figure is layouted immediately after
     * creation. Additional figures can be added at later times.
     **/
    public HorizontalCompositeFigure(Figure fig) {
        add(fig);
        layout();
    }

    protected void layout() {
        Dimension extent = calcExtent();
        fDisplayBox.width = extent.width;
        fDisplayBox.height = extent.height;

        FigureEnumeration figenumeration = figures();
        Point location = fDisplayBox.getLocation();
        while (figenumeration.hasMoreElements()) {
            Figure figure = figenumeration.nextElement();
            Dimension figureDim = figure.size();
            location.y = fDisplayBox.y
                         + Math.max(0,
                                    (fDisplayBox.height - figureDim.height) / 2);
            Point corner = new Point(location.x + figureDim.width,
                                     location.y + figureDim.height);
            figure.basicDisplayBox(location, corner);
            location.x += figureDim.width;
        }
        super.layout();
    }

    protected boolean needsLayout() {
        return !size().equals(calcExtent());
    }

    private Dimension calcExtent() {
        FigureEnumeration figenumeration = figures();
        Dimension extent = new Dimension(0, 0);
        while (figenumeration.hasMoreElements()) {
            Figure figure = figenumeration.nextElement();
            Dimension figureDim = figure.size();
            extent.width += figureDim.width;
            extent.height = Math.max(extent.height, figureDim.height);
        }
        return extent;
    }
}