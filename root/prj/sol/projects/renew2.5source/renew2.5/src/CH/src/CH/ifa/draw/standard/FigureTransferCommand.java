/*
 * @(#)FigureTransferCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureSelection;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.util.Clipboard;

import java.util.Vector;


/**
 * Common base clase for commands that transfer figures
 * between a drawing and the clipboard.
 */
abstract class FigureTransferCommand extends UndoableCommand {
    // protected DrawingEditor fEditor;

    /**
     * Constructs a drawing command.
     * @param name the command name
     */
    protected FigureTransferCommand(String name) {
        super(name);
        // fEditor = editor;
    }

    /**
     * Deletes the selection from the drawing.
     */
    protected void deleteSelection() {
        DrawingView view = getEditor().view();
        view.drawing().removeAll(view.selection());
        view.clearSelection();
    }

    /**
     * Copies the selection to the clipboard.
     */
    protected void copySelection() {
        FigureSelection selection = getEditor().view().getFigureSelection();
        Clipboard.getClipboard().setContents(selection);
    }

    /**
     * Inserts a vector of figures and translates them by the
     * given offset.
     */
    protected void insertFigures(Vector<Figure> figures, int dx, int dy) {
        DrawingView view = getEditor().view();

        view.addAll(figures);
        view.addToSelectionAll(figures);


        // OK: Finally fixed that annoying double move bug.
        // We could also call moveSelection, since exactly the added
        // elements should be selected.
        StandardDrawingView.moveFigures(figures, dx, dy);
        view.checkDamage();
    }
}