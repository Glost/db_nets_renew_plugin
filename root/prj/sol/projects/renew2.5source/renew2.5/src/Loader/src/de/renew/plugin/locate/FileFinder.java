package de.renew.plugin.locate;

import org.apache.log4j.Logger;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
 * This is a common class for all plug-ins to search for files in jars and directories.
 * @author Eva Mueller
 * @date Nov 14, 2010
 * @version 0.1
 */
public class FileFinder {
    private static Logger logger = Logger.getLogger(FileFinder.class);

    /**
     * Search for the given <b>netName</b> in all plug-ins providing the<br>
     * boolean property <i>option.include.rnws = true</i> in their etc/plugin.cfg file.<br><br>
     *
     * <span style="color: red;">Note:</span> the property <i>option.include.rnws</i><br>
     * is set automatically during the build process<br>
    * iff the ant property <i>option.include.rnws</i> is set.
     *
     * @param netName [String] Name of the net to search for (extension .rnw will be added by this method)
     * @return {@link InputStream}
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public static InputStream searchForRnwFileInJars(String netName) {
        String netNameTmp = getContent(netName);
        if (netNameTmp == null) {
            logger.warn("FileFinder.searchForRnwFileInJars : No net name set.");
            return null;
        }
        List<IPlugin> plugins = PluginManager.getInstance().getPlugins();
        for (IPlugin iPlugin : plugins) {
            PluginProperties properties = iPlugin.getProperties();
            if (!properties.getBoolProperty("option.include.rnws")) {
                continue;
            }
            InputStream is = searchForFileInJar(iPlugin, netNameTmp, "rnw");
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    /**
     * Search for the given <b>fileName</b> with the given <b>extension</b> in all plug-ins.
     *
     * @param fileName [String] Name of the file to search for
     * @param extension [String] Extension the file has (dot (.) not necessary)
     * @return {@link InputStream}
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public static InputStream searchForFileInJars(String fileName,
                                                  String extension) {
        String fileNameTmp = getContent(fileName);
        String extensionTmp = getContent(extension);
        if (fileNameTmp == null || extensionTmp == null) {
            if (fileNameTmp == null) {
                logger.warn("FileFinder.searchForFileInJars : No file name set.");
            }
            if (extensionTmp == null) {
                logger.warn("FileFinder.searchForFileInJars : No extension set.");
            }
            return null;
        }
        List<IPlugin> plugins = PluginManager.getInstance().getPlugins();
        for (IPlugin iPlugin : plugins) {
            InputStream is = searchForFileInJar(iPlugin, fileNameTmp,
                                                extensionTmp);
            if (is != null) {
                return is;
            }
        }
        return null;
    }

    /**
     * Get {@link InputStream} from jar file located at given <b>location</b><br>
     * for given <b>fileName</b>.
     * @param location [{@link URL}] URL of jar file
     * @param fileName [String] Absolute path of file within jar file
     * @return {@link InputStream}
     *
     * @author Eva Mueller
     * @date Nov 27, 2010
     * @version 0.1
     */
    public static InputStream getFileFromJarAsStream(URL location,
                                                     String fileName) {
        try {
            JarFile jarRes = new JarFile(new File(location.toURI()));
            ZipEntry entry = jarRes.getEntry(fileName);
            return entry != null ? jarRes.getInputStream(entry) : null;
        } catch (IOException e) {
            logger.error(e);
        } catch (URISyntaxException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Search for the given <b>fileName</b> with the given <b>extension</b> in the<br>
     * given <b>iPlugin</b>.
     *
     * @param iPlugin [{@link IPlugin}] The plug-in in which the search is executed.
     * @param fileName [String] Name of the file to search for
     * @param extension [String] Extension the file has (dot (.) not necessary)
     * @return {@link InputStream}
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    public static InputStream searchForFileInJar(IPlugin iPlugin,
                                                 String fileName,
                                                 String extension) {
        String fileNameTmp = getContent(fileName);
        String extensionTmp = getContent(extension);
        if (iPlugin == null || fileNameTmp == null || extensionTmp == null) {
            if (iPlugin == null) {
                logger.warn("FileFinder.searchForFileInJar : No plug-in set.");
            }
            if (fileNameTmp == null) {
                logger.warn("FileFinder.searchForFileInJar : No file name set.");
            }
            if (extensionTmp == null) {
                logger.warn("FileFinder.searchForFileInJar : No extension set.");
            }
            return null;
        }
        if (extensionTmp.startsWith(".")) {
            extensionTmp = extensionTmp.substring(1, extensionTmp.length());
        }
        if (fileNameTmp.endsWith("." + extensionTmp)) {
            fileNameTmp = fileNameTmp.replace("." + extensionTmp, "");
        }
        JarFile jarRes = null;
        try {
            jarRes = new JarFile(new File(iPlugin.getProperties().getURL()
                                                 .toURI()));
            ZipEntry entry = jarRes.getEntry(fileNameTmp + "." + extensionTmp);
            return entry != null ? jarRes.getInputStream(entry) : null;
        } catch (IOException e) {
            logger.error("FileFinder.searchForFileInJar : IOException ", e);
        } catch (URISyntaxException e) {
            logger.error("FileFinder.searchForFileInJar : URISyntaxException ",
                         e);
        }
        return null;
    }

    /**
    * Search for a file with the given <b>fileName</b><br>
    * relative to the given <b>classLoader</b> and <b>path</b>.<br>
    *
    * @param path [String] the /-separated path in which the class loader should search for.
    * @param filename [String] Complete file name (<span style="color: red;">Please provide extension!</span>).
    * @param classLoader [{@link ClassLoader}]
    *
    * @return [String] A string representation of the file's URL,<br>
    * or <code>null</code>, if no file with the given name was found.
    */
    public static String getURLforFile(String path, String fileName,
                                       ClassLoader classLoader) {
        String pathTmp = getContent(path);
        String fileNameTmp = getContent(fileName);
        if (fileNameTmp == null || classLoader == null) {
            if (fileNameTmp == null) {
                logger.warn("FileFinder.getURLforFile : No file name set.");
            }
            if (classLoader == null) {
                logger.warn("FileFinder.getURLforFile : No class loader set.");
            }
            return null;
        }
        if (pathTmp != null && !pathTmp.endsWith("/")) {
            pathTmp += "/";
        }
        if (pathTmp == null) {
            pathTmp = "";
        }

        logger.debug("FileFinder.getURLforFile: looking for " + pathTmp
                     + fileNameTmp + " ... ");
        URL url = classLoader.getResource(pathTmp + fileNameTmp);
        if (url != null) {
            logger.debug("FileFinder.getURLforFile: found " + url);
            return url.toString();
        }
        logger.warn("FileFinder.getURLforFile: Missing file. Could not locate "
                    + pathTmp + fileNameTmp);
        return null;
    }

    /**
     * Search for a file with the given <b>fileName</b><br>
     * relative to the given <b>classLoader</b> and <b>paths</b>.<br>
     *
     *
     * @param path [String[]] Array of /-separated paths in which the class loader should search for.
     * @param filename [String] Complete file name (<span style="color: red;">Please provide extension!</span>).
     * @param classLoader [{@link ClassLoader}]
     *
     * @return [List&lt;String&gt;] A list of the string representations of the file's URLs
     */
    public static List<String> getURLsforFile(String[] paths, String fileName,
                                              ClassLoader classLoader) {
        List<String> urls = new ArrayList<String>();
        String fileNameTmp = getContent(fileName);
        if (fileNameTmp == null || classLoader == null) {
            if (fileNameTmp == null) {
                logger.warn("FileFinder.getURLsforFile : No file name set.");
            }
            if (classLoader == null) {
                logger.warn("FileFinder.getURLsforFile : No class loader set.");
            }
            return urls;
        }
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                String url = getURLforFile(paths[i], fileNameTmp, classLoader);
                if (url != null) {
                    urls.add(url);
                }
            }
        } else {
            String url = getURLforFile(null, fileNameTmp, classLoader);
            if (url != null) {
                urls.add(url);
            }
        }
        return urls;
    }

    /**
     * Get effective content of given <b>string</b>.<br>
     * <span style="color: red;">Note</span>: If given <b>string</b> is an empty String<br>
     * then {@code null} will be returned.
     *
     * @param string [String]
     * @return trimmed String or {@code null} is string is empty or null.
     *
     * @author Eva Mueller
     * @date Nov 14, 2010
     * @version 0.1
     */
    private static String getContent(String string) {
        if (string != null && string.trim().length() > 0) {
            return string.trim();
        }
        return null;
    }
}