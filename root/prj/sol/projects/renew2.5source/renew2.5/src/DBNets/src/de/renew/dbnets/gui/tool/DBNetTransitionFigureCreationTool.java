package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import de.renew.gui.DBNetTransitionFigure;
import de.renew.gui.TransitionFigureCreationTool;

public class DBNetTransitionFigureCreationTool extends TransitionFigureCreationTool implements DrawingEditorSettable {

    public DBNetTransitionFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    @Override
    protected Figure createFigure() {
        return new DBNetTransitionFigure();
    }

    @Override
    public void setDrawingEditor(DrawingEditor editor) {
        fEditor = editor;
    }
}
