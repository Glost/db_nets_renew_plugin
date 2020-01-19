/*
 * @(#)JoinFigureCreationTool.java
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
public class JoinFigureCreationTool extends CreationTool {
    private int startPosY;
    private int startPosX;
    private boolean _vertical;
    private FigureDecoration _deco = null;

    public JoinFigureCreationTool(DrawingEditor editor, boolean vertical,
                                  FigureDecoration fd) {
        super(editor);
        _deco = fd;
        _vertical = vertical;
    }

    /**
     * Creates a new JoinFigure.
     */
    protected Figure createFigure() {
        ISplitFigure sf = null;

        if (_vertical) {
            sf = new VJoinFigure();
        }

        //         else {
        //            sf = new HJoinFigure();
        //        }
        if (sf != null) {
            sf.setFillColor(java.awt.Color.black);
            sf.setDecoration(_deco);

            return (Figure) sf;
        }
        return null;
    }

    public void mouseUp(MouseEvent e, int x, int y) {
        if (startPosX != x || startPosY != y) {
            Figure created = createdFigure();
            if (created.isEmpty()) {
                Point loc = created.displayBox().getLocation();
                Dimension d = new Dimension();
                if (_vertical) {
                    d = VJoinFigure.defaultDimension();
                    logger.debug("VJoinFigure");
                }


                //                else {
                //                    d = HJoinFigure.defaultDimension();
                //                    logger.debug("HJoinFigure");
                //                }
                int w2 = d.width / 2;
                int h2 = d.height / 2;
                created.displayBox(new Point(loc.x - w2, loc.y - h2),
                                   new Point(loc.x - w2 + d.width,
                                             loc.y - h2 + d.height));
            }
            super.mouseUp(e, x, y);
            //add the LifeLineConnection automatically
            if (getDrawnFigure() instanceof VJoinFigure) {
                Figure parentFigure1 = findParent(created,
                                                  created.displayBox().x);
                Figure parentFigure2 = findParent(created,
                                                  created.displayBox().x
                                                  + created.displayBox().width);

                if (parentFigure1 != null && parentFigure2 != null) {
                    Figure fig = created;

                    Vector<Figure> parents = ((TailFigure) fig).getDParents();
                    Iterator<Figure> pit = parents.iterator();
                    while (pit.hasNext()) {
                        LifeLineConnection lifeLine = new LifeLineConnection(1);
                        Figure parent = pit.next();

                        //NOTICEsignature
                        Connector startConnector = determineStartConnector(parent,
                                                                           parent
                                                                           .center().x,
                                                                           parent
                                                                           .displayBox().y
                                                                           + parent
                                                                             .displayBox().height);
                        Connector endConnector = fig.connectorAt(parent.center().x,
                                                                 fig.center().y);
                        lifeLine.startPoint(parent.center());
                        lifeLine.endPoint(fig.center());

                        lifeLine.connectStart(startConnector);
                        lifeLine.connectEnd(endConnector);
                        view().add(lifeLine);
                        //snapToFit(fig, parent, startConnector);
                        lifeLine.updateConnection();
                        ((TailFigure) fig).addDParentConnector(startConnector);
                    }

                    // add the peer
                    VJoinFigure join = (VJoinFigure) created;
                    logger.debug("joinFigure " + join);
                    String fileName = "cond";
                    if (join.getDecoration() instanceof ANDDecoration) {
                        fileName = "pjoin";
                    }
                    join.addPeerName(fileName);
                } else {
                    view().remove(created);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.framework.Tool#mouseDown(java.awt.event.MouseEvent, int, int)
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        startPosX = x;
        startPosY = y;
        super.mouseDown(e, x, y);
    }

    protected Figure findParent(Figure figure, int xPosition) {
        logger.debug("1Figure  " + figure);
        TailFigure fig = (TailFigure) figure;
        Locator loc = getLocator();
        if (loc != null) {
            Figure dParent = loc.getElementAtPosition(xPosition,
                                                      figure.displayBox().y);
            logger.debug("2Figure Parent is " + dParent);
            if (dParent != null) {
                //loc.add(fig);
                RoleDescriptorFigure head = ((TailFigure) dParent).getDHead();
                head.addToTail(figure);
                fig.setDHead(head);
                TailFigure parent = (TailFigure) dParent;
                fig.addDParent(parent);
                parent.addDChild(fig);


                //                if (dParent instanceof RoleDescriptorFigure) {
                //                    fig.addPeerName("start");
                //                }
                //logger.debug("DiagramDrawing second "+ generatePeerInstantaneously);
                if (generatePeerInstantaneously()) {
                    (fig).generatePeers();
                }
                logger.debug("Figure added as Parent " + dParent);
                return dParent;
            }
        }
        return null;
    }
}