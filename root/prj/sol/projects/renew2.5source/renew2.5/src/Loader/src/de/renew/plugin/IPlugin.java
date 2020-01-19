package de.renew.plugin;



/**
 * This is the interface that all active Plugins (i.e., the mainClass attribute
 * in the plugin.cfg) must implement. The init() method will be called on
 * activating the Plugin, the cleanup() method on shutting down. The
 * getProperties() must return a PluginProperties object.
 *
 * The plugins will be loaded by Implementors of the PluginLoader interface. If
 * you want to write a plugin, its constructor will probably be the deciding
 * instance by which PluginLoaders it can be instantiated.
 *
 * The standard PluginLoader, SimplePluginLoader, creates Plugin objects by
 * calling a constructor (via reflection) with a PluginProperties object as its
 * only argument:
 * <pre>
 * public IPluginImpl(PluginProperties prop) {...}
 * </pre>
 *
 * @author Joern Schumacher
 * @author Lawrence Cabac
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface IPlugin {

    /**
     * Return the name of this plugin. The name may not vary during the
     * lifetime of the plugin. It should be unique because it is used to
     * denote plugins to apply operations.
     *
     * @return The unique, invariant name of the plugin.
     **/
    public String getName();

    /**
     * Return the alias name of this plugin.
     *
     * @return an alias of the plugin, hopefully shorter than the name.
     **/
    public String getAlias();

    /**
     * This method is called when the plugin is activated. At this point, all
     * requirements (given in the plugin.cfg) are fulfilled and the respective
     * classes loaded.
     */
    public void init();

    /**
     * This method is called when the plugin is shut down.
     * The plugin is responsible to cap all its connections to other
     * plugins that have been created in the {@link #init} method.
     *
     * @return <code>true</code> if the cleanup was successful.
     *   A return value of <code>false</code> indicates that the shutdown
     *   process failed. The services of the plugin will not be removed
     *   from the system. If the shutdown process involves multiple
     *   plugins, remaining plugins will not be stopped.
     **/
    public boolean cleanup();

    /**
     * Return the <code>PluginProperties</code> of this plugin.
     *
     * @return The <code>PluginProperties</code> associated with this plugin.
     **/
    public PluginProperties getProperties();

    /**
     * Checks if the cleanup method may be called.
     *
     * @return <code>true</code> if the plugin has no objections against
     *   its shutdown. A return value of <code>false</code> indicates that
     *   the plugin is in a state where a shutdown would be inappropriate.
     */
    public boolean canShutDown();

    public void startUpComplete();

    public abstract String getVersion();
}