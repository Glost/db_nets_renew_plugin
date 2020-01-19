package de.renew.plugin.load;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.DependencyCheckList;
import de.renew.plugin.DependencyCheckList.DependencyElement;
import de.renew.plugin.DependencyNotFulfilledException;
import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginClassLoader;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.locate.PluginLocationFinders;

import java.beans.PropertyChangeEvent;

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * This Class is a Composition of PluginLoaders. When an instance's
 * {@link #loadPlugins} method is called, all contained PluginLoaders are
 * triggered.
 * <p>
 * In the current implementation, this class checks all dependencies and decides
 * when to add which plugin to the {@link PluginManager}. So this class
 * <i>must</i> be used as top level plugin loader, and it <i>may not</i> be
 * added as a tree node to the plugin loader hierarchy. However, the
 * implementation does not check for these conditions. In future versions,
 * dependency check and plugin addition can be moved somewhere else, where the
 * functions are more appropriate.
 * </p>
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 */
public class PluginLoaderComposition implements PluginLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginLoaderComposition.class);
    private Collection<PluginLoader> _loaders = new Vector<PluginLoader>();
    private static IExtendedProgressBar progressBar = null;

    /**
     * Constructor for PluginLoaderComposition.
     */
    public PluginLoaderComposition() {
        super();
    }

    public static void setProgressBar(IExtendedProgressBar bar) {
        progressBar = bar;
    }

    public Collection<IPlugin> loadPlugins() {
        logger.info("loading plugins...");
        DependencyCheckList<PluginProperties> dependencyList = new DependencyCheckList<PluginProperties>();

        Collection<PluginProperties> locations = PluginLocationFinders.getInstance()
                                                                      .findPluginLocations();
        Collection<IPlugin> result = new Vector<IPlugin>();

        PluginProperties splashscreenProps = null;

        List<String> loadedPluginMainClasses = new Vector<String>();

        for (PluginProperties props : locations) {
            if (props.getName().equals("Renew Splashscreen")) {
                splashscreenProps = props;
            } else {
                dependencyList.addElement(DependencyElement.create(props));
            }
        }

        List<PluginProperties> fulfilledDependencies = dependencyList
                                                           .getFulfilledObjects();

        for (PluginProperties plugin : fulfilledDependencies) {
            logger.debug(plugin);
        }
        int progess = 0;
        int start;
        if (splashscreenProps != null) {
            start = -1;
        } else {
            start = 0;
        }

        PluginClassLoader pluginClassLoader = PluginManager.getInstance()
                                                           .getPluginClassLoader();
        PluginProperties properties;
        URL[] pluginJars = new URL[0];
        for (int i = 0; i < fulfilledDependencies.size(); i++) {
            properties = fulfilledDependencies.get(i);
            pluginJars = AbstractPluginLoader.unifyURL(properties.getURL());
            for (int x = 0; x < pluginJars.length; x++) {
                pluginClassLoader.addURL(pluginJars[x]);
            }
        }

        for (int i = start; i < fulfilledDependencies.size(); i++) {
            if (progressBar != null) {
                int old = progess;
                progess++;
                double count = i + 1;
                double size = fulfilledDependencies.size();
                int newValue = (int) ((count / size) * 100);
                try {
                    progressBar.propertyChange(new PropertyChangeEvent(this,
                                                                       "progress",
                                                                       old,
                                                                       newValue));
                } catch (Exception e) {
                    progressBar = null;
                }
            }
            PluginProperties props;
            if (i == -1) {
                props = splashscreenProps;
            } else {
                props = fulfilledDependencies.get(i);
            }
            if (PluginManager.getInstance().getPluginByName(props.getName()) != null) {
                logger.debug("PluginLoader: A plugin with the name "
                             + props.getName()
                             + " has already been loaded. Skipping "
                             + props.getURL());
                // do not load if this plugin has already been loaded
                continue;
            }
            IPlugin loaded = loadPlugin(props);
            if (loaded != null) {
                try {
                    PluginManager.getInstance().addPlugin(loaded);
                    if (progressBar != null) {
                        try {
                            progressBar.propertyChange(new PropertyChangeEvent(this,
                                                                               "pluginLoaded",
                                                                               null,
                                                                               loaded
                                                                               .getName()));
                        } catch (Exception e) {
                            progressBar = null;
                        }
                    }
                    Collection<String> mainClass = loaded.getProperties()
                                                         .getProvisions();
                    if (!loadedPluginMainClasses.contains(mainClass)) {
                        loadedPluginMainClasses.addAll(mainClass);
                    }
                    result.add(loaded);
                } catch (DependencyNotFulfilledException e) {
                    assert false : "The PluginManager doubts our dependency check for "
                    + loaded;
                }
            } else {
                logger.debug("PluginLoaderComposition: did not load plugin from "
                             + props);
                Collection<DependencyElement<PluginProperties>> retracted = dependencyList
                                                                            .removeElementWithDependencies(props);
                if (!retracted.isEmpty()) {
                    logger.debug("PluginLoaderComposition: recalculated dependencies.");
                }

                // There is an important (dirty?) assumption here:
                // Because the dependencyList is ordered, the
                // fulfilledDependencies list should not differ in the
                // first part before the failed plugin. So we can just
                // continue to loop from the same point where we are.
                fulfilledDependencies = dependencyList.getFulfilledObjects();
                i--;
            }
        }

        Collection<DependencyElement<PluginProperties>> unfulfilled = dependencyList
                                                                      .getUnfulfilled();
        if (!unfulfilled.isEmpty()) {
            logger.warn("\nThere are plugins with unfulfilled dependencies:");
            List<String> provisions = new Vector<String>();
            Map<String, List<String>> missing = new HashMap<String, List<String>>();
            for (DependencyElement<PluginProperties> dependencyElement : unfulfilled) {
                logger.warn("Plugin with unfullfilled dependencies: "
                            + dependencyElement.toStringExtended(loadedPluginMainClasses)
                            + "\n");
                for (String provisionsByDE : dependencyElement.getProvisions()) {
                    if (!provisions.contains(provisionsByDE)) {
                        provisions.add(provisionsByDE);
                    }
                }
                for (String required : dependencyElement.getMissingRequirements(loadedPluginMainClasses)) {
                    if (!provisions.contains(required)) {
                        List<String> requiredBy = new Vector<String>();
                        if (missing.containsKey(required)) {
                            requiredBy = missing.get(required);
                        }
                        String pluginName = dependencyElement.getPluginName();
                        if (!requiredBy.contains(pluginName)) {
                            requiredBy.add(pluginName);
                        }
                        missing.put(required, requiredBy);
                    }
                }
                for (String provisionsByDE : dependencyElement.getProvisions()) {
                    if (missing.containsKey(provisionsByDE)) {
                        missing.remove(provisionsByDE);
                    }
                }
            }
            List<String> erroneousPlugins = new Vector<String>();
            for (String missingPlugin : missing.keySet()) {
                erroneousPlugins.add(missingPlugin + " [Required by: "
                                     + CollectionLister.toString(missing.get(missingPlugin),
                                                                 ", ") + "]");
            }
            logger.error("\nPLEASE add the plugin(s) with the following provisions in their plugin.cfg\n\n* "
                         + CollectionLister.toString(erroneousPlugins, "\n* ")
                         + "\n");
        }

        if (progressBar != null) {
            progressBar.close();
        }

        return result;
    }

    public void addLoader(PluginLoader l) {
        _loaders.add(l);
    }

    public void removeLoader(PluginLoader l) {
        _loaders.remove(l);
    }

    /**
     * This method is called to trigger the loading of a plugin with the
     * previously loaded properties.. All previously registered loaders are
     * called to try to load a plugin; the first successfully loaded is
     * returned.
     * <p>
     * In contrast to {@link #loadPluginFromURL} and {@link #loadPlugins}, this
     * method does neither check dependencies nor add the plugin to the
     * <code>PluginManager</code> automatically.
     * </p>
     */
    public IPlugin loadPlugin(PluginProperties props) {
        IPlugin result;
        for (PluginLoader loader : _loaders) {
            try {
                result = loader.loadPlugin(props);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                logger.error("PluginLoaderComposition: " + e + " occured when "
                             + loader + " tried to load plugin from "
                             + props.getURL());
                logger.error(e.getMessage(), e);
            }
        }

        logger.error("No loader was able to load " + props.getName() + "!");
        return null;
    }

    /**
     * This method is called to trigger the loading of a plugin at the given
     * location.
     * <p>
     * There are two ways of loading the plugin, they are tried in the given
     * order until a plugin has been loaded.
     * <ol>
     * <li><code>PluginLocationFinders</code> are asked to transform the given
     * <code>source</code> URL into <code>PluginProperties</code>. If the
     * finders are successful, the plugin is loaded according to the properties
     * found.</li>
     * <li>All registered <code>PluginLoader</code>s are called to try to load
     * the plugin without the assistance of a finder; the first successfully
     * loaded plugin is returned.</li>
     * </ol>
     * If a plugin has been loaded, it is automatically added to the
     * <code>PluginManager</code>.
     * </p>
     **/
    public IPlugin loadPluginFromURL(URL source) {
        IPlugin result = null;
        PluginManager mgr = PluginManager.getInstance();
        PluginProperties props = PluginLocationFinders.getInstance()
                                                      .checkPluginLocation(source);
        if (props != null) {
            if (mgr.checkDependenciesFulfilled(props)) {
                result = loadPlugin(props);
            } else {
                logger.error("Dependencies are not fulfilled for plugin \n\t"
                             + props.getName() + "\nlocated at \n\t"
                             + props.getURL());
            }
        } else {
            for (PluginLoader loader : _loaders) {
                try {
                    result = loader.loadPluginFromURL(source);
                    if (result != null) {
                        break;
                    }
                } catch (Exception e) {
                    logger.error("PluginLoaderComposition: " + e
                                 + " occured when " + loader
                                 + " tried to load plugin from " + source);
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        if (result != null) {
            try {
                mgr.addPlugin(result);
            } catch (DependencyNotFulfilledException e) {
                logger.error("Dependencies are not fulfilled for " + result
                             + " loaded from " + source);
                return null;
            }
        }
        return result;
    }
}