package de.renew.plugin.di;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginProperties;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-10
 */
public abstract class DIPlugin implements IPlugin {
    PluginProperties properties;

    /**
     * {@inheritDoc}
     * @return  The default implementation obtains the name of the plugin
     *          from the associated {@link PluginProperties} object.
     */
    @Override
    final public String getName() {
        return properties.getName();
    }

    /**
     * {@inheritDoc}
     * @return  The default implementation obtains the alias of the plugin
     *          from the associated {@link PluginProperties} object.
     */
    @Override
    final public String getAlias() {
        return properties.getProperty("alias");
    }

    /**
     * The version is now given in etc/plugin.cfg and will be displayed also when "info nc" is called on the command prompt.
     * @return  The Version information.
     */
    @Override
    final public String getVersion() {
        if (properties.containsKey("version")) {
            return properties.getProperty("version");
        }

        return "0.0.1";
    }

    /**
     * {@inheritDoc}
     * @return  The default implementation returns the name of the plugin,
     *          prepended by the word <code>plugin</code>.
     */
    @Override
    public String toString() {
        return "plugin " + getName();
    }

    /**
     * Sets the plugin properties.
     *
     * @param properties
     */
    final public void setProperties(PluginProperties properties) {
        this.properties = properties;
    }

    @Override
    final public PluginProperties getProperties() {
        return properties;
    }

    @Override
    public void init() {
    }

    @Override
    public boolean cleanup() {
        return true;
    }

    @Override
    public boolean canShutDown() {
        return true;
    }

    @Override
    public void startUpComplete() {
    }
}