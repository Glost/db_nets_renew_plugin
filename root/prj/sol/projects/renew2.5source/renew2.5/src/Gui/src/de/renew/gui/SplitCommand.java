package de.renew.gui;

import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;
import CH.ifa.draw.framework.ParentFigure;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.standard.FilteredFigureEnumerator;

import de.renew.shadow.ShadowArc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Splits a transition into two transitions connected by a place.
 *
 * SplitCommand.java
 * Created: Wed Nov  1 2000
 * @author Michael Duvigneau, Julia Hagemeister
 */
public class SplitCommand extends UndoableCommand {
    // private DrawingEditor editor;
    public SplitCommand(String name) {
        super(name);
        // this.editor = editor;
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            Drawing drawing = getEditor().drawing();
            DrawingView view = getEditor().view();

            NodeFigure origNode = (NodeFigure) view.selectionElements()
                                                   .nextElement();
            Point origNcenter = origNode.center();


            // find all arcs connected to the selected
            // transition 
            Enumeration<Figure> attachedArcs = new FilteredFigureEnumerator(drawing
                                                                            .figures(),
                                                                            new AttachedArcFilter(origNode));
            Vector<Figure> attachedArcBuffer = new Vector<Figure>();
            while (attachedArcs.hasMoreElements()) {
                attachedArcBuffer.addElement(attachedArcs.nextElement());
            }
            attachedArcs = attachedArcBuffer.elements();

            // create a new place and display it
            NodeFigure middleNode = createComplementaryNode(origNode, view);

            // create a new transition and display it
            NodeFigure endNode = createComplementaryNode(middleNode, view);
            Point endNcenter = endNode.center();

            // get connectors for all three nodes
            Connector endNconnector = endNode.connectorAt(endNcenter);

            // initialize geometrical middle calculation variables
            AveragePointCalculator inAverage = new AveragePointCalculator();
            AveragePointCalculator outAverage = new AveragePointCalculator();
            Point otherNcenter;


            // traverse all attached arcs and handle 
            // them according to their semantics
            while (attachedArcs.hasMoreElements()) {
                ArcConnection anArc = (ArcConnection) attachedArcs.nextElement();
                switch (anArc.getArcType()) {
                case ShadowArc.both:
                    // reserve arcs are splitted into two
                    // ordinary arcs, one for each of the
                    // two transitions
                    NodeFigure otherNode;
                    if (origNode.equals(anArc.startFigure())) {
                        anArc.setAttribute("ArrowMode",
                                           new Integer(PolyLineFigure.ARROW_TIP_START));
                        otherNode = (NodeFigure) anArc.endFigure();
                    } else {
                        anArc.setAttribute("ArrowMode",
                                           new Integer(PolyLineFigure.ARROW_TIP_END));
                        otherNode = (NodeFigure) anArc.startFigure();
                    }
                    ArcConnection secondArc = createArcFromTo(endNode,
                                                              otherNode, view);
                    cloneChildren(anArc, secondArc, view);
                    otherNcenter = otherNode.center();
                    inAverage.add(otherNcenter);
                    outAverage.add(otherNcenter);
                    break;
                case ShadowArc.ordinary:
                case ShadowArc.doubleOrdinary:
                    // ordinary arcs stay attached to the
                    // first transition, if inbound, or are
                    // reconnected to the second transition,
                    // if outbound
                    if (origNode.equals(anArc.startFigure())
                                && !anArc.isReverse()) {
                        anArc.disconnectStart();
                        anArc.connectStart(endNconnector);
                        anArc.updateConnection();
                        otherNcenter = anArc.endFigure().center();
                        outAverage.add(otherNcenter);
                    } else if (origNode.equals(anArc.endFigure())
                                       && anArc.isReverse()) {
                        anArc.disconnectEnd();
                        anArc.connectEnd(endNconnector);
                        anArc.updateConnection();
                        otherNcenter = anArc.startFigure().center();
                        outAverage.add(otherNcenter);
                    } else {
                        if (origNode.equals(anArc.startFigure())) {
                            otherNcenter = anArc.endFigure().center();
                        } else {
                            otherNcenter = anArc.startFigure().center();
                        }
                        inAverage.add(otherNcenter);
                    }
                    break;
                default:
                    // Other arcs don't need to be changed.
                    // (Because we don't know anything about them.)
                    // They are always treated as inbound arcs.
                    if (origNode.equals(anArc.startFigure())) {
                        otherNcenter = anArc.endFigure().center();
                    } else {
                        otherNcenter = anArc.startFigure().center();
                    }
                    inAverage.add(otherNcenter);
                    break;
                }
            }


            // create two new arcs connecting the
            // two transitions with the new place
            // and display them
            createArcFromTo(origNode, middleNode, view);
            createArcFromTo(middleNode, endNode, view);

            // reposition the three nodes
            Point inCenter = inAverage.average();
            Point outCenter = outAverage.average();
            if (inCenter == null) {
                inCenter = origNcenter;
                if (outCenter != null) {
                    inCenter = new Point(2 * origNcenter.x - outCenter.x,
                                         2 * origNcenter.y - outCenter.y);
                }
            }
            if (outCenter == null) {
                outCenter = origNcenter;
                if (inCenter != null) {
                    outCenter = new Point(2 * origNcenter.x - inCenter.x,
                                          2 * origNcenter.y - inCenter.y);
                }
            }

            if (inCenter != null) {
                int distX = (outCenter.x - inCenter.x) / 4;
                int distY = (outCenter.y - inCenter.y) / 4;
                if ((Math.abs(distX) >= 3) || (Math.abs(distY) >= 3)) {
                    centerDisplayBoxAround(inCenter.x + distX,
                                           inCenter.y + distY, origNode);
                    centerDisplayBoxAround(inCenter.x + 2 * distX,
                                           inCenter.y + 2 * distY, middleNode);
                    centerDisplayBoxAround(inCenter.x + 3 * distX,
                                           inCenter.y + 3 * distY, endNode);
                }
            }


            // refresh the display
            view.checkDamage();
            return true;
        }
        return false;
    }

    /**
     * @return true, if exactly one transition or place is selected. <br>
     *         false, otherwise.
     */
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        DrawingView view = getEditor().view();
        if (view.selectionCount() == 1) {
            Figure figure = view.selectionElements().nextElement();
            if (figure instanceof TransitionFigure) {
                return true;
            } else if (figure instanceof PlaceFigure) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies all child figures of a parent figure to another
     * parent figure. The cloned child figures are added to the
     * given view.
     *
     * Currently, the implementation does not recurse to child
     * figures of the cloned child figures, but that may change.
     *
     * @param source      The parent figure from which all child
     *                    figures should be copied.
     *
     * @param destination The parent figure to which the cloned
     *                    child figures should be attached
     *
     * @param view        The view to which the cloned child
     *                    figures should be attached. This should
     *                    be the view of the destination parent
     *                    figure.
     **/
    private void cloneChildren(ParentFigure source, ParentFigure destination,
                               DrawingView view) {
        FigureEnumeration childrenEnum = source.children();
        while (childrenEnum.hasMoreElements()) {
            ChildFigure child = (ChildFigure) childrenEnum.nextFigure().clone();
            child.setParent(destination);
            view.add(child);
        }
    }

    private void centerDisplayBoxAround(int x, int y, Figure fig) {
        Rectangle oldBox = fig.displayBox();
        fig.displayBox(new Point(x - (oldBox.width / 2), y
                                 - (oldBox.height / 2)),
                       new Point(x - (oldBox.width / 2) + oldBox.width,
                                 y - (oldBox.height / 2) + oldBox.height));
    }

    private NodeFigure createComplementaryNode(NodeFigure aNode,
                                               DrawingView view) {
        NodeFigure newNode;
        Dimension dim;
        if (aNode instanceof TransitionFigure) {
            newNode = new PlaceFigure();
            dim = PlaceFigure.defaultDimension();
        } else {
            newNode = new TransitionFigure();
            dim = TransitionFigure.defaultDimension();
        }
        Rectangle oldBox = aNode.displayBox();
        Point oldCorner = new Point(oldBox.x + oldBox.width,
                                    oldBox.y + oldBox.height);
        newNode.displayBox(oldCorner,
                           new Point(oldCorner.x + dim.width,
                                     oldCorner.y + dim.height));
        view.add(newNode);
        return newNode;
    }

    private ArcConnection createArcFromTo(NodeFigure from, NodeFigure to,
                                          DrawingView view) {
        ArcConnection newArc = new ArcConnection(ShadowArc.ordinary);
        Point fromCenter = from.center();
        Point toCenter = to.center();

        newArc.startPoint(fromCenter);
        newArc.endPoint(toCenter);
        view.add(newArc);
        newArc.connectStart(from.connectorAt(fromCenter));
        newArc.connectEnd(to.connectorAt(toCenter));
        newArc.updateConnection();
        return newArc;
    }

    /**
     * A figure filter which can be passed only by ArcConnections
     * with at least one end attached to a given node.
     * The node is specified by passing it to the
     * constructor.
     **/
    class AttachedArcFilter implements FigureFilter {
        NodeFigure origNode;

        public AttachedArcFilter(NodeFigure origNode) {
            this.origNode = origNode;
        }

        /**
         * @return <code>true</code>, if the given Figure is
         *         an ArcConnection and one of its ends is
         *         attached to the origNode specified at
         *         the constructor.
         **/
        public boolean isUsed(Figure figure) {
            if (figure instanceof ArcConnection) {
                ArcConnection arc = (ArcConnection) figure;
                if ((arc.startFigure().equals(origNode))
                            || (arc.endFigure().equals(origNode))) {
                    return true;
                }
            }
            return false;
        }
    }

    class AveragePointCalculator {
        int xSum = 0;
        int ySum = 0;
        int count = 0;

        public void add(Point p) {
            xSum += p.x;
            ySum += p.y;
            count++;
        }

        public Point average() {
            if (count == 0) {
                return null;
            } else {
                return new Point(xSum / count, ySum / count);
            }
        }
    }
} // SplitCommand
