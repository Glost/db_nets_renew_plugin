/*
 * @(#)SetFSRenderModeCommand.java 5.1
 *
 */
package de.renew.gui.fs;

import CH.ifa.draw.util.Command;


/**
 * A command to set the mode in which Feature Structures are rendered,
 * either to "normal" or to "UML".
 * <p>
 * Clears the undo history for all drawings!
 * </p>
 */
public class SetFSRenderModeCommand extends Command {
    private boolean fUML;

    /**
     * Constructs a toggle grid command.
     * @param name the command name
     * @param renderUML whether UML rendering should be used
     */
    public SetFSRenderModeCommand(String name, boolean renderUML) {
        super(name);
        fUML = renderUML;
    }

    public void execute() {
        FSPlugin.getCurrent().setUmlRenderMode(fUML);
    }

    public boolean isExecutable() {
        return true;
    }
}