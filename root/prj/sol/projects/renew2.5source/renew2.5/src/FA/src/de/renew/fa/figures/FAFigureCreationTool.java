/*
 * @(#)PlaceFigureCreationTool.java 5.1
 *
 */
package de.renew.fa.figures;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.CreationTool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * A more efficient version of the generic creation tool that is not based on
 * cloning.
 */
public class FAFigureCreationTool extends CreationTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FAFigureCreationTool.class);
    private FigureDecoration _deco;

    public FAFigureCreationTool(DrawingEditor editor) {
        super(editor);
        _deco = new NullDecoration();
    }

    public FAFigureCreationTool(DrawingEditor editor,
                                FigureDecoration decoration) {
        super(editor);
        _deco = decoration;
    }

    /**
     * Creates a new <code>FAStateFigure</code>.
     */
    @Override
    protected Figure createFigure() {
        FAStateFigure f = new FAStateFigure();
        f.setFillColor(Color.white);
        f.setDecoration(_deco);
        logger.debug("createFigure() created " + f);
        return f;
    }

    /**
     * Creates a FAStateFigure with associated decoration.
     */
    @Override
    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();
        if (created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            Dimension d = FAStateFigure.defaultDimension();
            int w2 = d.width / 2;
            int h2 = d.height / 2;
            created.displayBox(new Point(loc.x - w2, loc.y - h2),
                               new Point(loc.x - w2 + d.width,
                                         loc.y - h2 + d.height));
        }
        super.mouseUp(e, x, y);
    }
}