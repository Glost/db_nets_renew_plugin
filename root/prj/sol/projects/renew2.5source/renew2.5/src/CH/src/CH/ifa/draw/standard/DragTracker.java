/*
 * @(#)DragTracker.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import java.awt.event.MouseEvent;


/**
 * DragTracker implements the dragging of the clicked
 * figure.
 *
 * @see SelectionTool
 */
public class DragTracker extends AbstractTool {
    protected Figure fAnchorFigure;
    private int fLastX; // previous mouse position
    private int fLastY; // previous mouse position
    private boolean fMoved = false;

    public DragTracker(DrawingEditor editor, Figure anchor) {
        super(editor);
        fAnchorFigure = anchor;
        editor().prepareUndoSnapshot();
    }

    public void mouseDown(MouseEvent e, int x, int y) {
        super.mouseDown(e, x, y);
        fLastX = x;
        fLastY = y;

        if (e.isShiftDown()) {
            view().toggleSelection(fAnchorFigure);
            fAnchorFigure = null;
        } else if (!view().selection().contains(fAnchorFigure)) {
            view().clearSelection();
            view().addToSelection(fAnchorFigure);
        }
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
        super.mouseDrag(e, x, y);
        fMoved = (Math.abs(x - fAnchorX) > 4) || (Math.abs(y - fAnchorY) > 4);

        if (fMoved) {
            drag(x - fLastX, y - fLastY);
        }
        fLastX = x;
        fLastY = y;
    }

    protected void drag(int dx, int dy) {
        view().moveSelection(dx, dy);
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        if (fMoved) {
            editor().commitUndoSnapshot();
        }
        super.mouseUp(e, x, y);
    }
}