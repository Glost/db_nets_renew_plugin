/**
 *
 */
package de.renew.diagram.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Command;

import de.renew.diagram.ActionTextFigure;
import de.renew.diagram.DCServiceTextFigure;
import de.renew.diagram.DiagramTextFigure;


/**
 * @author Cabac
 *
 */
public class UnhideTextCommand extends Command {
    public UnhideTextCommand(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();
        if (app != null) {
            Drawing drawing = app.drawing();
            FigureEnumeration fe = drawing.figures();
            while (fe.hasMoreElements()) {
                Figure fig = fe.nextFigure();
                if (fig instanceof ActionTextFigure
                            || fig instanceof DCServiceTextFigure) {
                    DiagramTextFigure textf = (DiagramTextFigure) fig;
                    textf.unhideText();
                }
            }
            drawing.checkDamage();
        }
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.util.Command#isExecutable()
     */
    @Override
    public boolean isExecutable() {
        DrawApplication app = DrawPlugin.getGui();
        if (app != null) {
            if (app.drawing() == null) {
                return false;
            }
        }
        return super.isExecutable();
    }
}