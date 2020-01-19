package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;

import de.renew.lola.LolaResult;
import de.renew.lola.LolaTask;

import de.renew.plugin.command.CLCommand;

import java.io.File;
import java.io.PrintStream;


/**
 * Checks a selected verification request with Lola and colors the
 * TextFigure of the request depending on the result.
 *
 * @author hewelt
 *
 */
public class CheckAllTasksCommand extends Command implements CLCommand {
    public CheckAllTasksCommand(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckVerificationRequestCommand.class);

    /* (non-Javadoc)
    * @see CH.ifa.draw.util.Command#execute()
    */
    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();
        Drawing drawing = app.drawing();

        app.getUndoRedoManager().prepareUndoSnapshot(drawing);

        if (!(drawing instanceof CPNDrawing)) {
            logger.error("[Lola] Drawing needs to be a CPNDrawing. Aborting.");
            return;
        }
        FigureEnumeration figs = drawing.figures();
        while (figs.hasMoreElements()) {
            Figure fig = figs.nextElement();
            if (fig instanceof TextFigure) {
                LolaTask task = new LolaTask((TextFigure) fig,
                                             (CPNDrawing) drawing);
                if (task.isValid()) {
                    File taskFile = task.writeToFile();
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola Task] created file " + taskFile);
                    }
                    LolaResult output = task.check();
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola Task] checked, result is "
                                     + output.getExitValue());
                    }
                    task.colorFigure();
                }
            }
        }
        app.getUndoRedoManager().commitUndoSnapshot(drawing);
    }

    @Override
    public void execute(String[] args, PrintStream response) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }
}