package de.renew.util;

import org.apache.log4j.Logger;

import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * This class helps finding files.  It looks for matching extensions.
 * It can also look into jar files and will return the apropriate URIs.
 *
 * @author Benjamin Schleinzer (former FileFinder-based matching)
 * @author Dominic Dibbern (current extension-based matching)
 * @author Michael Duvigneau (documentation)
 */
public class FileFinder {
    protected static final Logger logger = Logger.getLogger(de.renew.util.FileFinder.class);

    /**
     * Recursively traverses all configured plugin locations and collects
     * all files whose file name ends with the given extension.
     * The search descends into directories and jar files.
     *
     * @param extension  the file name extension to look for.  It
     *                   is recommended to include the dot to avoid
     *                   extension substring matches.
     * @return list of matches.  When no matches are found, returns
     *   an empty list.
     *
     * @see PluginManager.PLUGIN_LOCATIONS_PROPERTY
     */
    public static ArrayList<URI> searchPluginLocations(String extension) {
        String pluginlocation = PluginProperties.getUserProperties()
                                                .getProperty(PluginManager.PLUGIN_LOCATIONS_PROPERTY);
        String[] locations = pluginlocation.split(File.pathSeparator);
        ArrayList<URI> fileList = new ArrayList<URI>();
        for (String location : locations) {
            File tmp = new File(location);
            if (!tmp.exists()) {
                logger.warn("\n\n\t ===> The specified plugin location ["
                            + location
                            + "] does not exist! (maybe specified with .renew.properties or start.sh or ...)\n\n");
                continue;
            }
            ArrayList<URI> searchResult = searchLocation(tmp, "." + extension);
            logger.debug("Search result for location " + location + ": "
                         + searchResult);
            fileList.addAll(searchResult);
        }
        return fileList;
    }

    /**
     * Recursively traverses the given search path and collects
     * all files whose file name ends with the given extension.
     * The search descends into directories and jar files.
     * If a plain file is given as starting location, only that
     * file name is checked whether it matches the extension.
     *
     * @param searchPath  the location where to start the search.
     * @param extension   the file name extension to look for.  It
     *                    is recommended to include the dot to avoid
     *                    extension substring matches.
     * @return list of matches.  When no matches are found, returns
     *   an empty list.
     */
    public static ArrayList<URI> searchLocation(File searchPath,
                                                String extension) {
        ArrayList<URI> fileList = new ArrayList<URI>();
        for (File entry : searchPath.listFiles()) {
            if (entry.isDirectory()) {
                fileList.addAll(searchLocation(entry, extension));
            } else if (entry.getName().endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(entry);
                    Enumeration<JarEntry> en = jar.entries();
                    while (en.hasMoreElements()) {
                        JarEntry jarEntry = en.nextElement();
                        if (jarEntry.getName().endsWith(extension)) {
                            URI jarURI = URI.create("jar:" + entry.toURI()
                                                    + "!/" + jarEntry);
                            fileList.add(jarURI);
                        }
                    }
                    jar.close();
                } catch (IOException e) {
                    logger.error("Cannot open JAR File: " + entry);
                    logger.debug(e);
                }
            } else if (entry.getName().endsWith(extension)) {
                fileList.add(entry.toURI());
            }
        }
        return fileList;
    }

    /**
     * Recursively traverses all configured plugin locations and collects
     * all files whose file name matches the given name.
     * The search descends into directories and jar files.
     *
     * @param filename  the file name to look for.  It is matched against
     *                  the end of URIs matching the same extension as the
     *                  given file name.
     * @return the first matches.  When no matches are found, returns
     *   <code>null</code>.
     *
     * @see PluginManager.PLUGIN_LOCATIONS_PROPERTY
     */
    public static URI searchFile(String filename) {
        // To be honest, the approach seems a bit overcomplicated.
        // Why first search for matching extensions (risking a failure if the
        // specified name has no extension) when the matching condition
        // is the same (endsWith())?
        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1);
        ArrayList<URI> fileList = searchPluginLocations(fileExtension);
        for (URI uri : fileList) {
            if (uri.toString().endsWith(filename)) {
                return uri;
            }
        }
        return null;
    }
}