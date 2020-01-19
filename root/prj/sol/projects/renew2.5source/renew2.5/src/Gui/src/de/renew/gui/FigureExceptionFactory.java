package de.renew.gui;

import org.apache.log4j.Logger;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.FigureException;

import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.SyntaxException;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


// Some time ago, this class extended java.lang.Exception, but why?
public class FigureExceptionFactory {
    private static final Logger logger = Logger.getLogger(FigureExceptionFactory.class);

    // the following should all be final, but compiler bug...
    public static FigureException createFigureException(SyntaxException e,
                                                        Drawing errorDrawing,
                                                        TextFigure textErrorFigure) {
        int line = e.line;
        int column = e.column;
        String message = e.getMessage();
        Object o = e.getProblemSpecificInformation();
        logger.debug("Syntax Error: ", e);
        FigureException figureException = new FigureException(message, line,
                                                              column,
                                                              errorDrawing,
                                                              textErrorFigure, o);

        return figureException;

    }

    public static FigureException createFigureException(SyntaxException e) {
        int line = e.line;
        int column = e.column;
        String message = e.getMessage();
        Object o = e.getProblemSpecificInformation();
        TextFigure textErrorFigure = null;
        Drawing errorDrawing;
        final Vector<Figure> errorFigures = new Vector<Figure>();

        if (!e.errorObjects.isEmpty()) {
            // Determine the offending drawing.
            errorDrawing = CPNDrawing.findDrawing(e.errorObjects.elementAt(0));
            if (errorDrawing != null) {
                boolean errorFound = false;
                Enumeration<Object> errorObjects = e.errorObjects.elements();
                while (errorObjects.hasMoreElements()) {
                    Object errorObject = errorObjects.nextElement();
                    Figure errorFigure = null;
                    if (errorObject instanceof ShadowNetElement) {
                        errorFigure = (Figure) ((ShadowNetElement) errorObject).context;
                    }
                    if (errorFigure != null) {
                        if (errorFound) {
                            textErrorFigure = null;
                            // only invoke text editor for single error objects!
                        } else {
                            errorFound = true;
                            if (e.line > 0 && e.column > 0
                                        && errorFigure instanceof TextFigure
                                        && ((TextFigure) errorFigure)
                                            .acceptsTyping()) {
                                textErrorFigure = (TextFigure) errorFigure;
                            }
                        }
                        errorFigures.addElement(errorFigure);
                    }
                }
            }
        } else {
            errorDrawing = null;
        }
        logger.debug("Syntax Error: ", e);


        FigureException figureException = new FigureException(message, line,
                                                              column,
                                                              errorDrawing,
                                                              textErrorFigure,
                                                              errorFigures, o);
        return figureException;
    }
}