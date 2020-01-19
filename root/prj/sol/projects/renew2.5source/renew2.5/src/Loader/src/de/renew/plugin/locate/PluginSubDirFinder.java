package de.renew.plugin.locate;

import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Vector;


/**
 * This class implements the PluginLocationFinder interface.
 * It searches Plugin configurations based on a given directory URL <i>u</i>
 * by searching all of the subdirectories for a file "plugin.cfg".
 *
 * The URL contained in the resulting PluginProperties object will
 * be a file URL to the found plugin.cfg file.
 *
 * This file must be a properties file as described in the java.util.Property documentation.
 * Note: The base url <i>u</u> itself is NOT searched!
 */
public class PluginSubDirFinder extends PluginFileFinder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginSubDirFinder.class);

    public PluginSubDirFinder(URL base) {
        super(base);
    }

    /**
     * return a list of directories contained in the gived directory.
     */
    protected File[] getPluginFiles(File dir) {
        File[] plugins = dir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
        return plugins;
    }

    protected Collection<PluginProperties> getPluginConfigurations(File[] plugins) {
        // convert to urls so URLClassloader can be used
        Vector<PluginProperties> pluginURLv = new Vector<PluginProperties>();
        for (int i = 0; i < plugins.length; i++) {
            PluginProperties cfg = getPluginConfigfromDirectory(plugins[i]);
            if (cfg != null) {
                pluginURLv.add(cfg);
            }
        }
        return pluginURLv;
    }

    /**
     * Creates a PluginConfiguration object from the given directory.
     * This is done by looking in for a plugin.cfg file which is then
     * used to construct the result object.
     */
    protected PluginProperties getPluginConfigfromDirectory(File location) {
        if (location.isDirectory()) {
            File[] cfgFiles = location.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String file) {
                        return file.equals("plugin.cfg");
                    }
                });
            File loc = null;
            if (cfgFiles.length > 0) {
                loc = cfgFiles[0];
                if (cfgFiles.length > 1) {
                    logger.warn("PluginSubDirFinder: " + location
                                + " contains more than one "
                                + "plugin.cfg file! using only " + location);
                }

                //					logger.debug ("added to class loader: " + jarFile.toURL());
            } else {
                logger.debug("PluginSubDirFinder: no plugin.cfg found in "
                             + location);
            }

            if (loc != null) {
                return createPluginConfig(loc);
            }
        }
        return null;
    }

    protected PluginProperties createPluginConfig(File loc) {
        try {
            URL url = loc.toURI().toURL();
            FileInputStream stream = new FileInputStream(loc);

            // load setting from the given file 
            PluginProperties props = new PluginProperties(url, stream);
            return props;
        } catch (MalformedURLException e) {
            logger.error("PluginSubDirFinder: could not create URL from " + loc
                         + ": " + e);
        } catch (Exception e) {
            logger.error("PluginSubDirFinder: " + e);
        }
        return null;
    }

    private static void list(Collection<?> l) {
        for (Object o : l) {
            System.out.println(o);
        }
    }

    public static void main(String[] args) {
        Collection<PluginProperties> found = new PluginSubDirFinder(null)
                                                 .findPluginLocations();
        System.out.println("found the following locations:");
        list(found);
    }
}