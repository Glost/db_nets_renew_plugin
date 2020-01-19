/*
 * @(#)SendToBackCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;


/**
 * A command to send the selection to the back of the drawing.
 */
public class SendToBackCommand extends UndoableCommand {
    // protected DrawingEditor fEditor;

    /**
     * Constructs a send to back command.
     * @param name the command name
     */
    public SendToBackCommand(String name) {
        super(name);
        // fEditor = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration k = new ReverseFigureEnumerator(view.selectionZOrdered());
        while (k.hasMoreElements()) {
            view.drawing().sendToBack(k.nextFigure());
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