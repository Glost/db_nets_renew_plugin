package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.NullDrawingEditor;

import CH.ifa.draw.util.Command;

import java.util.Vector;


/**
 * Selects the highlight figures for all selected
 * highlightable figures.
 *
 * <p></p>
 * HighlightSelectCommand.java
 * Created: Mon Feb 26  2001
 * (Code moved from CPNApplication)
 *
 * @author Frank Wienberg, Michael Duvigneau
 */
public class HighlightSelectCommand extends Command {
    // private DrawingEditor editor;
    public HighlightSelectCommand(String name) {
        super(name);
        // this.editor = editor;
    }

    protected DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    /**
     * @return <code>true</code>, if the current drawing is a
     *         <code>CPNDrawing</code> and there is at least
     *         one figure selected. If there is only one
     *         selected figure, it is checked to be a
     *         {@link FigureWithHighlight}.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        if (!super.isExecutable()) {
            return false;
        }
        Drawing drawing = getEditor().drawing();
        DrawingView view = getEditor().view();
        if (drawing instanceof CPNDrawing) {
            if (view.selectionCount() == 1) {
                return (view.selectionElements().nextFigure() instanceof FigureWithHighlight);
            } else if (view.selectionCount() > 1) {
                return true;
            }
        }
        return false;
    }

    public void execute() {
        if (isExecutable()) {
            DrawingView view = getEditor().view();
            FigureEnumeration figenumeration = view.selectionElements();
            Vector<Figure> highlights = new Vector<Figure>();
            while (figenumeration.hasMoreElements()) {
                Figure fig = figenumeration.nextFigure();
                if (fig instanceof FigureWithHighlight) {
                    Figure hilight = ((FigureWithHighlight) fig)
                                         .getHighlightFigure();
                    if (hilight != null) {
                        highlights.addElement(hilight);
                    }
                }
            }

            if (highlights.isEmpty()) {
                getEditor()
                    .showStatus("Select some nodes "
                                + "(places or transitions) with highlight figures.");
            } else {
                view.clearSelection();
                view.addToSelectionAll(highlights);


                // Redraw the newly selected elements.
                view.repairDamage();
                getEditor().showStatus("Highlight(s) selected!");
            }
        }
    }
}