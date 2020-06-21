package de.renew.dbnets.gui.tool;

import CH.ifa.draw.framework.DrawingEditor;

/**
 * The interface for the UI figures which support setting the drawing editor's instance after its creation.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public interface DrawingEditorSettable {

    /**
     * Sets the drawing editor's instance to the UI figure.
     *
     * @param editor The drawing editor's instance.
     */
    void setDrawingEditor(DrawingEditor editor);
}
