package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;
import de.renew.gui.VirtualPlaceFigure;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaResult;

import de.renew.plugin.command.CLCommand;

import java.awt.Color;

import java.io.File;
import java.io.PrintStream;


public class CheckAllCommand extends Command implements CLCommand {
    private String lolaPath;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckAllCommand.class);

    public CheckAllCommand(String name, String path) {
        super(name);
        lolaPath = path;
    }

    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();
        CPNDrawing drawing = (CPNDrawing) app.drawing();
        // enable undo
        app.getUndoRedoManager().prepareUndoSnapshot(app.drawing());

        // get selected figures
        FigureEnumeration figures = app.drawing().figures();
        LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);

        // Write lola File
        File netFile = LolaFileCreator.writeTemporaryLolaFile(drawing);
        if (logger.isDebugEnabled()) {
            logger.debug(CheckAllCommand.class.getSimpleName()
                         + ": temp file name: " + netFile.getPath());
        }

        while (figures.hasMoreElements()) {
            Figure fig = figures.nextFigure();
            LolaResult result = null;
            if (fig instanceof PlaceFigure) {
                if (fig instanceof VirtualPlaceFigure) {
                    continue;
                }
                result = analyzer.checkPlace(((PlaceFigure) fig), netFile);
            }
            if (fig instanceof TransitionFigure) {
                result = analyzer.checkTransition(((TransitionFigure) fig),
                                                  netFile);
            }
            if (result != null) {
                int val = result.getExitValue();
                switch (val) {
                case 0:
                    if (fig instanceof TransitionFigure) {
                        analyzer.colorFigure(fig, Color.GREEN);
                    } else if (fig instanceof PlaceFigure) {
                        analyzer.colorFigure(fig, Color.RED);
                    }
                    break;
                case 1:
                    if (fig instanceof TransitionFigure) {
                        analyzer.colorFigure(fig, Color.RED);
                    } else if (fig instanceof PlaceFigure) {
                        analyzer.colorFigure(fig, Color.GREEN);
                    }
                    break;
                case 5:
                    break;
                }
            }
        }
        app.getUndoRedoManager().commitUndoSnapshot(app.drawing());
    }

    @Override
    public void execute(String[] args, PrintStream response) {
        //TODO: implement the check from console
    }

    @Override
    public String getDescription() {
        return "Check all places and transitions.";
    }
}