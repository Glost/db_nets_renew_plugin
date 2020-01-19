/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package de.renew.plugin.load;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginClassLoader;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.annotations.Inject;
import de.renew.plugin.di.DIPlugin;
import de.renew.plugin.di.DependencyFinder;
import de.renew.plugin.di.MissingDependencyException;
import de.renew.plugin.di.ServiceContainer;

import java.lang.reflect.Constructor;

import java.util.LinkedList;


/**
 * Dependency Injection support class that loads static plugins.
 */
public class DIPluginLoader extends SimplePluginLoader
        implements DependencyFinder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DIPluginLoader.class);

    public DIPluginLoader(PluginClassLoader loader, ServiceContainer container) {
        super(loader, container);
    }

    @Override
    public Object findDependency(Class<?> service)
            throws MissingDependencyException {
        return container.get(service);
    }

    @Override
    protected IPlugin createPlugin(PluginProperties props,
                                   Class<?extends IPlugin> mainClass)
            throws PluginInstantiationException {
        // Get the constructor which requires injections.
        Constructor<?> injectionPoint = findInjectionPoint(mainClass);

        // No injection point? Just try to make an instance.
        if (injectionPoint == null) {
            try {
                final IPlugin plugin = mainClass.newInstance();

                if (plugin instanceof DIPlugin) {
                    logger.warn(props.getName() + " has no injection point.");
                    final DIPlugin diPlugin = (DIPlugin) plugin;
                    diPlugin.setProperties(props);

                    return diPlugin;
                }

                return null;
            } catch (ReflectiveOperationException e) {
                throw new PluginInstantiationException(props.getName(), e);
            }
        }

        // Collect all constructor parameters.
        final LinkedList<Object> dependencies = new LinkedList<Object>();
        final Class<?>[] params = injectionPoint.getParameterTypes();
        for (Class<?> param : params) {
            // Add the required plugin to the constructor parameters.
            try {
                dependencies.add(findDependency(param));
            } catch (MissingDependencyException e) {
                throw new PluginInstantiationException(props.getName(), e);
            }
        }

        // Try to instantiate.
        try {
            Object plugin = injectionPoint.newInstance(dependencies.toArray());

            if (!(plugin instanceof DIPlugin)) {
                return null;
            }

            // Configure the new dependency injected Plugin.
            final DIPlugin diPlugin = (DIPlugin) plugin;
            diPlugin.setProperties(props);

            return diPlugin;
        } catch (ReflectiveOperationException e) {
            if (e.getCause() != null) {
                logger.error(props.getName() + " fails during instantiation!");
                e.getCause().printStackTrace();
            }
            throw new PluginInstantiationException(props.getName(), e);
        }
    }

    /**
     * Finds the injection point of a plugin class.
     *
     * @param pluginClass The plugin class to look in.
     * @return a constructor or <code>null</code>, if no injection point exists.
     */
    private Constructor<?> findInjectionPoint(Class<?extends IPlugin> pluginClass) {
        Constructor<?>[] constructors = pluginClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                return constructor;
            }
        }

        return null;
    }
}