package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.standard.ConnectionTool;
import de.renew.gui.ReadArcConnection;

public class ReadArcConnectionCreationTool extends ConnectionTool implements DrawingEditorSettable {

    public ReadArcConnectionCreationTool(DrawingEditor editor) {
        super(editor, ReadArcConnection.READ_ARC_CONNECTION);
    }

    @Override
    public void setDrawingEditor(DrawingEditor editor) {
        fEditor = editor;
    }
}
