package de.renew.gui;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;


/**
 * Merges two transitions into one transition.
 *
 * MergeCommand.java
 * Created: Wed Nov  8 2000
 * @author Jens Norgall, Marc Schoenberg
 */
public class MergeCommand extends UndoableCommand {
    // private DrawingEditor editor;
    public MergeCommand(String name) {
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
            FigureEnumeration selected = view.selectionElements();
            int i = 1;
            Object buf_obj;

            // die erste Transition wird die letzte sein...	
            while (!((buf_obj = selected.nextElement()) instanceof TransitionFigure)) {
            }
            TransitionFigure finalTrans = (TransitionFigure) buf_obj;
            int finalx = finalTrans.center().x;
            int finaly = finalTrans.center().y;

            //... waehrend wir die anderen sukzessive durchgehen.
            while (selected.hasMoreElements()) {
                if ((buf_obj = selected.nextElement()) instanceof TransitionFigure) {
                    i++;
                    TransitionFigure nextTrans = (TransitionFigure) buf_obj;
                    FigureEnumeration figures = drawing.figures();

                    // hier gehen wir die im Drawing enthaltenen Figuren Stueck fuer Stueck durch...
                    while (figures.hasMoreElements()) {
                        Figure fig = figures.nextElement();

                        // ... und wenn wir einen Pfeil finden...
                        if (fig instanceof ArcConnection) {
                            ArcConnection arc = (ArcConnection) fig;

                            //... der bei nextTrans beginnt,...
                            if (arc.startFigure().equals(nextTrans)) {
                                //... dann haengen wir seinen Anfang an finalTrans um.
                                arc.disconnectStart();
                                arc.connectStart(finalTrans.connectorAt(finalTrans
                                                                        .center().x,
                                                                        finalTrans
                                                                        .center().y));
                                arc.updateConnection();
                            }

                            // entsprechend fuer Pfeil, der bei nextTrans endet.
                            if (arc.endFigure().equals(nextTrans)) {
                                arc.disconnectEnd();
                                arc.connectEnd(finalTrans.connectorAt(finalTrans
                                                                      .center().x,
                                                                      finalTrans
                                                                      .center().y));
                                arc.updateConnection();
                            }
                        }
                    }

                    // children umhaengen
                    while (nextTrans.children().hasMoreElements()) {
                        ChildFigure fig = (ChildFigure) nextTrans.children()
                                                                 .nextElement();
                        fig.setParent(finalTrans);
                        drawing.bringToFront(fig);
                    }

                    finalx += nextTrans.center().x;
                    finaly += nextTrans.center().y;
                    view.remove(nextTrans);
                }
            }
            finalx = finalx / i;
            finaly = finaly / i;
            finalTrans.moveBy((finalx - finalTrans.center().x),
                              (finaly - finalTrans.center().y));
            view.checkDamage();
            return true;
        }
        return false;
    }

    /**
     * @return true, if at least two transitions are selected. <br>
     *         false, otherwise.
     */
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        DrawingView view = getEditor().view();
        FigureEnumeration fe = view.selectionElements();

        //NOTICEredundant
        while (fe.hasMoreElements()) {
            if (fe.nextElement() instanceof TransitionFigure) {
                while (fe.hasMoreElements()) {
                    if (fe.nextElement() instanceof TransitionFigure) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}