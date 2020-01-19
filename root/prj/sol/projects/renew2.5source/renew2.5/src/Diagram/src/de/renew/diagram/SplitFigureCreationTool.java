/*
 * @(#)splitFigureCreationTool.java 5.1
 *
 */
package de.renew.diagram;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import java.util.Iterator;
import java.util.Vector;


/**
 * A more efficient version of the generic creation
 * tool that is not based on cloning.
 */
public class SplitFigureCreationTool extends CreationTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SplitFigureCreationTool.class);
    private boolean _vertical;
    private FigureDecoration _deco = null;

    public SplitFigureCreationTool(DrawingEditor editor, boolean vertical,
                                   FigureDecoration fd) {
        super(editor);
        _deco = fd;
        _vertical = vertical;
    }

    /**
     * Creates a new splitFigure.
     */
    protected Figure createFigure() {
        ISplitFigure sf = null;

        if (_vertical) {
            sf = new VSplitFigure();
        } else {
            sf = new HSplitFigure();
        }

        sf.setFillColor(java.awt.Color.black);
        sf.setDecoration(_deco);

        return (Figure) sf;
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        Figure created = createdFigure();
        if (created.isEmpty()) {
            Point loc = created.displayBox().getLocation();
            Dimension d = new Dimension();
            if (_vertical) {
                d = VSplitFigure.defaultDimension();
                logger.debug("VSplitFigure");
            } else {
                d = HSplitFigure.defaultDimension();
                logger.debug("HSplitFigure");
            }

            int w2 = d.width / 2;
            int h2 = d.height / 2;
            created.displayBox(new Point(loc.x - w2, loc.y - h2),
                               new Point(loc.x - w2 + d.width,
                                         loc.y - h2 + d.height));
        }
        super.mouseUp(e, x, y);
        //add the LifeLineConnection automatically
        if (getDrawnFigure() instanceof VSplitFigure) {
            Figure parentFigure = findParent(created);
            if (parentFigure != null) {
                LifeLineConnection lifeLine = new LifeLineConnection(1);
                Figure fig = created;
                Vector<Figure> parents = ((TailFigure) fig).getDParents();
                Iterator<Figure> pit = parents.iterator();
                while (pit.hasNext()) {
                    Figure parent = pit.next();

                    //NOTICEsignature
                    Connector startConnector = determineStartConnector(parent,
                                                                       x, y);
                    Connector endConnector = fig.connectorAt(fig.center());
                    lifeLine.startPoint(parent.center());
                    lifeLine.endPoint(fig.center());

                    lifeLine.connectStart(startConnector);
                    lifeLine.connectEnd(endConnector);
                    view().add(lifeLine);
                    snapToFit(fig, parent, startConnector);
                    lifeLine.updateConnection();
                    ((TailFigure) fig).addDParentConnector(startConnector);
                }

                // add the peer
                VSplitFigure split = (VSplitFigure) created;
                logger.debug("splitFigure " + split);
                String fileName = "cond";
                if (split.getDecoration() instanceof ANDDecoration) {
                    fileName = "psplit";
                }
                split.addPeerName(fileName);
            } else {
                view().remove(created);
            }
        }
    }
}