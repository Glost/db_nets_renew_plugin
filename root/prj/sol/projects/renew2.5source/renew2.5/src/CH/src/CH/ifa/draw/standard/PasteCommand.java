/*
 * @(#)PasteCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureSelection;

import CH.ifa.draw.util.Clipboard;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Command to insert the clipboard into the drawing.
 * @see Clipboard
 */
public class PasteCommand extends FigureTransferCommand {

    /**
     * Constructs a paste command.
     * @param name the command name
     */
    public PasteCommand(String name) {
        super(name);
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();
        Point lastClick = view.lastClick();
        FigureSelection selection = (FigureSelection) Clipboard.getClipboard()
                                                               .getContents();
        if (selection != null) {
            Vector<Figure> figures = selection.getData(FigureSelection.TYPE);
            if (figures.size() == 0) {
                return false;
            }

            Rectangle r = bounds(figures.elements());
            view.clearSelection();

            insertFigures(figures, lastClick.x - r.x, lastClick.y - r.y);
            view.checkDamage();
            return true;
        }
        return false;
    }

    Rectangle bounds(Enumeration<Figure> k) {
        Rectangle r = (k.nextElement()).displayBox();
        while (k.hasMoreElements()) {
            r.add((k.nextElement()).displayBox());
        }
        return r;
    }
}