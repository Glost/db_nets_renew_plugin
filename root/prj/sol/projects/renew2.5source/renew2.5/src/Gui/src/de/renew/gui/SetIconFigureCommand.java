package de.renew.gui;

import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.standard.AbstractFigure;


/**
 * Sets the currently selected figure as simulation icon
 * for the active drawing.
 *
 * <p></p>
 * SetIconFigureCommand.java
 * Created: Fri Feb 23  2001
 * (Code moved from CPNApplication)
 *
 * @author Frank Wienberg, Michael Duvigneau
 */
public class SetIconFigureCommand extends UndoableCommand {
    // DrawingEditor editor;
    public SetIconFigureCommand(String name) {
        super(name);
        // this.editor = editor;
    }

    /**
     * @return <code>true</code>, if exactly one figure is selected.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return (getEditor().drawing() instanceof CPNDrawing)
               && (getEditor().view().selectionCount() == 1);
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            ((CPNDrawing) getEditor().drawing()).setIconFigure((AbstractFigure) getEditor()
                                                                                    .view()
                                                                                    .selectionElements()
                                                                                    .nextFigure());
            return true;
        }
        return false;
    }
}