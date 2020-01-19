package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;

import de.renew.plugin.command.CLCommand;

import java.io.PrintStream;


/**
 * A command that closes a drawing.
 *
 * @author Michael Duvigneau
 **/
public class CloseDrawingCommand extends Command implements CLCommand {

    /**
     * Create a new command with the title "Close Drawing"
     */
    public CloseDrawingCommand() {
        super("Close Drawing");
    }

    /**
     * This command is executable only if a drawing to close is
     * available.
     *
     * @return <code>true</code> if there exists a current drawing.
     **/
    public final boolean isExecutable() {
        DrawApplication app = DrawPlugin.getGui();
        if (app == null) {
            return false;
        }
        return !(app.drawing() instanceof NullDrawing);
    }

    /**
     * Closes the current drawing.
     **/
    public final void execute() {
        DrawApplication app = DrawPlugin.getGui();
        if (app.fViewContainer != null) {
            app.closeViewContainer(app.fViewContainer);
        }
    }

    @Override
    public void execute(String[] args, PrintStream response) {
        response.append("trying to close current drawing...");
        execute();
    }

    @Override
    public String getDescription() {
        return "Close the current drawing.";
    }
}