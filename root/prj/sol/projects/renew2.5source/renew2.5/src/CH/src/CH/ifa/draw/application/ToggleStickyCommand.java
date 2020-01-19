/*
 * @(#)ToggleGridCommand.java 5.1
 *
 */
package CH.ifa.draw.application;

import CH.ifa.draw.util.Command;


/**
 * A command to toggle the snap to grid behavior.
 */
public class ToggleStickyCommand extends Command {
    protected DrawApplication fAppl;

    /**
     * Constructs a toggle sticky command.
     * @param name the command name
     * @param appl the drawing application to be toggled
     */
    public ToggleStickyCommand(String name, DrawApplication appl) {
        super(name);
        fAppl = appl;
    }

    public void execute() {
        fAppl.toggleAlwaysSticky();
    }
}