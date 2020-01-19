package de.renew.gui;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import java.util.Vector;


/**
 * Associates a highlight figure to a highlightable figure.
 *
 * <p></p>
 * HighlightAssociateCommand.java
 * Created: Mon Feb 26  2001
 * (Code moved from CPNApplication)
 *
 * @author Frank Wienberg, Michael Duvigneau
 */
public class HighlightAssociateCommand extends UndoableCommand {
    //    private DrawingEditor editor;
    public HighlightAssociateCommand(String name) {
        super(name);
        //        this.editor = editor;
    }


    /**
     * @return <code>true</code>, if the current drawing is a
     *         <code>CPNDrawing</code>, there are exactly two
     *         selected figures and one of them is a
     *         {@link FigureWithHighlight}.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        Drawing drawing = getEditor().drawing();
        DrawingView view = getEditor().view();
        if (drawing instanceof CPNDrawing && view.selectionCount() == 2) {
            FigureEnumeration sel = view.selectionElements();
            while (sel.hasMoreElements()) {
                if (sel.nextFigure() instanceof FigureWithHighlight) {
                    return true;
                }
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
            Vector<Figure> vfig = view.selection();
            int pos = -1;
            if (vfig.elementAt(0) instanceof FigureWithHighlight) {
                pos = 0;
            } else if (vfig.elementAt(1) instanceof FigureWithHighlight) {
                pos = 1;
            }
            FigureWithHighlight nf = (FigureWithHighlight) vfig.elementAt(pos);
            Figure fig = vfig.elementAt(1 - pos);
            drawing.setHighlightFigure(nf, fig);
            getEditor().showStatus("Highlight associated!");
            return true;
        }
        getEditor()
            .showStatus("Select a node (place or transition) and another figure!");
        return false;
    }
}