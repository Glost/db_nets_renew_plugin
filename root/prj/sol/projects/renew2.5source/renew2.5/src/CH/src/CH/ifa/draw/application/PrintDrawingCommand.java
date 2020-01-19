package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;


/**
 * A command that prints a drawing.
 *
 * @author Michael Duvigneau
 **/
public class PrintDrawingCommand extends Command {
    public PrintDrawingCommand() {
        super("Print Drawing...");
    }

    /**
     * This command is executable only if a drawing to print is
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
     * Prints the current drawing.
     **/
    public final void execute() {
        DrawApplication app = DrawPlugin.getGui();
        app.print();
    }
}