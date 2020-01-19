package de.renew.navigator.diff;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;

import de.renew.imagenetdiff.PNGDiffCommand;

import java.io.File;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
public class FileDifferImpl implements FileDiffer {
    @Override
    public void showFileDiff(File f1, File f2) {
        // Open the drawings.
        DrawApplication application = DrawPlugin.getGui();
        application.openOrLoadDrawing(f1.getPath());
        application.openOrLoadDrawing(f2.getPath());

        // Load the drawings.
        Drawing drawing1 = DrawingFileHelper.loadDrawing(f1, application);
        Drawing drawing2 = DrawingFileHelper.loadDrawing(f2, application);

        new PNGDiffCommand().doDiff(application, drawing1, drawing2, false);
    }
}