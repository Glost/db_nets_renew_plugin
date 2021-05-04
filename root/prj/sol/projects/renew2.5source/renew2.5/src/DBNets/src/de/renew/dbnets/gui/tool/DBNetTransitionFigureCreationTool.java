package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import de.renew.gui.DBNetTransitionFigure;
import de.renew.gui.TransitionFigureCreationTool;

/**
 * The palette tool for creating the db-net transition UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class DBNetTransitionFigureCreationTool extends TransitionFigureCreationTool implements DrawingEditorSettable {

    /**
     * The palette tool's constructor.
     *
     * @param editor The drawing editor's instance.
     */
    public DBNetTransitionFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    /**
     * Creates the db-net transition UI figure.
     *
     * @return The db-net transition UI figure.
     */
    @Override
    protected Figure createFigure() {
        return new DBNetTransitionFigure();
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
