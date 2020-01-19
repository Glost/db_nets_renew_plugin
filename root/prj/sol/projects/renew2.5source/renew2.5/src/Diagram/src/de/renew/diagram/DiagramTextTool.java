/*
 * Created on Apr 16, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.figures.TextFigure;
import CH.ifa.draw.figures.TextTool;

import CH.ifa.draw.framework.DrawingEditor;


/**
 * @author Lawrence Cabac
 */
public class DiagramTextTool extends TextTool implements IDiagramElement {
    public DiagramTextTool(DrawingEditor editor, TextFigure prototype) {
        super(editor, prototype);
    }
}