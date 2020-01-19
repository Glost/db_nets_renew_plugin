package de.renew.plugin.command;

import jline.console.ConsoleReader;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;


/**
 * This command loads a plugin from the given URL. If the argument "-v" is
 * given, verbose output is printed.
 *
 * @author Jörn Schumacher
 * @author Michael Simon
 */
public class LoadCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(LoadCommand.class);

    public void execute(String[] args, PrintStream response) {
        if (args.length != 1 || "h".equals(args[0]) || "-h".equals(args[0])
                    || "--help".equals(args[0]) || "--h".equals(args[0])
                    || "-help".equals(args[0])) {
            response.println("usage: load <url>\n" + "examples: \n"
                             + "load file:/path/to/location/name/plugin.cfg\n"
                             + "load file:/path/to/location/pluginname.jar\n"
                             + "load pluginname.jar\n" + "load pluginname*");
        } else {
            URL url = null;
            File parameterFile = null;
            final String parameter = args[0];

            response.println("\n\n------- LOAD -------\n\n");

            if (!parameter.endsWith("*")) {
                try {
                    // Try to interpret the parameter as an URL:
                    URL tmpURL = new URL(parameter);
                    parameterFile = getFile(tmpURL);
                    if (parameterFile == null) {
                        // The URL does not point to a local file.
                        // Use it directly:
                        url = tmpURL;
                    }
                } catch (MalformedURLException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(LoadCommand.class.getSimpleName()
                                     + ": Ignoring exception: " + e);
                    }
                    // The parameter could not be interpreted as an URL.
                    // Treat it as a file path instead:
                    parameterFile = new File(parameter);
                }
            }

            // Is the URL already determined?
            if (url != null) {
                // Yes, because it does not point to a local file.
                // Is it already loaded?
                if (isLoaded(url, response)) {
                    // Abort.
                    url = null;
                }
            } else {
                // No. Determine the right URL...
                try {
                    if (parameterFile != null && parameterFile.isAbsolute()
                                && parameterFile.exists()) {
                        // The file exists and is a file (not a directory).
                        // Is it already loaded?
                        if (!isLoaded(parameterFile, response)) {
                            // Not loaded.
                            url = createURL(parameterFile);
                            if (logger.isDebugEnabled()) {
                                logger.debug(LoadCommand.class.getSimpleName()
                                             + ": directly found URL: " + url);
                            }
                        }

                        // Otherwise it is already loaded: abort.
                    } else {
                        // The file does not exist directly.
                        // Search for it in the plug-in locations...
                        String pluginLocationProp = PluginProperties.getUserProperties()
                                                                    .getProperty(PluginManager.PLUGIN_LOCATIONS_PROPERTY);
                        ArrayList<File> pluginLocations = new ArrayList<File>();

                        if (pluginLocationProp != null) {
                            String[] pluginLocationNames = pluginLocationProp
                                                               .split(File.pathSeparator);
                            for (String string : pluginLocationNames) {
                                pluginLocations.add(new File(string)
                                    .getCanonicalFile());
                            }
                        }
                        File loaderLocation = new File(new File(PluginManager.getLoaderLocation()
                                                                             .toURI()),
                                                       "plugins")
                                                  .getCanonicalFile();
                        if (!pluginLocations.contains(loaderLocation)) {
                            pluginLocations.add(loaderLocation);
                        }
                        ArrayList<File> matches = new ArrayList<File>();
                        int length = 0;
                        for (File pluginLocation : pluginLocations) {
                            logger.debug(LoadCommand.class.getSimpleName()
                                         + ": Plugin Location found: "
                                         + pluginLocation.getAbsolutePath());
                            if (pluginLocation.exists()) {
                                // Was the argument given without a trailing '*'?
                                if (parameterFile != null) {
                                    // Yes. Search for it directly in the plug-in locations.
                                    File file = new File(pluginLocation,
                                                         parameterFile.getPath());
                                    if (file.exists()) {
                                        // Compile the results from all plug-in locations.
                                        matches.add(file);
                                    }
                                } else {
                                    // No. Compile possible candidates...
                                    File[] files = pluginLocation.listFiles();
                                    if (files != null) {
                                        for (int i = 0; i < files.length;
                                                     i++) {
                                            if (!files[i].isDirectory()
                                                        && files[i].getName()
                                                                           .toLowerCase()
                                                                           .startsWith(parameter.toLowerCase()
                                                                                                        .replace("*",
                                                                                                                         ""))) {
                                                logger.debug(LoadCommand.class
                                                    .getSimpleName()
                                                             + ": Adding file to mached plugin list: "
                                                             + files[i]
                                                    .getAbsolutePath());
                                                if (!matches.contains(files[i])) {
                                                    matches.add(files[i]);
                                                }
                                                length = Math.max(length,
                                                                  files[i].getAbsolutePath()
                                                                          .length());
                                            }
                                            if (files[i].isDirectory()
                                                        && files[i].getName()
                                                                           .toLowerCase()
                                                                           .startsWith(parameter.toLowerCase()
                                                                                                        .replace("*",
                                                                                                                         ""))) {
                                                logger.debug(LoadCommand.class
                                                    .getSimpleName()
                                                             + ": Adding plugin.cfg file to mached directory plugin list: "
                                                             + files[i]
                                                    .getAbsolutePath());
                                                File tmp = new File(files[i],
                                                                    "plugin.cfg");
                                                if (!matches.contains(tmp)) {
                                                    matches.add(tmp);
                                                }
                                                length = Math.max(length,
                                                                  files[i].getAbsolutePath()
                                                                          .length());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (matches.size() > 1) {
                            // Multiple matches.

                            // Let the user select a file:
                            int choice = selectFile(response, matches, args[0],
                                                    length);
                            if (choice >= 0) {
                                url = createURL(matches.get(choice));
                            } else {
                                // Aborted by user.
                                return;
                            }
                        } else if (matches.size() == 1) {
                            // Single match.
                            final File file = matches.get(0);

                            // Is the plug-in already loaded?
                            if (!isLoaded(file, response)) {
                                // Not loaded.
                                url = createURL(file);
                            }

                            // Otherwise it is already loaded: abort.
                        } else {
                            // No match.
                            response.println("No match found for " + args[0]
                                             + ".");
                        }
                    }
                } catch (Exception e) {
                    response.println("load failed: " + e);
                    logger.error(e.getMessage(), e);
                    url = null;
                }
            }

            if (url == null) {
                response.println("Stop command 'load " + args[0] + "'.");
                return;
            }

            logger.debug(LoadCommand.class.getSimpleName()
                         + ": Trying to load plugin from URL: " + url);

            IPlugin plugin = PluginManager.getInstance().loadPlugin(url);
            if (plugin != null) {
                response.println("\nPlug-in: \t" + plugin.getName()
                                 + "\t successfully loaded.\n");
            } else {
                response.println("Plug-in (" + url
                                 + ") not loaded. See log messages for details.");
            }
        }
    }

    private static URL createURL(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }

    /**
     * If the {@link URL} points to a local file, return it.
     * @return the local file or {@code null}, if there is none
     */
    private static File getFile(URL url) {
        if ("file".equals(url.getProtocol())) {
            // The URL points to a local file.
            return new File(url.getFile());
        } else {
            return null;
        }
    }

    private int selectFile(PrintStream out, ArrayList<File> matches,
                           String prefix, int length) {
        out.println("The following plug-ins were found for " + prefix + " : ");
        out.println("");
        String tmp = "";
        int count = 0;
        while (tmp.length() < length && count < 500) {
            tmp += " ";
            count++;
        }
        tmp += "  ";
        String title = " Selection   Path " + tmp + " Status ";
        out.println(title);
        String line = "";
        int countLine = 0;
        while (line.length() < title.length() && countLine < 500) {
            line += "-";
            countLine++;
        }
        out.println(line);
        out.println("");
        for (int i = 0; i < matches.size(); i++) {
            final File file = matches.get(i);
            String absolutePath = file.getAbsolutePath();
            String status = isLoaded(file) ? "loaded" : "  --";
            String space = "";
            int countSpace = 0;
            int max = length - absolutePath.length() + 4;
            while (space.length() < max && countSpace < 100) {
                space += " ";
                countSpace++;
            }
            space += "  ";
            out.println(" " + (i + 1) + "           " + absolutePath + " "
                        + space + " " + status);
        }
        out.println("");
        out.print("Press enter to stop loading plug-in, or type selection number: ");
        String selection = null;
        String stop = "Stop  command 'load " + prefix + "'.";
        try {
            ConsoleReader in = new ConsoleReader();
            selection = in.readLine();
        } catch (IOException e) {
            out.println("Could not load selected plug-in due to exception : "
                        + e + ".");
            out.println(stop);
            return -1;
        }
        if (selection != null && selection.trim().length() > 0) {
            selection = selection.trim();
            try {
                int choice = Integer.parseInt(selection) - 1;
                if (choice < 0 || choice >= matches.size()) {
                    out.println("Selection " + selection + " not known.");
                    out.println(stop);
                    return -1;
                } else {
                    final File file = matches.get(choice);
                    if (file == null) {
                        out.println("Selection " + choice + " not known");
                        out.println(stop);
                        return -1;
                    } else if (isLoaded(file, out)) {
                        out.println(stop);
                        return -1;
                    } else {
                        return choice;
                    }
                }
            } catch (Exception e) {
                out.println("Selection " + selection + " not known.");
                out.println(stop);
                return -1;
            }
        }
        out.println("No selection made.");
        out.println(stop);
        return -1;
    }

    /**
     * Whether the plug-in at {@code address} is already loaded.
     *
     * @param address must be a {@link File} or {@link URL} instance
     * @return whether the plug-in is already loaded
     *
     * @author Michael Simon
     */
    private static boolean isLoaded(Object address) {
        return getPlugin(address) != null;
    }

    /**
     * Returns the same as {@link #isLoaded(Object)}, but writes a notification to {@code ps},
     * if the plug-in is already loaded.
     *
     * @param address must be a {@link File} or {@link URL} instance
     * @param ps the stream to write the notification to
     * @return whether the plug-in is already loaded
     *
     * @author Michael Simon
     */
    private static boolean isLoaded(Object address, PrintStream ps) {
        IPlugin plugin = getPlugin(address);

        String addressName;
        if (address instanceof File) {
            final File file = (File) address;
            addressName = file.getName();
            if ("plugin.cfg".equals(addressName)) {
                addressName = file.getParentFile().getName();
            }
        } else {
            addressName = address.toString();
        }

        if (plugin != null) {
            ps.println("\n\tPlug-in \t" + plugin.getName() + " [" + addressName
                       + "]\t already loaded.\n");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Find a plug-in with the same path or URL as the given file.
     *
     * @param address must be a {@link File} or {@link URL} instance
     * @author Michael Simon
     */
    private static IPlugin getPlugin(Object address) {
        if (address instanceof File) {
            return getPlugin((File) address);
        } else if (address instanceof URL) {
            return getPlugin((URL) address);
        } else {
            throw new IllegalArgumentException(address + " is neither a "
                                               + File.class.getSimpleName()
                                               + " nor a "
                                               + URL.class.getSimpleName()
                                               + " instance.");
        }
    }

    /**
     * Find a plug-in with the same path as the given file.
     *
     * @author Eva Mueller
     * @author Michael Simon
     */
    private static IPlugin getPlugin(File file) {
        try {
            final File cf = file.getCanonicalFile();
            for (IPlugin plugin : PluginManager.getInstance().getPlugins()) {
                // Test if the plug-in's file equals the given file.
                try {
                    final File pf = getFile(plugin.getProperties().getURL());
                    if (pf != null) {
                        File cpf = pf.getCanonicalFile();
                        if ( // Compare the given path with the plug-in URL's path:
                                cf.equals(cpf)
                                // Also compare the given path with the plug-in's parent directory:
                                || (cf.equals(cpf.getParentFile())
                                           && cpf.isFile())
                                // And the plug-in's path with the given path's parent directory:
                                || (cpf.equals(cf.getParentFile())
                                           && cf.isFile())) {
                            // This covers all cases, because the plug-in's URL is either a file,
                            // or its parent directory and the same it true for the given path.
                            return plugin;
                        }
                    }
                } catch (IOException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(LoadCommand.class.getSimpleName()
                                     + ": Ignoring exception while comparing the given file with "
                                     + plugin.getProperties().getURL().getFile()
                                     + ": " + e);
                    }
                }
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(LoadCommand.class.getSimpleName()
                             + ": Aborting the comparison of " + file
                             + " with the plug-in URLs because of: " + e);
            }
        }

        // If no plug-in for the given file was found.
        return null;
    }

    /**
     * Find a plug-in with the same URL as the given URL.
     *
     * @author Michael Simon
     */
    private static IPlugin getPlugin(URL url) {
        for (IPlugin plugin : PluginManager.getInstance().getPlugins()) {
            if (url.sameFile(plugin.getProperties().getURL())) {
                return plugin;
            }
        }

        // If no plug-in for the given file was found.
        return null;
    }

    public String getDescription() {
        return "Load a new plug-in. Type 'load -help' to get examples of usage.";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "locationNames";
    }
}