package de.renew.netcomponents;

import CH.ifa.draw.figures.GroupFigure;
import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.standard.ReverseFigureEnumerator;

import CH.ifa.draw.util.ColorMap;

import de.renew.gui.GuiPlugin;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;


/**
 * Command to group the selection into a NetComponentFigure.
 *
 * @see GroupFigure
 */
public class GroupCommand extends UndoableCommand {
    // protected DrawingEditor fEditor;

    /**
     * Constructs a group command.
     */
    public GroupCommand() {
        super("group selection");
        // fEditor = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = GuiPlugin.getCurrent().getDrawingEditor().view();
        Vector<Figure> selected = view.selectionZOrdered();
        HashSet<Figure> selection = new HashSet<Figure>();
        Drawing drawing = view.drawing();
        if (selected.size() > 0) {
            view.clearSelection();
            Iterator<Figure> iter = selected.iterator();
            while (iter.hasNext()) {
                Figure figure = iter.next();
                if (!(figure instanceof NetComponentFigure)) {
                    selection.add(figure);
                }
            }


            // find all NetComponentFigures that are involved with the current selection. 
            HashSet<Figure> ncFiguresInvolvedInSelection = getInvolvedNCFigures(drawing,
                                                                                selected);


            // add all elements of the NCFs to the selection and remove the NCFs from the drawing
            Iterator<Figure> it = ncFiguresInvolvedInSelection.iterator();
            while (it.hasNext()) {
                NetComponentFigure ncFigure = (NetComponentFigure) it.next();
                selection.addAll(ncFigure.getAttached());
                drawing.remove(ncFigure);
            }
            drawing.orphanAll(selected);

            drawing.orphanAll(new Vector<Figure>(selection));


            // devide the selected figures into three devisions, so that they can be layered
            // Text and Connections on top then Transitions and Places then
            // the invisible NetComponentFigure and in the back all the rest.
            Vector<Figure> first = new Vector<Figure>();
            Vector<Figure> second = new Vector<Figure>();
            Vector<Figure> third = new Vector<Figure>();
            it = selection.iterator();
            while (it.hasNext()) {
                Figure figure = it.next();
                if (figure instanceof TextFigure
                            || figure instanceof ConnectionFigure) {
                    third.add(figure);
                } else if (figure instanceof TransitionFigure
                                   || figure instanceof PlaceFigure) {
                    second.add(figure);

                } else {
                    first.add(figure);
                }
            }
            NetComponentFigure group = new NetComponentFigure();
            group.setFillColor(ColorMap.NONE);
            group.setFrameColor(ColorMap.NONE);
            group.setAttribute("LineStyle", LineConnection.LINE_STYLE_DOTTED);
            group.group(selection);


            // now add all the figures to the drawing in the predefined order
            Vector<Figure> v = new Vector<Figure>();
            v.addAll(first);
            v.add(group);
            v.addAll(second);
            v.addAll(third);
            drawing.addAll(v);


            //            selection.add(group);
            //            drawing.sendToBack(selection);
            FigureEnumeration k = new ReverseFigureEnumerator(v);
            while (k.hasMoreElements()) {
                view.drawing().sendToBack(k.nextFigure());
            }
            view.checkDamage();


            view.checkDamage();
            GuiPlugin.getCurrent()
                     .showStatus("Figures attachted fo NetComponentFigure");
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    private HashSet<Figure> getInvolvedNCFigures(Drawing drawing,
                                                 Vector<Figure> selected) {
        HashSet<Figure> ncfs = new HashSet<Figure>();
        FigureEnumeration it = drawing.figures();
        while (it.hasMoreElements()) {
            Figure figure = it.nextFigure();
            if (figure instanceof NetComponentFigure) {
                ncfs.add(figure);

            }
        }
        HashSet<Figure> involvedNCFigures = new HashSet<Figure>();

        Iterator<Figure> jt = ncfs.iterator();
        while (jt.hasNext()) {
            Figure figure = jt.next();
            if (selected.contains(figure)) {
                involvedNCFigures.add(figure);
            }
            NetComponentFigure ncFigure = ((NetComponentFigure) figure);
            Iterator<Figure> kt = ncFigure.getAttached().iterator();
            while (kt.hasNext()) {
                Figure attachedFigure = kt.next();
                if (selected.contains(attachedFigure)) {
                    involvedNCFigures.add(ncFigure);
                }
            }
        }
        return involvedNCFigures;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return GuiPlugin.getCurrent().getDrawingEditor().view().selectionCount() > 0;
    }
}