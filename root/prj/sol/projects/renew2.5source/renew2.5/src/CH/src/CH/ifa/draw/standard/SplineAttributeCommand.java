/**
 * SplineAttributeCommand.java
 *
 *
 * Created: Wed Dec 13 15:29:02 2000
 *
 * @author Friedrich Delgado Friedrichs, Lutz Kirsten
 * @version
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.util.BSpline;


public class SplineAttributeCommand extends UndoableCommand {
    // DrawingEditor fEditor;
    String fAttribute;
    int value;

    /**
     * Constructs a spline attribute command.
     * @param name the command name
     * @param attributeName the name of the attribute to be changed
     * @param pvalue the int value "attributeName" should be set to
     */
    public SplineAttributeCommand(String name, final String attributeName,
                                  int pvalue) {
        super(name);
        // this.fEditor = editor;
        fAttribute = attributeName;
        value = pvalue;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        DrawingView view = getEditor().view();
        if (view.selectionCount() <= 0) {
            return false;
        } else if (view.selectionCount() > 1) {
            return true;
        } else {
            return (view.selectionElements().nextElement() instanceof PolyLineFigure);
        }
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            DrawingView view = getEditor().view();
            Object val = new Integer(value);
            Object spline = new Integer(PolyLineFigure.BSPLINE_SHAPE);
            FigureEnumeration k = view.selectionElements();
            while (k.hasMoreElements()) {
                Figure f = k.nextFigure();
                if (f instanceof PolyLineFigure) {
                    f.setAttribute("LineShape", spline);
                    if (fAttribute.equals("standard")) {
                        f.setAttribute("BSplineSegments",
                                       new Integer(BSpline.DEFSEGMENTS));
                        f.setAttribute("BSplineDegree",
                                       new Integer(BSpline.DEFDEGREE));
                    } else {
                        f.setAttribute(fAttribute, val);
                        // logger.debug(fAttribute + ": " + val);
                    }
                }
            }
            view.drawAll(view.getGraphics());
            return true;
        }
        return false;
    }
} // SplineAttributeCommand
