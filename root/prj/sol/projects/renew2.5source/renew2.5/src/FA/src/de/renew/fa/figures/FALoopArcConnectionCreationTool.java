package de.renew.fa.figures;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.UndoableTool;

import java.awt.Point;
import java.awt.event.MouseEvent;


public class FALoopArcConnectionCreationTool extends UndoableTool {
    public FALoopArcConnectionCreationTool(DrawingEditor editor) {
        super(editor);
    }

    @Override
    public void mouseDown(MouseEvent e, int x, int y) {
        Figure pressedFigure = drawing().findFigureInside(x, y);

        FAArcConnection arc = new FAArcConnection(null, new ArrowTip(),
                                                  AttributeFigure.LINE_STYLE_NORMAL);
        Object spline = new Integer(PolyLineFigure.BSPLINE_SHAPE);
        arc.setAttribute("LineShape", spline);

        arc.startPoint(pressedFigure.center());
        arc.endPoint(pressedFigure.center());
        arc.connectStart(pressedFigure.connectorAt(pressedFigure.center()));
        arc.connectEnd(pressedFigure.connectorAt(pressedFigure.center()));
        arc.updateConnection();

        int top = pressedFigure.displayBox().y - 20;
        int left = pressedFigure.center().x - 20;
        int right = pressedFigure.center().x + 20;

        Point p1 = new Point(left, top);
        arc.insertPointAt(p1, 1);
        Point p2 = new Point(right, top);
        arc.insertPointAt(p2, 2);

        view().add(arc);
        changesMade();
    }

    @Override
    public void mouseUp(MouseEvent e, int x, int y) {
        editor().toolDone();
    }
}