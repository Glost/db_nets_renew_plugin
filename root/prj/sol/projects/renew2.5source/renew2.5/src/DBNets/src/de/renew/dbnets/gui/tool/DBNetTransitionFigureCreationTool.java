package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;
import de.renew.gui.DBNetTransitionFigure;
import de.renew.gui.TransitionFigureCreationTool;

/**
 * The palette tool for creating the db-net transition UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
