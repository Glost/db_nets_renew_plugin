package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.standard.ConnectionTool;
import de.renew.gui.RollbackArcConnection;

public class RollbackArcConnectionCreationTool extends ConnectionTool implements DrawingEditorSettable {

    public RollbackArcConnectionCreationTool(DrawingEditor editor) {
        super(editor, RollbackArcConnection.ROLLBACK_ARC_CONNECTION);
    }

    @Override
    public void setDrawingEditor(DrawingEditor editor) {
        fEditor = editor;
    }
}
