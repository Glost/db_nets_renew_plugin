package de.renew.plugin.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginManager;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.net.URL;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * This command displays a list of the previously loaded plugins in the system.
 *
 * @author J&ouml;rn Schumacher
 *
 */
public class ListCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(InfoCommand.class);

    /**
     * Prints the list of loaded plugins to the <code>response</code> stream.
     *
     * @param args
     *            {@inheritDoc}
     * @param response
     *            {@inheritDoc}
     */
    public void execute(String[] args, PrintStream response) {
        CommandLineParser parser = new DefaultParser();
        Options opts = new Options();
        Option ordered = new Option("o", "ordered", false, "Print ordered list.");
        opts.addOption(ordered);
        Option extended = new Option("l", "long", false,
                                     "Display more information about every plugin.");
        opts.addOption(extended);
        Option libs = new Option("j", "jar", false,
                                 "List locations of all jar files.");
        opts.addOption(libs);
        Option help = new Option("h", "help", false, "Print this message.");
        opts.addOption(help);
        CommandLine line = null;
        try {
            line = parser.parse(opts, args);
        } catch (ParseException e1) {
            HelpFormatter formatter = new HelpFormatter();
            PrintWriter writer = new PrintWriter(response, true);
            formatter.printHelp(writer, formatter.getWidth(), "list", null,
                                opts, formatter.getLeftPadding(),
                                formatter.getDescPadding(), null, true);
            return;
        }
        if (line.hasOption(help.getOpt())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setArgName("Plugin Name Fragment");
            String header = "List loaded Renew plugins.";
            String footer = "Plugin name fragment can be any part of the plugin name.\nPlease, escape white spaces.";
            PrintWriter writer = new PrintWriter(response, true);
            formatter.printHelp(writer, formatter.getWidth(),
                                "list [options] [plugin name fragment]",
                                header, opts, formatter.getLeftPadding(),
                                formatter.getDescPadding(), footer, true);
            return;
        }
        String pluginNameContains = null;
        if (line.getArgs() != null && line.getArgs().length > 0) {
            pluginNameContains = line.getArgs()[0]; //CLCommandHelper.getPluginName(line.getArgs());
        }

        // get Plugin name from input
        if (line.hasOption(libs.getOpt())) {
            Iterator<IPlugin> it = PluginManager.getInstance().getPlugins()
                                                .iterator();
            response.println("Plugin JAR-file locations:");
            while (it.hasNext()) {
                IPlugin next = it.next();
                if (skip(next, pluginNameContains)) {
                    continue;
                }

                response.println(next.getProperties().getURL());
            }
            response.println("Libraries JAR-file locations:");
            URL[] urls = PluginManager.getInstance().getLibs();
            for (URL url : urls) {
                response.println(url);
            }
            return;
        }
        List<IPlugin> list = PluginManager.getInstance().getPlugins();
        if (line.hasOption(ordered.getOpt())) {
            java.util.Collections.sort(list, new PluginNameComparator());
        }
        if (line.hasOption(extended.getOpt())) {
            try {
                Iterator<IPlugin> it = list.iterator();
                while (it.hasNext()) {
                    IPlugin plugin = it.next();
                    if (skip(plugin, pluginNameContains)) {
                        continue;
                    }

                    String versionstring = "";
                    if (plugin instanceof PluginAdapter) {
                        PluginAdapter pa = (PluginAdapter) plugin;
                        versionstring = pa.getVersion();
                    }
                    response.println(plugin.getProperties()
                                           .getFilteredProperty(".date")
                                     + "\t "
                                     + plugin.getProperties()
                                             .getFilteredProperty(".user")
                                     + "\t " + versionstring + "\t "
                                     + plugin.getName());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            } catch (NumberFormatException e) {
                response.println("cleanup canceled.");
            } catch (Exception e) {
                response.println("cleanup canceled: " + e + "; "
                                 + e.getMessage());
                logger.error(e.getMessage(), e);
            }
            return;
        }
        Iterator<IPlugin> it = list.iterator();
        while (it.hasNext()) {
            IPlugin plugin = it.next();
            if (skip(plugin, pluginNameContains)) {
                continue;
            }

            response.println(plugin);
        }
    }

    private boolean skip(IPlugin plugin, String searchText) {
        if (searchText == null || searchText.trim().length() == 0) {
            return false;
        }
        String search = searchText.trim().toLowerCase();
        if (plugin.getName() != null
                    && plugin.getName().toLowerCase().contains(search)) {
            return false;
        }
        if (plugin.getAlias() != null
                    && plugin.getAlias().toLowerCase().contains(search)) {
            return false;
        }
        return true;
    }

    public String getDescription() {
        return "lists all loaded plugins. Options: [-l| --long] for more information; "
               + "[-c| --comment] to show compile comments; "
               + "[-j|--jar] to show all JAR file locations;"
               + "[-o] to show ordered list.";
    }


    /**
     * Compares <code>IPlugin</code>s according to their nanes.
     * @author Lawrence Cabac
     *
     */
    public class PluginNameComparator implements Comparator<IPlugin> {
        public int compare(IPlugin o1, IPlugin o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();
            return name1.compareTo(name2);
        }
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "(--ordered|--long|--jar|--help) (--ordered|--long)";
    }
}