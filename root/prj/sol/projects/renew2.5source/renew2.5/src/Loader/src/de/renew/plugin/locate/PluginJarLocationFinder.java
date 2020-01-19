package de.renew.plugin.locate;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
 * This class is used to find the jar files in a directory
 * that contain a "plugin.cfg" entry.
 *
 * This is used to create the resulting PluginProperties object.
 *
 */
public class PluginJarLocationFinder extends PluginFileFinder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginJarLocationFinder.class);

    public PluginJarLocationFinder(URL basedir) {
        super(basedir);
    }

    protected File[] getPluginFiles(File dir) {
        File[] plugins = dir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    String name = file.getName();
                    return file.isFile() && name.endsWith(".jar");
                }
            });
        return plugins;
    }

    protected Collection<PluginProperties> getPluginConfigurations(File[] plugins) {
        // convert to urls so URLClassloader can be used
        Vector<PluginProperties> pluginURLv = new Vector<PluginProperties>();
        for (int i = 0; i < plugins.length; i++) {
            PluginProperties cfg = createPluginConfig(plugins[i]);
            if (cfg != null) {
                pluginURLv.add(cfg);
            }
        }
        return pluginURLv;
    }

    protected PluginProperties createPluginConfig(File loc) {
        if (!loc.exists()) {
            logger.debug("jar file " + loc + " does not exist.");
            return null;
        }
        logger.debug("looking for plugin.cfg in jar file " + loc + "...");

        try {
            URL url = loc.toURI().toURL();
            PluginProperties props = null;
            JarFile jarFile = new JarFile(loc);
            ZipEntry entry = jarFile.getEntry("plugin.cfg");
            if (entry != null) {
                InputStream stream = jarFile.getInputStream(entry);
                props = new PluginProperties(url, stream);
                logger.debug("zip entry " + entry + " found in "
                             + jarFile.getName());
            } else {
                logger.debug("no zip entry found in " + jarFile.getName());
            }
            return props;
        } catch (MalformedURLException e) {
            logger.error("PluginJarLocationFinder: could not create URL from "
                         + loc);
        } catch (Exception e) {
            logger.error("PluginJarLocationFinder: " + e);
        }
        return null;
    }

    public static void main(String[] args) {
        PluginProperties.getUserProperties().setProperty("debug", "false");
        PluginJarLocationFinder finder = new PluginJarLocationFinder(null);
        Collection<PluginProperties> found = finder.findPluginLocations();
        System.out.println("found the following locations:");
        System.out.println(CollectionLister.toString(found));
    }
}