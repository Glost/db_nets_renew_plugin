package de.renew.plugin;

import de.renew.plugin.command.CLCommand;


public interface CommandsListener {
    public void commandAdded(String name, CLCommand command);

    public void commandRemoved(String name);
}