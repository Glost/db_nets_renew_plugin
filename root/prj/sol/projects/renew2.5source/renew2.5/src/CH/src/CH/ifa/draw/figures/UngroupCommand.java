/*
 * @(#)UngroupCommand.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import java.util.Vector;


/**
 * Command to ungroup the selected figures.
 * @see GroupCommand
 */
public class UngroupCommand extends UndoableCommand {
    // protected DrawingEditor getEditor();

    /**
     * Constructs a group command.
     * @param name the command name
     */
    public UngroupCommand(String name) {
        super(name);
        // getEditor() = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        FigureEnumeration selection = view.selectionElements();
        view.clearSelection();

        Vector<Figure> parts = new Vector<Figure>();
        while (selection.hasMoreElements()) {
            Figure selected = selection.nextFigure();
            Figure group = view.drawing().orphan(selected);
            FigureEnumeration k = group.decompose();
            while (k.hasMoreElements()) {
                parts.addElement(k.nextFigure());
            }
        }
        view.addAll(parts);
        view.addToSelectionAll(parts);
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