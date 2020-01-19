package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.standard.NullDrawing;

import CH.ifa.draw.util.Command;


/**
 * A command that inserts drawing from a file into another drawing.
 *
 * @author Michael Duvigneau
 **/
public class InsertDrawingCommand extends Command {
    public InsertDrawingCommand() {
        super("Insert Drawing...");
    }

    /**
     * This command is executable only if a drawing to insert into is
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
     * Inserts a drawing from a file.
     **/
    public final void execute() {
        DrawPlugin.getGui().promptInsert();
    }
}