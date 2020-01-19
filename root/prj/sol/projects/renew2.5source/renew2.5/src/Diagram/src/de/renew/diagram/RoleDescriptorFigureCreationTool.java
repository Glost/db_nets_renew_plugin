/*
 * @(#)RoleDescriptorFigureCreationTool.java
 *
 */
package de.renew.diagram;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import de.renew.diagram.drawing.DiagramDrawing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;


/**
 * Creation tool for RoleDescriptor figures.
 */
public class RoleDescriptorFigureCreationTool extends CreationTool {
    public RoleDescriptorFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }

    /**
     * Creates a new RoleDescriptorFigure.
     */
    protected Figure createFigure() {
        RoleDescriptorFigure rdf = new RoleDescriptorFigure();

        // logger.debug("Text figure added" + tf);
        rdf.setFillColor(java.awt.Color.white);
        return rdf;
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();
        if (created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            Dimension d = RoleDescriptorFigure.defaultDimension();

            int w2 = d.width / 2;
            int h2 = d.height / 2;
            created.displayBox(new Point(loc.x - w2, loc.y - h2),
                               new Point(loc.x - w2 + d.width,
                                         loc.y - h2 + d.height));
        }
        super.mouseUp(e, x, y);
        //NOTICEredundant
        if (created != null) {
            Drawing drawing = view().drawing();
            if (drawing instanceof DiagramDrawing) {
                // add the created figure to the Locator so it can be found later
                //((DiagramDrawing) drawing).getLocator().add(created);
                // add also the describing text to the RDF
                RoleDescriptorFigure rdf = (RoleDescriptorFigure) created;
                DiagramTextFigure dtf = new DiagramTextFigure(rdf.getName());
                dtf.setAttribute(TextFigure.ALIGN_ATTR, TextFigure.CENTER);
                view().add(dtf);
                if (generatePeerInstantaneously()) {
                    rdf.createNewConnectedDrawing();
                }
                dtf.setParent(rdf);
            }
        }
    }
}