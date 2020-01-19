package de.renew.navigator;

import CH.ifa.draw.util.Command;

import de.renew.plugin.command.CLCommand;

import java.io.PrintStream;


/**
 * This is the OpenNavigatorCommand class to open the Navigator.
 *
 * @author Hannes Ahrens (4ahrens)
 * @date March 2009
 *
 * Add CLCommand interface.
 *
 * @author Lawrence Cabac (cabac)
 * @date September 2015
 */
public class OpenNavigatorCommand extends Command implements CLCommand {
    NavigatorPlugin _plugin;
    public static final String CMD = "navigator";

    /**
     * @param plugin the NavigatorPlugin to call on execution
     */
    public OpenNavigatorCommand(NavigatorPlugin plugin) {
        super("Open Navigator");
        _plugin = plugin;
    }

    /**
     * Calls the NavigatorPlugin.openNavigator() method on execution.
     */
    public void execute() {
        _plugin.openNavigator();
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[], java.io.PrintStream)
     */
    @Override
    public void execute(String[] args, PrintStream response) {
        execute();
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Opens the Navigator window.";
    }
}