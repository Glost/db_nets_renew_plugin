package de.renew.dcdiagram;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import de.renew.diagram.TaskFigure;
import de.renew.diagram.TaskFigureCreationTool;


public class DCTaskFigureCreationTool extends TaskFigureCreationTool {
    public DCTaskFigureCreationTool(DrawingEditor editor) {
        super(editor);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Figure createFigure() {
        DCTaskFigure tf = new DCTaskFigure();
        tf.setFillColor(java.awt.Color.LIGHT_GRAY);

        return tf;
    }
}