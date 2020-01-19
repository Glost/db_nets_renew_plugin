package de.renew.plugin.load;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginClassLoader;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.annotations.Provides;
import de.renew.plugin.di.FactoryDefinition;
import de.renew.plugin.di.ServiceContainer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-11
 */
public abstract class AbstractPluginLoader implements PluginLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimplePluginLoader.class);
    protected final PluginClassLoader loader;
    protected final ServiceContainer container;

    public AbstractPluginLoader(PluginClassLoader loader,
                                ServiceContainer container) {
        this.loader = loader;
        this.container = container;
    }

    /**
     * Retrieves all JAR urls from a directory specified by an URL.
     *
     * @param url URL specifying the directory.
     * @return An array of URLs referencing JARs.
     */
    public static URL[] unifyURL(URL url) {
        File dir = null;
        try {
            dir = new File(url.toURI());
        } catch (Exception e) {
            logger.warn("Unable to search for JAR files in " + url + ": " + e);
        }

        URL[] jarURLs = getURLsFromDirectory(dir);
        if (jarURLs == null || jarURLs.length == 0) {
            logger.warn("No JAR found in " + url + ", resorting to given URL.");
            return new URL[] { url };
        }

        return jarURLs;
    }

    private static URL[] getURLsFromDirectory(File location) {
        // changed (06.05.2004) to support more than one jar in a plugin folder
        if (location == null) {
            return null;
        } else if (location.getName().endsWith(".jar")) {
            try {
                // look into jar file if there are additional lib jars
                // includes
                Vector<URL> urls = new Vector<URL>();
                urls.add(location.toURI().toURL());

                String baseURL = "jar:" + location.toURI().toURL() + "!/";
                JarFile jar = new JarFile(location);
                Enumeration<JarEntry> e = jar.entries();
                while (e.hasMoreElements()) {
                    JarEntry entry = e.nextElement();
                    if ((entry.getName().startsWith("libs/"))
                                && (entry.getName().endsWith(".jar"))) {
                        urls.add(new URL(baseURL + entry.getName()));
                    }
                }

                return urls.toArray(new URL[urls.size()]);
            } catch (MalformedURLException e) {
                logger.error("SimplePluginLoader: Could not convert to URL: "
                             + location + " (" + e.getMessage() + ").");
            } catch (IOException e1) {
                logger.error("Error while opening/reading jar file: "
                             + location, e1);
            }
        }

        if (!location.isDirectory()) {
            location = location.getParentFile();
        }

        final Vector<URL> result = getJarsRecursiveFromDir(location);
        return result.toArray(new URL[result.size()]);
    }

    private static Vector<URL> getJarsRecursiveFromDir(File dir) {
        Vector<URL> v = new Vector<URL>();

        File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".jar");
                }
            });

        // No JARs or directories found?
        if (files == null) {
            return v;
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    v.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.debug("Can't convert file location to URL: "
                                 + e.getMessage(), e);
                }
            } else if (file.isDirectory()) {
                v.addAll(getJarsRecursiveFromDir(file));
            }
        }

        return v;
    }

    /**
     * Loads the plugin in the given directory.
     * For that, the plugin.cfg file is parsed and searched for
     * the pluginClassName entry.
     * This class will then be loaded and instantiated.
     */
    public final IPlugin loadPlugin(PluginProperties props) {
        URL pluginLocation = props.getURL();
        logger.debug(getClass().getSimpleName() + " loading from "
                     + props.getURL());
        URL[] pluginJars = unifyURL(pluginLocation);


        // Get all URLs.
        Set<URL> urls = new HashSet<URL>();
        Collections.addAll(urls, loader.getURLs());

        for (URL pluginJar : pluginJars) {
            if (!urls.contains(pluginJar)) {
                loader.addURL(pluginJar);
            }
        }

        try {
            // Find main class.
            Class<?extends IPlugin> mainClass = findMainClass(props);

            // If no main class exists, create a basic plugin adapter.
            if (mainClass == null) {
                logger.debug("* no main class!");
                return new PluginAdapter(props);
            }

            // Create a plugin from properties.
            final IPlugin plugin = createPlugin(props, mainClass);
            bindPluginServices(mainClass, plugin);

            return plugin;
        } catch (PluginInstantiationException e) {
            logger.debug(getClass().getSimpleName() + ": " + e.getMessage());
            if (logger.isTraceEnabled()) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public final IPlugin loadPluginFromURL(URL url) {
        PluginProperties props = new PluginProperties(url);
        try {
            InputStream stream = SimplePluginLoader.PluginConfigFinder
                                     .getConfigInputStream(url);
            props.load(stream);
            return loadPlugin(props);
        } catch (Exception e) {
            logger.error("SimplePluginLoader.loadPluginFromURL: " + e);
        }

        return null;
    }

    protected abstract IPlugin createPlugin(PluginProperties props,
                                            Class<?extends IPlugin> mainClass)
            throws PluginInstantiationException;

    /**
     * @param props The plugin properties to search in.
     * @return Main Class instance or null.
     */
    protected Class<?extends IPlugin> findMainClass(PluginProperties props) {
        String className = props.getProperty("mainClass");

        if (className == null) {
            return null;
        }

        try {
            logger.debug("* creating a " + className + " with cl " + loader);
            Class<?> mainClass = loader.loadClass(className);
            if (mainClass.getClassLoader() != loader) {
                logger.warn("system class loader was used to load " + mainClass
                            + "; it was probably in your classpath.");
                logger.warn("That might turn out as a problem.");
            }

            return mainClass.asSubclass(IPlugin.class);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            return null;
        } catch (ClassCastException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Converts a String into a list by separating by commas
     */
    protected Collection<String> parseListString(String list) {
        StringTokenizer tok = new StringTokenizer(list, ",");
        Collection<String> result = new Vector<String>(tok.countTokens());
        try {
            while (tok.hasMoreTokens()) {
                String currentToken = tok.nextToken();
                result.add(currentToken);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("PluginLoader: " + e + " when parsing " + list
                         + " as list!");
        }
        return result;
    }

    /**
     * @param mainClass
     * @param plugin
     */
    private void bindPluginServices(Class<?extends IPlugin> mainClass,
                                    final IPlugin plugin) {
        // Save plugin in container.
        container.set(mainClass, plugin);
        logger.debug("Bound Service: " + mainClass);

        for (final Method method : plugin.getClass().getMethods()) {
            if (method.getAnnotation(Provides.class) != null) {
                final Class<?> service = method.getReturnType();
                container.addDefinition(new FactoryDefinition<Object>(service,
                                                                      new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            return method.invoke(plugin);
                        }
                    }));
                logger.debug("Bound Service: " + service.toString());
            }
        }
    }
}
