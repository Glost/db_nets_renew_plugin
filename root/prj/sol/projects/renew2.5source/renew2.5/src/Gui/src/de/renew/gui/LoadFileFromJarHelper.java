package de.renew.gui;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.InvocationTargetException;

import java.net.URISyntaxException;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.SwingUtilities;


public class LoadFileFromJarHelper {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LoadFileFromJarHelper.class);
    private static final String RNW_IMG_PROPERTY = "de.renew.jars.containing.rnw.files";
    private static String extensionUsed;


    /**
     * Search for image with name <b>fileName</b> in jar files (specified under: {@link LoadFileFromJarHelper#RNW_IMG_PROPERTY})
     * @param fileName [String]
     * @param extensions [String...] allowed resp. possible extendsions
     * @return NULL || {@link BufferedInputStream}
     */
    public static synchronized BufferedInputStream getImageFromJar(String fileName,
                                                                   String... extensions) {
        try {
            logger.trace("LoadFileFromJarHelper.getImageFromJar: FILENAME "
                         + fileName);
            extensionUsed = null;
            JarFile jarFile = getJarFileContainingFile(fileName, extensions);
            if (jarFile == null) {
                logger.warn("1. Image with name " + fileName + " not found");
                return null;
            }
            ZipEntry entry = jarFile.getEntry(fileName + "." + extensionUsed);
            if (entry == null) {
                logger.warn("2. Image with name " + fileName + " not found");
                return null;
            }
            InputStream stream = jarFile.getInputStream(entry);
            return new BufferedInputStream(stream);
        } catch (IOException e) {
            // do nothing
            logger.warn(e.getMessage());
        } catch (NullPointerException e) {
            // do nothing
            logger.warn(e.getMessage());
        }
        return null;
    }

    /**
     * Load rnw with given <b>name</b> from jar file. Possible jar files to search in should be specified under: {@link LoadFileFromJarHelper#RNW_IMG_PROPERTY}
     * @param name [String] Name of rnw (".rnw" not required)
     */
    public static synchronized void loadRnwFileFromJar(String name) {
        extensionUsed = null;
        Drawing newDrawing = null;
        InputStream stream = null;
        JarFile jarFile = null;
        if (name != null && name.endsWith(".rnw")) {
            name = name.replace(".rnw", "");
        }
        try {
            jarFile = getJarFileContainingFile(name, "rnw");
            ZipEntry entry = jarFile.getEntry(name + ".rnw");
            stream = jarFile.getInputStream(entry);
            newDrawing = DrawingFileHelper.loadDrawing(stream, name);
        } catch (Exception e) {
            logger.error("Failed to load drawing " + name, e);
        }

        if (newDrawing != null && newDrawing instanceof CPNDrawing) {
            final IOHelper ioHelper = DrawPlugin.getCurrent().getIOHelper();
            if (ioHelper != null && jarFile != null) {
                ZipEntry entry = jarFile.getEntry(name + ".rnw");
                InputStream tmp = null;
                try {
                    tmp = jarFile.getInputStream(entry);
                } catch (IOException e) {
                    logger.error("Failed to get InputStream for " + name
                                 + ".rnw from jar file", e);
                }
                final InputStream fstream = tmp;
                final String fName = name;
                if (SwingUtilities.isEventDispatchThread()) {
                    try {
                        ioHelper.loadAndOpenDrawing(fstream, fName);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        EventQueue.invokeAndWait(new Runnable() {
                                public void run() {
                                    try {
                                        ioHelper.loadAndOpenDrawing(fstream,
                                                                    fName);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    } catch (InterruptedException e) {
                        logger.trace("HelperGui.test : "
                                     + "Caught interrupt while loading drawing",
                                     e);
                    } catch (InvocationTargetException e) {
                        logger.trace("HelperGui.test : "
                                     + "Caught InvocationTargetException while loading drawing",
                                     e);
                    }
                }
            }
        }
    }

    /**
     * Get plugin with <b>pluginName</b>
     * @param pluginName [String]
     *
     * @return {@link File}
     */
    private static synchronized File findJarFile(String pluginName) {
        IPlugin plugin = PluginManager.getInstance().getPluginByName(pluginName);
        if (plugin != null) {
            try {
                logger.debug("FOUND jar file for pluginName " + pluginName);
                return new File(plugin.getProperties().getURL().toURI());
            } catch (URISyntaxException e) {
                logger.error("Error while creating File for plugin "
                             + pluginName + " of URL "
                             + plugin.getProperties().getURL(), e);
            }
        }
        logger.warn("No plugin found with name " + pluginName);
        return null;
    }

    /**
     * Get {@link JarFile} file which contains the <b>filename</b> with one of the given <b>extensions</b>.
     * @param filename [String]
     * @param extensions [String...]
     *
     * @return NULL | {@link JarFile}
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static synchronized JarFile getJarFileContainingFile(String filename,
                                                                 String... extensions)
            throws FileNotFoundException, IOException {
        if (extensions == null || extensions.length == 0) {
            throw new NullPointerException("No extension(s) given");
        }
        String property = PluginProperties.getUserProperties()
                                          .getProperty(RNW_IMG_PROPERTY);

        // MAPA / FIXME If RNW_IMG_PROPERTY is not set in .renew.properties, then no image will be displayed! 
        // To be changed so that plugin properties will be evaluated as well
        if (property == null) {
            logger.warn("Property " + RNW_IMG_PROPERTY + " not set!");
            return null;
        }
        String[] pluginNames = property.split(":");
        if (pluginNames == null || pluginNames.length == 0) {
            return null;
        }
        for (int i = 0; i < pluginNames.length; i++) {
            File jarFile = findJarFile(pluginNames[i]);
            if (jarFile == null) {
                continue;
            }
            JarFile jarRes = new JarFile(jarFile);
            ZipEntry entry = null;
            for (String extension : extensions) {
                entry = jarRes.getEntry(filename + "." + extension);
                if (entry != null) {
                    extensionUsed = extension;
                    break;
                }
            }
            if (entry != null) {
                logger.debug("Found ZIPEntry which contains file " + filename);
                return jarRes;
            }
        }
        throw new FileNotFoundException("File " + filename
                                        + " not found in any of the given plugins "
                                        + property);
    }
}