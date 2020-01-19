package de.renew.plugin.command;

import de.renew.plugin.PluginManager;

import java.io.PrintStream;


/**
 * Executing this command unloads all plugins and exits the Java VM.
 * If the argument "force" is given, the plugins are not shut down
 * previous to exiting.
 *
 * @author J&ouml;rn Schumacher
 */
public class ExitCommand implements CLCommand {
    /*
    * exit the plugin system
    */
    public void execute(String[] args, PrintStream response) {
        if (args.length > 0) {
            if ("force".equals(args[0])) {
                response.println("Forcing exit of whole system.");
                System.exit(0);
            } else if ("ifidle".equals(args[0])) {
                if (PluginManager.getInstance().checkExit()) {
                    response.println("Exiting because all plugins are inactive.");
                } else {
                    response.println("Not exiting, there are active plugins.");
                }
            } else {
                response.println("Unknown parameter to exit: " + args[0]);
            }
        } else {
            response.println("Requesting exit of whole system.");
            PluginManager.getInstance().stop();
        }
    }

    public String getDescription() {
        return "leave plugin system (append \"ifidle\" or \"force\" to modify behaviour)";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "[forcei|ifidle]";
    }
}