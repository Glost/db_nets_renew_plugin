package de.renew.navigator;

import CH.ifa.draw.util.Command;


/**
 * This is the OpenInNavigatorCommand class to open the Navigator and a JFileChooser.
 *
 * @author Hannes Ahrens (4ahrens)
 * @date March 2009
 */
public class OpenInNavigatorCommand extends Command {
    NavigatorPlugin _plugin;

    /**
     * @param plugin the NavigatorPlugin to call on execution
     */
    public OpenInNavigatorCommand(NavigatorPlugin plugin) {
        super("Open in Navigator...");
        _plugin = plugin;
    }

    /**
     * Calls the NavigatorPlugin.openInNavigator() method on execution.
     */
    public void execute() {
        _plugin.openNavigator();
    }
}