package de.renew.plugin;

import de.renew.plugin.command.CLCommand;


/**
 * An observable that provides information about the adding and
 * removing of <code>ClCommand</code>s.
 *
 * @author cabac
 *
 */
public interface CommandsProvider {
    public void addCommandListener(CommandsListener listener);

    public void removeCommandListener(CommandsListener listener);

    public void notifyCommandAdded(String name, CLCommand command);

    public void notifyCommandRemoved(String name);
}