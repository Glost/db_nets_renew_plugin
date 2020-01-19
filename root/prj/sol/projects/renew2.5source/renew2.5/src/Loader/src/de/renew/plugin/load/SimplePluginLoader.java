/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package de.renew.plugin.load;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginClassLoader;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.di.ServiceContainer;

import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Singleton support class that loads static plugins.
 *
 * There are two ways to instantiate a plugin with this class' loadPlugin(PluginProperties) method,
 * depending on what a call to getURL() on the given properties object returns:
 * if it is a jar file, this jar file will be assumed to contain the plugin code
 * as well as the plugin.cfg file with the plugin's configuration information;
 * if it is a directory, the directory is searched for the plugin.cfg file as well as
 * for a jarfile which is then used as the plugin code source.
 * If there is no jarfile in the directory, the directory itself is used as code source for
 * the resulting plugin.
 */
public class SimplePluginLoader extends AbstractPluginLoader {

    /**
     * Initializes the PluginLoader instance.
     */
    public SimplePluginLoader(PluginClassLoader loader,
                              ServiceContainer container) {
        super(loader, container);
    }

    /**
     * Creates the actual plugin.
     *
     * @param props The plugin properties to search in.
     * @param mainClass Main class to instantiate.
     * @return Plugin instance or <code>null</code> on error.
     */
    @Override
    protected IPlugin createPlugin(PluginProperties props,
                                   Class<?extends IPlugin> mainClass)
            throws PluginInstantiationException {
        try {
            // Build class params.
            Class<?>[] params = { props.getClass() };


            // Find the constructor.
            Constructor<?extends IPlugin> cons = mainClass.getConstructor(params);
            Object[] args = { props };

            return cons.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new PluginInstantiationException(props.getName(), e);
        }
    }

    public static class PluginConfigFinder {
        public static InputStream getConfigInputStream(URL pluginLocation)
                throws MalformedURLException, IOException {
            URL configLocation;
            configLocation = new URL(pluginLocation, "plugin.cfg");
            //		logger.debug ("loading cfg from " + configLocation.toExternalForm());
            InputStream stream = null;
            try {
                stream = configLocation.openStream();
            } catch (Exception e) {
                // maybe its a jar. try on.
                String urlString = pluginLocation.toExternalForm();
                int index = urlString.lastIndexOf('!');
                if (index > -1) {
                    urlString = urlString.substring(0, index) + "/plugin.cfg";
                    logger.debug("trying alternate URL for plugin.cfg: "
                                 + urlString);
                }
                configLocation = new URL(urlString);
                stream = configLocation.openStream();
            }
            return stream;
        }
    }
}