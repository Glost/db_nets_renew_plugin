package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * A command that saves a drawing into its file.  If there is no associated
 * file, the user is queried for a file name.
 *
 * @author Michael Duvigneau
 **/
public class SaveDrawingCommand extends Command {
    public SaveDrawingCommand() {
        super("Save Drawing");
    }

    /**
     * This command is executable only if a drawing to save is
     * available.
     *
     * @return <code>true</code> if there exists a current drawing.
     **/
    public final boolean isExecutable() {
        DrawApplication app = DrawPlugin.getGui();
        if (app == null) {
            return false;
        }
        return app.drawing().isStorable();
    }

    /**
     * Saves the current drawing into its file.
     **/
    public final void execute() {
        DrawApplication app = DrawPlugin.getGui();
        app.saveDrawing(app.drawing());
    }
}