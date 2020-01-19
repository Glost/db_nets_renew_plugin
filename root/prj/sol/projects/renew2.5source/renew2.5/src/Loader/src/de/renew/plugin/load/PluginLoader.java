package de.renew.plugin.load;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginProperties;

import java.net.URL;


/**
 * This interface enables to load different types of plugins.
 * If a PluginLoader is written for a specific component model,
 * it can be registered in the PluginLoaderComposition of the
 * PluginManager.
 *
 * @author Joern Schumacher
 */
public interface PluginLoader {

    /**
     * Tries to load the plugin specified by the given properties.
     * May return <code>null</code>, if the plugin could not be loaded.
     * <p>
     * When this method is called, usually some
     * {@link de.renew.plugin.locate.PluginLocationFinder}
     * has located the plugin before and supplied the given <code>props</code>.
     * The <code>props</code> should then include the URL where the plugin
     * is located.
     * </p>
     *
     * @param props  the properties of the plugin to load.
     *
     * @return  an instance of the loaded plugin's main class. Returns
     *          <code>null</code> if the plugin could not be loaded.
     **/
    public IPlugin loadPlugin(PluginProperties props);

    /**
     * Tries to load a plugin from the given URL.
     * May return <code>null</code>, if the plugin could not be loaded.
     * <p>
     * The plugin loader has to determine the plugin's properties by
     * itself. Usually, after retrieving the <code>PluginProperties</code>
     * object in some way from the given <code>url</code>, this method
     * delegates <code>loadPlugin(PluginProperties)</code>.
     * </p>
     *
     * @param url  the location of the plugin to load.
     *
     * @return  an instance of the loaded plugin's main class. Returns
     *          <code>null</code> if the plugin could not be loaded.
     **/
    public IPlugin loadPluginFromURL(URL url);
}