/*
 * Created on Apr 16, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.figures.ConnectedTextTool;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.DrawingEditor;


/**
 * @author Lawrence Cabac
 */
public class DiagramConnectedTextTool extends ConnectedTextTool
        implements IDiagramElement {
    public DiagramConnectedTextTool(DrawingEditor editor, TextFigure prototype) {
        super(editor, prototype);
    }

    public DiagramConnectedTextTool(DrawingEditor editor, TextFigure prototype,
                                    boolean mustConnect) {
        super(editor, prototype, mustConnect);
    }
}