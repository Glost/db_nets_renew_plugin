package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.util.Command;

import java.util.Enumeration;


/**
 * A command that saves all open drawings into their files.
 * If a drawing has no associated file, a file name is queried
 * from the user.
 *
 * @author Michael Duvigneau
 **/
public class SaveAllDrawingsCommand extends Command {
    public SaveAllDrawingsCommand() {
        super("Save All Drawings");
    }

    /**
     * This command is executable only if the list of open drawings is not
     * empty.
     *
     * @return <code>true</code> if there exist any open drawings.
     **/
    public final boolean isExecutable() {
        DrawApplication app = DrawPlugin.getGui();
        if (app == null) {
            return false;
        }
        return app.drawings().hasMoreElements();
    }

    /**
     * Saves all current drawings into their files.
     * But first the user is queried for missing file names.
     **/
    public final void execute() {
        DrawApplication app = DrawPlugin.getGui();
        Enumeration<Drawing> drawings = app.drawings();
        while (drawings.hasMoreElements()) {
            Drawing drawing = drawings.nextElement();
            if (drawing.isStorable()) {
                if (drawing.getFilename() == null) {
                    app.showDrawingViewContainer(drawing);
                    if (!app.promptSaveAs(drawing)) {
                        return;
                    }
                }
            }
        }
        drawings = app.drawings();
        while (drawings.hasMoreElements()) {
            Drawing drawing = drawings.nextElement();
            if (drawing.isStorable()) {
                app.saveDrawing(drawing);
            }
        }
    }
}