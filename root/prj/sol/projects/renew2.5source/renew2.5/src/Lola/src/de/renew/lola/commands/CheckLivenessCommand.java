package de.renew.lola.commands;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.util.Command;

import de.renew.gui.CPNDrawing;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaResult;


public class CheckLivenessCommand extends Command {
    private String lolaPath;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(CheckLivenessCommand.class);

    public CheckLivenessCommand(String name, String path) {
        super(name);
        lolaPath = path;
    }

    @Override
    public void execute() {
        DrawApplication app = DrawPlugin.getGui();
        CPNDrawing drawing = (CPNDrawing) app.drawing();
        LolaAnalyzer analyzer = new LolaAnalyzer(lolaPath);

        LolaResult result = analyzer.checkNetLiveness(drawing);
        if (result.getExitValue() == 0) {
            logger.info("YES, net is live.");
        } else {
            if (result.getExitValue() == 1) {
                logger.info("NO, net is not live.");
            } else {
                logger.info("Unspecified result for liveness. Exit code is: "
                            + result.getExitValue());
            }
        }
    }
}