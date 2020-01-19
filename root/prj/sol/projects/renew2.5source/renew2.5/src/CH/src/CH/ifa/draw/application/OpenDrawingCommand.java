package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * A command that opens drawing from a file in the editor.
 *
 * @author Michael Duvigneau
 **/
public class OpenDrawingCommand extends Command {
    private String path = null;

    public OpenDrawingCommand() {
        super("Open Drawing...");
    }

    public OpenDrawingCommand(String path, String name) {
        super(name);
        this.path = path;
    }


    /**
     * This command is always executable.
     * <p>
     * It is of course not executable when there is no gui open, but then
     * the command is not accessible, either.
     * </p>
     * @return always <code>true</code>
     **/
    public final boolean isExecutable() {
        return true;
    }

    /**
     * Opens a drawing from a file.
     **/
    public final void execute() {
        if (path == null) {
            DrawPlugin.getGui()
                      .promptOpen(DrawPlugin.getCurrent().getIOHelper()
                                            .getFileFilter());
        } else {
            DrawPlugin.getGui().openOrLoadDrawing(path);

        }
    }
}