package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * A command that opens a new drawing in the editor.
 * The type of the drawing can be chosen interactively.
 *
 * @author Michael Duvigneau
 **/
public class NewDrawingCommand extends Command {
    public NewDrawingCommand() {
        super("New Drawing...");
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
     * Opens a new drawing.
     * The type of the drawing can be chosen interactively.
     **/
    public final void execute() {
        DrawPlugin.getGui().promptChooseNew();
    }
}