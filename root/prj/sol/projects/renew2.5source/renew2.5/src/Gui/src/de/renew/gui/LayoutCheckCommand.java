package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.FigureException;

import CH.ifa.draw.util.Command;

import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;


public class LayoutCheckCommand extends Command {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(LayoutCheckCommand.class);
    private static final double OVERLAPWARNINGTHRESHOLD = 0.5;

    // private CPNApplication application;
    public LayoutCheckCommand(String name) {
        super(name);
        // this.application = application;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }

    public synchronized void execute() {
        try {
            CPNApplication application = (CPNApplication) DrawPlugin.getGui();
            Enumeration<Drawing> drawings = application.drawings();
            while (drawings.hasMoreElements()) {
                Drawing drawing = drawings.nextElement();
                checkSingleDrawing(drawing);
            }
        } catch (ClassCastException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     *  Computes the relative overlap factor of two rectangles
     *
     * @author Michael Koehler, Heiko Roelke
     *
     */
    private double overlap(Rectangle r1, Rectangle r2) {
        Rectangle intersection = r1.intersection(r2);
        if (intersection.isEmpty()) {
            return 0.0;
        } else {
            double intersectionArea = intersection.height * intersection.width;
            double r1Area = r1.height * r1.width;
            double r2Area = r2.height * r2.width;
            return Math.max(intersectionArea / r1Area, intersectionArea / r2Area);
        }
    }

    /**
     *  Checks whether a @see CH.ifa.draw.figures.TextFigure is hidden
     *  by an overlapping TextFigure
     *
     * @author Michael Koehler, Heiko Roelke
     *
     *
     */
    private void checkSingleDrawing(Drawing drawing) {
        Vector<Figure> allTextFigures = new Vector<Figure>();
        FigureEnumeration figures = drawing.figures();
        boolean overlapFound = false;

        while (figures.hasMoreElements() && !overlapFound) {
            Figure figure = figures.nextFigure();
            if (figure instanceof TextFigure) {
                Rectangle figureRectangle = figure.displayBox();
                Enumeration<Figure> knownTextFigures = allTextFigures.elements();
                while (knownTextFigures.hasMoreElements() && !overlapFound) {
                    TextFigure compareTextFigure = (TextFigure) knownTextFigures
                                                   .nextElement();
                    Rectangle compareRectangle = compareTextFigure.displayBox();

                    overlapFound = (overlap(figureRectangle, compareRectangle) > OVERLAPWARNINGTHRESHOLD);
                    if (overlapFound) {
                        GuiPlugin.getCurrent()
                                 .processFigureException(new FigureException("Warning",
                                                                             "Overlapping figures detected",
                                                                             drawing,
                                                                             figure),
                                                         true);
                    }
                }
                if (!overlapFound) {
                    allTextFigures.addElement(figure);
                }
            }
        }
        if (!overlapFound) {
            DrawPlugin.getGui().showStatus("Layout check successful");
        }
    }
}