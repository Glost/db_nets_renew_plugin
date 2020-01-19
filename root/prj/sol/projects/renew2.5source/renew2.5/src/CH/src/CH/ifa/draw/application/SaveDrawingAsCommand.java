package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * A command that saves a drawing into an interactively specified file.
 *
 * @author Michael Duvigneau
 **/
public class SaveDrawingAsCommand extends Command {
    public SaveDrawingAsCommand() {
        super("Save Drawing As...");
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
     * Saves the current drawing into an interactively specified file.
     **/
    public final void execute() {
        DrawApplication app = DrawPlugin.getGui();
        if (app.promptSaveAs(app.drawing())) {
            app.saveDrawing(app.drawing());
        }
    }
}