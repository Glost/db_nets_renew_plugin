/*
 * @(#)TaskFigureCreationTool.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import java.util.Vector;


/**
 * A more efficient version of the generic creation
 * tool that is not based on cloning.
 */
public class TaskFigureCreationTool extends CreationTool {

    /**
     * Initializes a CreationTool with the given prototype.
     */


    //NOTICEsignature
    public TaskFigureCreationTool(DrawingEditor editor, Figure prototype) {
        super(editor);
    }

    /**
     * Constructs a CreationTool without a prototype.
     * This is for subclassers overriding createFigure.
     */
    protected TaskFigureCreationTool(DrawingEditor editor) {
        super(editor);
    }


    /**
     * Creates a new TaskFigure.
     */
    protected Figure createFigure() {
        TaskFigure tf = new TaskFigure();
        tf.setFillColor(java.awt.Color.white);
        //tf.setFDisplayBox( new Rectangle(TaskFigure.defaultDimension()));
        return tf;
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();

        //NOTICEnull created maybe null at this location
        if (created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            Dimension d = TaskFigure.defaultDimension();

            int w2 = d.width / 2;
            int h2 = d.height / 2;
            created.displayBox(new Point(loc.x - w2, loc.y - h2),
                               new Point(loc.x - w2 + d.width,
                                         loc.y - h2 + d.height));
        }

        super.mouseUp(e, x, y);

        //NOTICEredundant
        if (created != null) {
            Figure parentFigure = findParent(created);
            if (parentFigure == null) {
                view().remove(created);
                return;
            }
            LifeLineConnection lifeLine = new LifeLineConnection(1);
            Figure fig = created;
            Vector<Figure> parents = ((TailFigure) fig).getDParents();
            if (parents.size() == 1) {
                Figure parent = parents.elementAt(0);

                //NOTICEsignature
                Connector startConnector = determineStartConnector(parent, x, y);
                Connector endConnector = fig.connectorAt(fig.displayBox().x + 2,
                                                         fig.displayBox().y + 2);
                lifeLine.startPoint(parent.center());
                lifeLine.endPoint(fig.center());

                lifeLine.connectStart(startConnector);
                lifeLine.connectEnd(endConnector);
                view().add(lifeLine);
                snapToFit(fig, parent, startConnector);
                lifeLine.updateConnection();
                ((TailFigure) fig).addDParentConnector(startConnector);
                if (generatePeerInstantaneously()) {
                    ((TailFigure) fig).generatePeers();
                }
            }
        }
    }
}