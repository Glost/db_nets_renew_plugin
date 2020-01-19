package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;


/**
 * A command that closes all open drawings.
 *
 * @author Michael Haustermann
 **/
public class CloseAllDrawingsCommand extends Command {

    /**
     * Create a new command with the title "Close All Drawings"
     */
    public CloseAllDrawingsCommand() {
        super("Close All Drawings");
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
        while (app.fViewContainer != null) {
            boolean closed = app.closeViewContainer(app.fViewContainer);
            if (!closed) {
                break;
            }
        }
    }
}