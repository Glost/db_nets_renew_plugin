/*
 * @(#)GroupCommand.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.UndoableCommand;

import java.util.Vector;


/**
 * Command to group the selection into a GroupFigure.
 *
 * @see GroupFigure
 */
public class GroupCommand extends UndoableCommand {
    // protected DrawingEditor fEditor;

    /**
     * Constructs a group command.
     * @param name the command name
     */
    public GroupCommand(String name) {
        super(name);
        // fEditor = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        Vector<Figure> selected = view.selectionZOrdered();
        Drawing drawing = view.drawing();
        if (selected.size() > 0) {
            view.clearSelection();
            drawing.orphanAll(selected);

            GroupFigure group = new GroupFigure();
            group.addAll(selected);
            view.addToSelection(drawing.add(group));
            view.checkDamage();
            return true;
        }
        return false;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return getEditor().view().selectionCount() > 0;
    }
}