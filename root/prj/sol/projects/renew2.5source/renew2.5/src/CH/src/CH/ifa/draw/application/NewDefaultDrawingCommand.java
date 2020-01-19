package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingTypeManager;
import CH.ifa.draw.framework.DrawingTypeManagerListener;

import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.util.UpdatingCommand;


/**
 * A command that opens a new default drawing in the editor.
 * The default drawing type is queried from and kept up-to-date with the
 * {@link DrawingTypeManager}.
 *
 * @author Michael Duvigneau
 **/
public class NewDefaultDrawingCommand extends UpdatingCommand
        implements DrawingTypeManagerListener {
    public NewDefaultDrawingCommand() {
        super("New Default Drawing");
        getTypeManager().addListener(this);
        setType(getTypeManager().getDefaultFileFilter().getDescription());
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
     * Opens a new default drawing.
     **/
    public final void execute() {
        DrawApplication app = getDrawApplication();
        app.promptNew();
    }

    /**
     * Changes the command description according to the given drawing type.
     *
     * @param typeDescription the description of the default drawing type.
     **/
    private synchronized void setType(String typeDescription) {
        setName("New " + typeDescription);
    }

    // Implementation of CH.ifa.draw.framework.DrawingTypeManagerListener


    /**
     * Does nothing because this event is not of interest.
     **/
    public final void typeRegistered(final String string,
                                     final SimpleFileFilter simpleFileFilter) {
    }

    /**
     * Updates description and effect of this command in accordance to the
     * newly configured default type.
     *
     * @param simpleFileFilter  the <code>SimpleFileFilter</code> that
     *                          describes the new default drawing type.
     **/
    public final void defaultTypeChanged(final SimpleFileFilter simpleFileFilter) {
        setType(simpleFileFilter.getDescription());
    }

    private DrawingTypeManager getTypeManager() {
        return DrawingTypeManager.getInstance();
    }

    private DrawApplication getDrawApplication() {
        return DrawPlugin.getGui();
    }
}