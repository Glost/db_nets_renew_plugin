package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;


/**
 * A command that opens a drawing fetched from some URL in the editor.
 *
 * @author Michael Duvigneau
 **/
public class OpenURLCommand extends Command {
    public OpenURLCommand() {
        super("Open URL...");
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
     * Opens a drawing fetched from some URL.
     **/
    public final void execute() {
        DrawPlugin.getGui().promptOpenURL();
    }
}