package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import de.renew.gui.ViewPlaceFigure;
import de.renew.gui.PlaceFigureCreationTool;

public class ViewPlaceFigureCreationTool extends PlaceFigureCreationTool implements DrawingEditorSettable {

    public ViewPlaceFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    @Override
    protected Figure createFigure() {
        return new ViewPlaceFigure();
    }

    @Override
    public void setDrawingEditor(DrawingEditor editor) {
        fEditor = editor;
    }
}
