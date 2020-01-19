/*
 * @(#)BringToFrontCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;


/**
 * BringToFrontCommand brings the selected figures in the front of
 * the other figures.
 *
 * @see SendToBackCommand
 */
public class BringToFrontCommand extends UndoableCommand {

    /**
     * Constructs a bring to front command.
     * @param name the command name
     */
    public BringToFrontCommand(String name) {
        super(name);
        // getEditor() = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration k = new FigureEnumerator(view.selectionZOrdered());
        while (k.hasMoreElements()) {
            view.drawing().bringToFront(k.nextFigure());
        }
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