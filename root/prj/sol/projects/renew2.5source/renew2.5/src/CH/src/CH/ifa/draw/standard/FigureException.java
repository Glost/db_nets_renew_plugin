package CH.ifa.draw.standard;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;

import java.util.Vector;


// Some time ago, this class extended java.lang.Exception, but why?
// Comment by Joern:
// I'll tell you why, so it's catchable and you can use methods
// like printStackTrace.
// Well, it's probably never thrown, so I'll leave it like this.
public class FigureException {
    public final int line;
    public final int column;
    public final TextFigure textErrorFigure;
    public final String title;
    public final String message;
    public final Drawing errorDrawing;
    public final Vector<Figure> errorFigures;
    private Object o;

    public FigureException(String message, int line, int column,
                           Drawing drawing, TextFigure textErrorFigure) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.errorDrawing = drawing;
        this.textErrorFigure = textErrorFigure;
        this.title = "Renew: Syntax Error";
        this.errorFigures = new Vector<Figure>();
        this.errorFigures.addElement(textErrorFigure);
    }

    public FigureException(String title, String message, Drawing drawing,
                           Figure offendingFigure) {
        this.message = message;
        this.title = title;
        this.errorDrawing = drawing;
        this.errorFigures = new Vector<Figure>();
        this.errorFigures.addElement(offendingFigure);
        this.line = this.column = 0;
        this.textErrorFigure = null;
    }

    public FigureException(String title, String message, Drawing drawing,
                           Vector<?extends Figure> offendingFigures) {
        this.message = message;
        this.title = title;
        this.errorDrawing = drawing;
        this.errorFigures = new Vector<Figure>();
        this.errorFigures.addAll(offendingFigures);
        this.line = this.column = 0;
        this.textErrorFigure = null;
    }

    public FigureException(String message, int line, int column,
                           Drawing drawing, TextFigure textErrorFigure,
                           Vector<Figure> errorFigures, Object o) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.errorDrawing = drawing;
        this.textErrorFigure = textErrorFigure;
        this.title = "Renew: Syntax Error";
        this.errorFigures = new Vector<Figure>(errorFigures.size());
        this.errorFigures.addAll(errorFigures);
        this.o = o;
    }

    public FigureException(String message, int line, int column,
                           Drawing drawing, TextFigure textErrorFigure, Object o) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.errorDrawing = drawing;
        this.textErrorFigure = textErrorFigure;
        this.title = "Renew: Syntax Error";
        this.errorFigures = new Vector<Figure>();
        this.errorFigures.addElement(textErrorFigure);
        this.o = o;
    }

    public String getMessage() {
        return message;
    }

    public Object getProblemSpecificInformation() {
        return this.o;
    }
}