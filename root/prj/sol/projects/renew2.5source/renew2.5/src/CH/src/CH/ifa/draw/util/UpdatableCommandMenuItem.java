package CH.ifa.draw.util;



/**
 * A <code>CommandMenuItem</code> whose description can be updated by its
 * associated <code>UpdatingCommand</code>.
 * <p>
 * <b>Feature:</b>
 * For the time being, the association between the menu item and its
 * command is never released.
 * </p>
 * Created: Mon Jun 20  2005
 *
 * @author Michael Duvigneau
 * @see UpdatingCommand
 **/
public class UpdatableCommandMenuItem extends CommandMenuItem {

    /**
     * Creates a new <code>UpdatableCommandMenuItem</code> associated with
     * the given command.
     *
     * @param cmd  the associated <code>UpdatingCommand</code>
     **/
    public UpdatableCommandMenuItem(UpdatingCommand cmd) {
        super(cmd);
        cmd.addMenuItem(this);
    }

    /**
     * Creates a new <code>UpdatableCommandMenuItem</code> associated with
     * the given command, using the given accelerator key.
     *
     * @param cmd  the associated <code>UpdatingCommand</code>
     * @param ms   the accelerator key to use as shortcut for this command.
     **/
    public UpdatableCommandMenuItem(UpdatingCommand cmd, int ms) {
        super(cmd, ms);
        cmd.addMenuItem(this);
    }

    /**
     * To be called by the associated <code>UpdatingCommand</code> when its
     * name changes.
     *
     * @param newName a <code>String</code> value
     **/
    public void updateName(String newName) {
        setText(newName);
    }
}