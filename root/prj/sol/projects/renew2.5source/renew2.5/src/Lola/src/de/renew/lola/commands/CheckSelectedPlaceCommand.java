package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;
import de.renew.gui.PlaceFigure;
import de.renew.gui.VirtualPlaceFigure;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaResult;

import de.renew.plugin.command.CLCommand;

import java.awt.Color;

import java.io.File;
import java.io.PrintStream;

import java.util.Vector;


public class CheckSelectedPlaceCommand extends Command implements CLCommand {
    private String lolaPath;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckSelectedPlaceCommand.class);

    public CheckSelectedPlaceCommand(String name, String path) {
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
        Vector<Figure> selection = app.view().selection();
        LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);

        // Write lola File
        File netFile = LolaFileCreator.writeTemporaryLolaFile(drawing);
        if (logger.isDebugEnabled()) {
            logger.debug(CheckAllCommand.class.getSimpleName()
                         + ": temp file name: " + netFile.getPath());
        }

        for (Figure fig : selection) {
            LolaResult result;
            if (fig instanceof PlaceFigure) {
                if (fig instanceof VirtualPlaceFigure) {
                    continue;
                }
                result = analyzer.checkPlace(((PlaceFigure) fig), netFile);
                if (result != null) {
                    int val = result.getExitValue();
                    switch (val) {
                    case 1:
                        analyzer.colorFigure(fig, Color.GREEN);
                        break;
                    case 0:
                        analyzer.colorFigure(fig, Color.RED);
                        break;
                    case 5:
                        break;
                    }
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
        return "Check if the selected place is bounded";
    }
}