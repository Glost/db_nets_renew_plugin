/*
 * Created on Jun 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.renew.fa.commands;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.Command;

import de.renew.fa.figures.FAStateFigure;
import de.renew.fa.figures.FigureDecoration;

import de.renew.gui.GuiPlugin;

import java.util.Iterator;
import java.util.Vector;


/**
 * @author jo
 *
 *
 */
public class ChangeDecorationCommand extends Command {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ChangeDecorationCommand.class);
    private FigureDecoration _deco;

    /**
     * @param name
     */
    public ChangeDecorationCommand(String name, FigureDecoration deco) {
        super(name);
        _deco = deco;
        // TODO Auto-generated constructor stub
    }

    /**
     * Changes the decoration, but also checks,
     * if there is a startstate already in case to change type to startstate,
     * and tells the FADrawing to allow a startstate agein in case a startstate
     * is changed to another type.
     *
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        DrawingView view = GuiPlugin.getCurrent().getDrawingEditor().view();
        Vector<Figure> selection = view.selection();
        for (Iterator<Figure> it = selection.iterator(); it.hasNext();) {
            Figure figure = it.next();
            if (figure instanceof FAStateFigure) {
                FAStateFigure fafigure = (FAStateFigure) figure;

                // FigureDecoration fd = fafigure.getDecoration();
                // if (_deco.equals(fd) ){
                // fafigure.setDecoration(null);
                // }
                // else{
                fafigure.setDecoration(_deco);
                fafigure.changed();

                // }
                // fafigure.invalidate();
            }
        }
        view.checkDamage();
    }
}