/*
 * Created on 28.01.2004
 *
 */
package CH.ifa.draw.util;

import java.awt.Toolkit;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


/**
 * This class represents a JMenuItem that knows a Command to be triggered
 * if the item is selected by the user.
 *
 * @author J&ouml;rn Schumacher
 */
public class CommandMenuItem extends JMenuItem {

    /**
     * Holds the reference to the associated <code>Command</code>.
     **/
    private Command cmd;

    /**
     * Creates a new <code>CommandMenuItem</code> associated with
     * the given command.
     *
     * @param cmd  the associated <code>Command</code>
     **/
    public CommandMenuItem(Command cmd) {
        super(cmd.name());
        this.cmd = cmd;
    }

    /**
     * Creates a new <code>CommandMenuItem</code> associated with
     * the given command, using the given accelerator key.
     *
     * @param cmd  the associated <code>Command</code>
     * @param ms   the accelerator key to use as shortcut for this command.
     **/
    public CommandMenuItem(Command cmd, int ms) {
        super(cmd.name());
        setAccelerator(KeyStroke.getKeyStroke(ms,
                                              Toolkit.getDefaultToolkit()
                                                     .getMenuShortcutKeyMask()));
        this.cmd = cmd;
    }

    /**
     * Creates a new <code>CommandMenuItem</code> associated with
     * the given command, using the given accelerator key.
     *
     * @param cmd  the associated <code>Command</code>
     * @param ms   the accelerator key to use as shortcut for this command.
     * @param modifier the modifier that determines the modifying key(s)
     **/
    public CommandMenuItem(Command cmd, int ms, int modifier) {
        super(cmd.name());
        setAccelerator(KeyStroke.getKeyStroke(ms, modifier));
        this.cmd = cmd;
    }

    public Command getCommand() {
        return this.cmd;
    }

    public void setCommand(Command c) {
        this.cmd = c;
    }
}