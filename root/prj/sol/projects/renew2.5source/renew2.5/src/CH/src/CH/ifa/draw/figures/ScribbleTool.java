/*
 * @(#)ScribbleTool.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.UndoableTool;

import java.awt.event.MouseEvent;


/**
 * Tool to scribble a PolyLineFigure
 * @see PolyLineFigure
 */
public class ScribbleTool extends UndoableTool {
    private PolyLineFigure fScribble;
    private int fLastX;
    private int fLastY;

    public ScribbleTool(DrawingEditor editor) {
        super(editor);
    }

    public void activate() {
        super.activate();
        fScribble = null;
    }

    public void deactivate() {
        if (fScribble != null) {
            if (fScribble.size().width < 4 || fScribble.size().height < 4) {
                drawing().remove(fScribble);
                noChangesMade();
            }
        }
        fScribble = null;
        super.deactivate();
    }

    private void point(int x, int y) {
        if (fScribble == null) {
            fScribble = new PolyLineFigure(x, y);
            view().add(fScribble);
            changesMade();
        } else if (fLastX != x || fLastY != y) {
            fScribble.addPoint(x, y);
        }

        fLastX = x;
        fLastY = y;
    }

    public void mouseDown(MouseEvent e, int x, int y) {
        if (e.getClickCount() >= 2) {
            editor().toolDone();
        } else {
            // use original event coordinates to avoid
            // supress that the scribble is constrained to
            // the grid
            point(e.getX(), e.getY());
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
        if (fScribble != null) {
            point(e.getX(), e.getY());
        }
    }
}