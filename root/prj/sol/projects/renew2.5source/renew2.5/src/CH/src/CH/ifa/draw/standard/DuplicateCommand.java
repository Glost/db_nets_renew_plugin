/*
 * @(#)DuplicateCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureSelection;

import java.util.Vector;


/**
 * Duplicate the selection and select the duplicates.
 */
public class DuplicateCommand extends FigureTransferCommand {

    /**
     * Constructs a duplicate command.
     * @param name the command name
     */
    public DuplicateCommand(String name) {
        super(name);
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();

        FigureSelection selection = view.getFigureSelection();

        view.clearSelection();

        Vector<Figure> figures = selection.getData(FigureSelection.TYPE);
        insertFigures(figures, 10, 10);
        view.checkDamage();
        return true;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}