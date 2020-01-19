package de.renew.netcomponents;

import CH.ifa.draw.figures.GroupFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.UndoableCommand;

import de.renew.gui.GuiPlugin;

import java.util.Iterator;
import java.util.Vector;


/**
 * Command to group the selection into a NetComponentFigure.
 *
 * @see GroupFigure
 */
public class UngroupCommand extends UndoableCommand {
    // protected DrawingEditor fEditor;

    /**
     * Constructs an ungroup command.
     */
    public UngroupCommand() {
        super("ungroup" + " selection");
        // fEditor = editor;
    }

    /**
     * @see CH.ifa.draw.framework.UndoableCommand#executeUndoable()
     */
    public boolean executeUndoable() {
        DrawingView view = GuiPlugin.getCurrent().getDrawingEditor().view();
        Vector<Figure> selected = view.selectionZOrdered();
        Drawing drawing = view.drawing();
        Iterator<Figure> it = selected.iterator();
        boolean netComponentFigureFound = false;
        while (it.hasNext()) {
            Figure figure = it.next();
            if (figure instanceof de.renew.netcomponents.NetComponentFigure) {
                drawing.remove(figure);
                netComponentFigureFound = true;
            }
            GuiPlugin.getCurrent().showStatus("Netcomponent detached.");
        }

        return netComponentFigureFound;
    }

    /**
     * @see CH.ifa.draw.util.Command#isExecutable()
     */
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return GuiPlugin.getCurrent().getDrawingEditor().view().selectionCount() > 0;
    }
}