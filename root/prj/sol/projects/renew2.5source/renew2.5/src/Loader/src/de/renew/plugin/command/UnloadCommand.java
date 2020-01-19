package de.renew.plugin.command;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;

import java.io.PrintStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * This command unloads the plugin with the given name.
 * If the argument "-v" is given, verbose output is printed,
 * especially concerning load errors regarding dependency problems.
 *
 * @author J&ouml;rn Schumacher
 */
public class UnloadCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(UnloadCommand.class);

    public void execute(String[] args, PrintStream response) {
        if (args.length == 1
                    && CLCommandHelper.isFlagSet(args, "-h", "--h", "-help",
                                                         "--help")) {
            response.println("usage: unload [-v | -r | -m] <PluginName>\n"
                             + "Description: \n"
                             + "use parameter -v to see a list of all dependent plug-ins\n"
                             + "use parameter -r to unload all dependent plug-ins recursivly\n"
                             + "use parameter -m to remove given plug-ins iff there are no dependors and\n"
                             + "\t\t at least one further plug-in which provides the same service (EXPERIMENTAL!!!)");
            return;
        }
        boolean verbose = CLCommandHelper.isFlagSet(args, "-v");
        boolean recursive = CLCommandHelper.isFlagSet(args, "-r");
        boolean allowMultipleServices = CLCommandHelper.isFlagSet(args, "-m");

        String pluginName = CLCommandHelper.getPluginName(args);
        if (pluginName == null || pluginName.trim().length() == 0) {
            response.println("UnloadCommand: please set name of plug-in!");
            return;
        }

        IPlugin toShutDown = PluginManager.getInstance()
                                          .getPluginByName(pluginName);
        if (toShutDown == null) {
            response.println("UnloadCommand: did not find plug-in with name "
                             + pluginName);
            return;
        }
        response.println("\n\n------- UNLOAD -------\n\n");
        try {
            Map<String, Collection<IPlugin>> deps = CLCommandHelper
                                                        .checkDependencies(toShutDown,
                                                                           allowMultipleServices);
            Set<Entry<String, Collection<IPlugin>>> entries = deps.entrySet();
            if (entries.isEmpty()) {
                PluginManager.getInstance().stop(toShutDown);
                response.println("Plug-in " + toShutDown
                                 + " successfully removed.");
            } else if (recursive) {
                List<IPlugin> dependers = CLCommandHelper.getDependers(toShutDown,
                                                                       allowMultipleServices);
                response.println("Recursively unloading:\n -> "
                                 + CollectionLister.toString(dependers, "\n -> "));
                PluginManager.getInstance().stop(dependers);
                response.println("\nDependent plug-ins successfully removed:\n -> "
                                 + CollectionLister.toString(dependers, "\n -> "));
                response.println("\nFinally: Plug-in " + toShutDown
                                 + " successfully removed.");
            } else {
                response.println("UnloadCommand: could not unload "
                                 + toShutDown
                                 + ": there are plug-ins depending on it.");
                if (verbose) {
                    Iterator<Entry<String, Collection<IPlugin>>> entryIt = entries
                                                                           .iterator();
                    while (entryIt.hasNext()) {
                        Entry<String, Collection<IPlugin>> ent = entryIt.next();
                        response.print(ent.getKey() + ":\n\t -> ");
                        response.println(CollectionLister.toString(ent.getValue(),
                                                                   "\n\t -> "));
                    }
                } else {
                    response.println("Use \"unload -v <PluginName>\" to see a list.");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (NumberFormatException e) {
            response.println("cleanup canceled.");
        } catch (Exception e) {
            response.println("cleanup canceled: " + e + "; " + e.getMessage());
            logger.error(e.getMessage(), e);
        }
    }

    public String getDescription() {
        return "cleans up (deactivates) a plugin";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "pluginNames";
    }
}