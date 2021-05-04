package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;

/**
 * The interface for the UI figures which support setting the drawing editor's instance after its creation.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public interface DrawingEditorSettable {

    /**
     * Sets the drawing editor's instance to the UI figure.
     *
     * @param editor The drawing editor's instance.
     */
    void setDrawingEditor(DrawingEditor editor);
}
