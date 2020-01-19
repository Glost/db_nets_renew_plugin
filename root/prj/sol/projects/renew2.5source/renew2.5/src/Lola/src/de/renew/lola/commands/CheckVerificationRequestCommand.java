/**
 *
 */
package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaHelper;
import de.renew.lola.LolaResult;

import de.renew.plugin.command.CLCommand;

import java.awt.Color;

import java.io.File;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Vector;


/**
 * Checks a selected verification request with Lola and colors the
 * TextFigure of the request depending on the result.
 *
 * @author hewelt
 *
 */
public class CheckVerificationRequestCommand extends Command
        implements CLCommand {
    private File lolaPath;
    private LolaFileCreator creator;
    private LolaAnalyzer analyzer;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckVerificationRequestCommand.class);

    /**
     * @param name
     */
    public CheckVerificationRequestCommand(String name, String path) {
        super(name);
        lolaPath = new File(path);
        creator = new LolaFileCreator();
        analyzer = new LolaAnalyzer(lolaPath.toString());
    }

    /**
     * Checks selected verification requests.
     *
     */
    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();
        Drawing drawing = app.drawing();

        app.getUndoRedoManager().prepareUndoSnapshot(drawing);
        Vector<Figure> selection = app.view().selection();

        if (!(drawing instanceof CPNDrawing)) {
            logger.error("[Lola] Drawing needs to be a CPNDrawing. Aborting.");
            return;
        }

        // create lola file
        File lolaFile = LolaFileCreator.writeTemporaryLolaFile((CPNDrawing) drawing);
        String netName = drawing.getName();
        Boolean changed = false;
        for (Figure fig : selection) {
            if (fig instanceof TextFigure) {
                String taskType = "";
                String taskText = ((TextFigure) fig).getText();

                // special treatment for assert statements
                if (((TextFigure) fig).parent() != null
                            && taskText.startsWith(LolaHelper.assertKey)) {
                    Figure parent = ((TextFigure) fig).parent();
                    String parentName = creator.name((FigureWithID) parent);
                    if (parent instanceof PlaceFigure) {
                        taskText = LolaHelper.placeKey + " " + parentName;
                        taskType = LolaHelper.placeFileName;
                        if (logger.isDebugEnabled()) {
                            logger.debug("[Lola] Found assert task.");
                        }
                    } else if (parent instanceof TransitionFigure) {
                        taskText = LolaHelper.transitionKey + " " + parentName;
                        taskType = LolaHelper.transitionFileName;
                        if (logger.isDebugEnabled()) {
                            logger.debug("[Lola] Found assert task.");
                        }
                    } else {
                        taskType = ""; // so we won't call lola
                        logger.error("[Lola] Assert statements need to be attached to places or transitions.");
                    }
                } else { // parsing of other tasks is outsourced
                    taskType = creator.parseTask((TextFigure) fig);
                }
                if (taskType.equals("")) {
                    logger.error("[Lola] No parseable task found in text.");
                } else { // check found task
                    String taskFileName = netName + taskType;
                    File taskFile = creator.writeTaskFile(taskFileName,
                                                          taskText, taskType);
                    String lolaCommand = LolaHelper.taskCommandMap.get(taskType);
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola] Checking task of type " + taskType
                                     + " from file " + taskFile.toString()
                                     + ".");
                    }
                    LolaResult output = analyzer.checkTask(taskFile, lolaFile,
                                                           lolaCommand);
                    if (output.getExitValue() == 3) {
                        fig.setAttribute("FrameColor", Color.BLACK);
                        logger.error("[Lola] Syntax Error, correct verification request."
                                     + output.toString());
                        logger.info("[Lola]\n"
                                    + formatMessage(output.getOutput()));

                    } else if (output.getExitValue() == 5) {
                        fig.setAttribute("FrameColor", Color.GRAY);
                        logger.error("[Lola] Verification couldn't be verified. Maximal number of states reached.");
                    } else if (output.getExitValue() == 0) {
                        fig.setAttribute("FrameColor",
                                         taskType.equals(LolaHelper.placeFileName)
                                         ? Color.RED : Color.GREEN);
                        logger.info("[Lola] " + output.getResult().toString());
                    } else if (output.getExitValue() == 1) {
                        fig.setAttribute("FrameColor",
                                         taskType.equals(LolaHelper.placeFileName)
                                         ? Color.GREEN : Color.RED);
                        logger.info("[Lola] " + output.getResult().toString());
                        if (logger.isDebugEnabled()) {
                            logger.debug("[Lola] "
                                         + output.getOutput().toString());
                        }
                    }
                    fig.changed();

                    changed = true;
                }
            } else { // if we are not a TextFigure
                logger.info("[Lola] Currently only checking TextFigures.");
            }
        }
        if (changed) {
            DrawPlugin.getGui().view().checkDamage();
            app.getUndoRedoManager().commitUndoSnapshot(drawing);
        }
    }

    private String formatMessage(ArrayList<String> list) {
        StringBuffer sb = new StringBuffer();
        for (String s : list) {
            sb.append(s + "\n");
        }
        return sb.toString();
    }

    /* (non-Javadoc)
    * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
    */
    @Override
    public void execute(String[] args, PrintStream response) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }
}