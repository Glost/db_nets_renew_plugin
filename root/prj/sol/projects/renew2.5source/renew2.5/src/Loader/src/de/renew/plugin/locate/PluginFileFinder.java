package de.renew.plugin.locate;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.PluginProperties;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Collection;
import java.util.Vector;


/**
 * This class, given an URL, creates a list of Files where plugins may be located.
 * It provides a check if the given URL is a directory and whether it exists;
 * subclasses must implement the methods getPluginFiles() and getConfigsFromDirectory().
 */
public abstract class PluginFileFinder implements PluginLocationFinder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginFileFinder.class);
    private URL _pluginBase;

    /**
     * The URL is supposed to be a local file.
     * If null is given, the present directory is used as a default.
     */
    public PluginFileFinder(URL basedir) {
        if (basedir == null) {
            try {
                basedir = new File(System.getProperty("user.dir")).toURI()
                                                                  .toURL();
            } catch (MalformedURLException e) {
                logger.error("PluginFileFinder: now, that IS strange.");
            }
        }
        _pluginBase = basedir;
    }

    protected URL getPluginBase() {
        return _pluginBase;
    }

    public String toString() {
        return "a PluginFileFinder looking below " + _pluginBase;
    }

    /**
     * Returns a Collection which contains PluginProperties.
     * It searches in the local file system:
     * the subdirectories of the base given to the constructor
     * are searched for configurations (by calling the getPluginConfigurations()
     * method that needs to by implemented by subclasses).
     */
    public Collection<PluginProperties> findPluginLocations() {
        Collection<PluginProperties> urls = new Vector<PluginProperties>();

        try {
            // use the given url to look for plugin directories
            URL url = getPluginBase();
            logger.debug(getClass().getName() + ": plugin base is "
                         + url.toExternalForm());
            File dir = convert(url, true);
            if (dir == null) {
                // Error message has already been printed by convert().
                return new Vector<PluginProperties>();
            }

            if (!dir.exists()) {
                logger.error("plugin directory " + dir + " not found.");
                return new Vector<PluginProperties>();
            }

            if (!dir.isDirectory()) {
                return new Vector<PluginProperties>();
            }


            // collect list of all directories (where the plugins
            // should be located)
            File[] plugins = getPluginFiles(dir);

            urls = getPluginConfigurations(plugins);

            logger.debug(urls.size() + " plugin locations found.");
            logger.debug(CollectionLister.toString(urls));
        } catch (RuntimeException e) {
            logger.error("something went wrong looking for a plugin location.");
        }
        return urls;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>url</code> is converted into a <code>File</code> object,
     * if possible.  The file is then checked against the usual rules of
     * this finder, if it denotes a plugin.  Relative file URLs are
     * resolved against this finder's base directory.  However, an absolute
     * <code>url</code> is allowed point anywhere.
     * </p>
     **/
    public PluginProperties checkPluginLocation(URL url) {
        if (url == null || (!"file".equals(url.getProtocol()))) {
            return null;
        }
        URL resolved;
        try {
            resolved = new URL(_pluginBase, url.getPath());
        } catch (MalformedURLException e) {
            logger.error("PluginFileFinder: Strange URL \"" + url + "\": " + e,
                         e);
            resolved = url;
        }
        File asFile = convert(resolved, false);
        if (asFile != null) {
            Collection<PluginProperties> coll = getPluginConfigurations(new File[] { asFile });
            if (!coll.isEmpty()) {
                return coll.iterator().next();
            }
        }
        return null;
    }

    private File convert(URL url, boolean verbose) {
        File asFile = null;
        try {
            asFile = new File(new URI(url.toExternalForm()));
        } catch (URISyntaxException e) {
            if (verbose) {
                logger.error("PluginFileFinder: Strange URL \"" + url + "\": "
                             + e, e);
            }
            logger.debug("PluginFileFinder: Strange URL \"" + url + "\": " + e,
                         e);
        } catch (IllegalArgumentException e) {
            if (verbose) {
                logger.error("PluginFileFinder: Not a file? URL \"" + url
                             + "\": " + e.getMessage(), e);
            }
            logger.debug("PluginFileFinder: Not a file? URL \"" + url + "\": "
                         + e.getMessage(), e);
        } catch (NullPointerException e) {
            if (verbose) {
                logger.error("PluginFileFinder: " + e, e);
            }
            logger.debug("PluginFileFinder: " + e, e);
        }
        return asFile;
    }

    /**
     * This method must be implemented by subclasses so that a list of files
     * is created based on the given file (which is guaranteed to be a directory).
     * This file list will later be the argument to the getConfigsFromDirectories()
     * method.
     *
     * @param dir the base directory where to look.
     * @return a list of files in which a plugin configuration may be found.
     */
    protected abstract File[] getPluginFiles(File dir);

    /**
     * This method is used to create plugin configuration objects
     * (of type PluginProperties).
     * @return a Collection containing instances of PluginProperties
     */
    protected abstract Collection<PluginProperties> getPluginConfigurations(File[] fileList);
}