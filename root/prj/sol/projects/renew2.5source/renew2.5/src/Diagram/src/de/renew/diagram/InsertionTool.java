/*
 * @(#)InsertionTool.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureSelection;

import CH.ifa.draw.standard.StandardDrawingView;
import CH.ifa.draw.standard.UndoableTool;

import java.awt.event.MouseEvent;

import java.util.Vector;


/**
 * A tool for inserting a vector of figures into a drawing .
 * All figures will be inserted individually
 * into the current drawing.
 * After insertion the inserted figures will automatically be selected
 * for convenient location adjustment.
 *
 * @author Lawrence Cabac
 * @version 0.2,  June 2002
 */
public class InsertionTool extends UndoableTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(InsertionTool.class);

    /**
     * the vector of figures to be drawn
     */
    private Vector<Figure> _fv;

    /**
     * the vector of figures that was drawn
     */
    private Vector<Figure> _selectedf;

    /**
     * Constructs a Tool for inserting a vector of figures
     */
    public InsertionTool(DrawingEditor editor, Vector<Figure> fv) {
        super(editor);
        _fv = fv;
    }

    /**
     * Inserts the vector of figures into the drawing at the given position.
     * @param e - the mouse event
     * @param x - the horizontal position of the mouse event
     * @param y - the vertical position of the mouse event
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        insert(x, y);
    }

    /**
     * Inserts the vector of figures into the drawing at the given position.
     * @param x - the horizontal position of the figures to be drawn
     * @param y - the vertical position of the figures to be drawn
     */
    public void insert(int x, int y) {
        try {
            Vector<Figure> v = _fv;

            FigureSelection fs = new FigureSelection(v);
            Vector<Figure> figures = fs.getData(FigureSelection.TYPE);
            _selectedf = figures;
            view().addAll(figures);
            view().addToSelectionAll(figures);
            StandardDrawingView.moveFigures(figures, x, y);


            // view().clearSelection();
            view().checkDamage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        changesMade();
    }

    /**
     * Do nothing while mouse is dragged.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        //view().moveSelection(x,y);
    }

    /**
      * Reset tool choice to default on releas of mouse button.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        editor().toolDone();
        view().addToSelectionAll(_selectedf);
    }
}