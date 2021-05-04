package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.standard.ConnectionTool;
import de.renew.gui.RollbackArcConnection;

/**
 * The palette tool for creating the db-net rollback arc UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class RollbackArcConnectionCreationTool extends ConnectionTool implements DrawingEditorSettable {

    /**
     * The palette tool's constructor.
     *
     * @param editor The drawing editor's instance.
     */
    public RollbackArcConnectionCreationTool(DrawingEditor editor) {
        super(editor, RollbackArcConnection.ROLLBACK_ARC_CONNECTION);
    }

    /**
     * Sets the drawing editor's instance to the UI figure.
     *
     * @param editor The drawing editor's instance.
     */
    @Override
    public void setDrawingEditor(DrawingEditor editor) {
        fEditor = editor;
    }
}
