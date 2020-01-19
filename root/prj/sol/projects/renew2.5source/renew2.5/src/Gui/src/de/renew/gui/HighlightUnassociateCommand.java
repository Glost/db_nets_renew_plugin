package de.renew.gui;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.UndoableCommand;


/**
 * Breaks up the association from a highlightable figure to
 * its highlight figure.
 *
 * <p></p>
 * HighlightUnassociateCommand.java
 * Created: Mon Feb 26  2001
 * (Code moved from CPNApplication)
 *
 * @author Frank Wienberg, Michael Duvigneau
 */
public class HighlightUnassociateCommand extends UndoableCommand {
    // private DrawingEditor editor;
    public HighlightUnassociateCommand(String name) {
        super(name);
        // this.editor = editor;
    }

    /**
     * @return <code>true</code>, if the current drawing is a
     *         <code>CPNDrawing</code>, there is exactly one
     *         selected figure and it's a {@link FigureWithHighlight}.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        Drawing drawing = getEditor().drawing();
        DrawingView view = getEditor().view();
        if (drawing instanceof CPNDrawing && view.selectionCount() == 1) {
            if (view.selectionElements().nextFigure() instanceof FigureWithHighlight) {
                return true;
            }
        }
        return false;
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }


            // The isExecutable() assertions are neccessary.
            // Otherwise, there would be errors in the following
            // code...
            CPNDrawing drawing = (CPNDrawing) getEditor().drawing();
            DrawingView view = getEditor().view();
            FigureWithHighlight fig = (FigureWithHighlight) view.selectionElements()
                                                                .nextFigure();
            drawing.setHighlightFigure(fig, null);
            getEditor().showStatus("Highlight unassociated!");
            return true;
        }
        getEditor().showStatus("Select a single node (place or transition)!");
        return false;
    }
}