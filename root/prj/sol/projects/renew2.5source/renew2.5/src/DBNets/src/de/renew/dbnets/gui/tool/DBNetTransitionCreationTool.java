package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.standard.CreationTool;
import de.renew.dbnets.gui.figure.DBNetTransitionFigure;

public class DBNetTransitionCreationTool extends CreationTool {

    public DBNetTransitionCreationTool(DrawingEditor editor) {
        super(editor, new DBNetTransitionFigure(null)); // TODO: implement.
    }
}
