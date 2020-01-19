package de.renew.gui;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;
import CH.ifa.draw.framework.ParentFigure;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.standard.FilteredFigureEnumerator;

import java.util.HashSet;
import java.util.Set;


/**
 * Coarsing subnets
 *
 * CoarseCommand.java
 * Created: Wed Nov 22 2000
 * @author Jens Norgall, Marc Schoenberg
 */
public class CoarseCommand extends UndoableCommand {
    //    private DrawingEditor editor;
    public CoarseCommand(String name) {
        super(name);
        //        this.editor = editor;
    }

    public boolean executeUndoable() {
        if (isExecutable()) {
            if (!super.isExecutable()) {
                return false;
            }
            Drawing drawing = getEditor().drawing();
            DrawingView view = getEditor().view();
            FigureEnumeration selected = view.selectionElements();
            int i = 1; // counter for number of coarsed nodes (places and transitions)
            int finalx; // coordinates for final node
            int finaly; // coordinates for final node

            FigureEnumeration borderFigures = new FilteredFigureEnumerator(selected,
                                                                           new BorderFigureFilter());
            Set<Figure> borderSet = new HashSet<Figure>();

            // define some border figure as final figure
            NodeFigure finalFig = (NodeFigure) borderFigures.nextFigure();
            borderSet.add(finalFig);
            while (borderFigures.hasMoreElements()) {
                borderSet.add(borderFigures.nextFigure());
            }

            selected = view.selectionElements();

            finalx = finalFig.center().x;
            finaly = finalFig.center().y;

            // traverse other selected figures...
            while (selected.hasMoreElements()) {
                Figure nextFig = selected.nextFigure();
                if ((!nextFig.equals(finalFig))
                            && ((nextFig instanceof TransitionFigure)
                                       || (nextFig instanceof PlaceFigure))) {
                    FigureEnumeration figures = drawing.figures();

                    // traverse all figures contained in drawing
                    while (figures.hasMoreElements()) {
                        Figure fig = figures.nextFigure();

                        // if we encounter an ArcConnection...
                        if (fig instanceof ArcConnection) {
                            ArcConnection arc = (ArcConnection) fig;

                            //... starting at nextFig...
                            if (arc.startFigure().equals(nextFig)) {
                                //... change its starting point to finalFig, if nextFig is a borderFigure
                                //    otherwise remove it.
                                if (borderSet.contains(nextFig)) {
                                    arc.disconnectStart();
                                    arc.connectStart(finalFig.connectorAt(finalFig
                                                                          .center().x,
                                                                          finalFig
                                                                          .center().y));
                                    arc.updateConnection();
                                } else {
                                    view.remove(arc);
                                }
                            }

                            // respectively for ArcConnections ending at nextFig.
                            if (arc.endFigure().equals(nextFig)) {
                                if (borderSet.contains(nextFig)) {
                                    arc.disconnectEnd();
                                    arc.connectEnd(finalFig.connectorAt(finalFig
                                                                        .center().x,
                                                                        finalFig
                                                                        .center().y));
                                    arc.updateConnection();
                                } else {
                                    view.remove(arc);
                                }
                            }
                        }
                    }

                    // if nextFig is of same type as borderFigures, move nextFig's children to finalFig
                    if (((nextFig instanceof TransitionFigure)
                                && (finalFig instanceof TransitionFigure))
                                || ((nextFig instanceof PlaceFigure)
                                           && (finalFig instanceof PlaceFigure))) {
                        ParentFigure parent = (ParentFigure) nextFig;
                        while (parent.children().hasMoreElements()) {
                            ChildFigure fig = (ChildFigure) parent.children()
                                                                  .nextFigure();
                            fig.setParent((ParentFigure) finalFig);
                            drawing.bringToFront(fig);
                        }
                    }
                    i++;
                    finalx += nextFig.center().x;
                    finaly += nextFig.center().y;
                    view.remove(nextFig);
                }
            }
            finalx = finalx / i;
            finaly = finaly / i;
            finalFig.moveBy((finalx - finalFig.center().x),
                            (finaly - finalFig.center().y));
            view.checkDamage();
            return true;
        }
        return false;
    }

    /**
     * @return true, if all border figures of selected area
     *                are either places or transitions<br>
     *         false, otherwise.
     */
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        DrawingView view = getEditor().view();
        FigureEnumeration selected = view.selectionElements();
        FigureEnumeration borderFigures = new FilteredFigureEnumerator(selected,
                                                                       new BorderFigureFilter());
        Figure firstFig = borderFigures.nextFigure();

        if (firstFig instanceof TransitionFigure) {
            while (borderFigures.hasMoreElements()) {
                if (!(borderFigures.nextFigure() instanceof TransitionFigure)) {
                    return false;
                }
            }
            return true;
        } else if (firstFig instanceof PlaceFigure) {
            while (borderFigures.hasMoreElements()) {
                if (!(borderFigures.nextFigure() instanceof PlaceFigure)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * A figure filter which is passed by all places and transitions
     * an ArcConnection's end is attached to while the other end is
     * attached to a Figure outside of the selected area.
     **/
    class BorderFigureFilter implements FigureFilter {
        Set<Figure> selectedSet;

        public BorderFigureFilter() {
            DrawingView view = getEditor().view();
            FigureEnumeration selected = view.selectionElements();
            this.selectedSet = new HashSet<Figure>();
            while (selected.hasMoreElements()) {
                this.selectedSet.add(selected.nextFigure());
            }
        }

        /**
         * @return <code>true</code>, if the given Figure is
         *           a place or a transition inside the selected area
         *           and there is at least one ArcConnection with one end attached to the
         *           given Figure and the other end outside of the selected area.
         **/
        public boolean isUsed(Figure figure) {
            if ((figure instanceof TransitionFigure)
                        || (figure instanceof PlaceFigure)) {
                FigureEnumeration figures = getEditor().drawing().figures();
                while (figures.hasMoreElements()) {
                    Figure fig = figures.nextFigure();
                    if (fig instanceof ArcConnection) {
                        ArcConnection arc = (ArcConnection) fig;
                        if (((arc.startFigure().equals(figure))
                                    && (!(selectedSet.contains(arc.endFigure()))))
                                    || ((arc.endFigure().equals(figure))
                                               && (!(selectedSet.contains(arc
                                        .startFigure()))))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        // isUsed
    }

    // BorderFigureFilter
} //CoarseCommand 
