package de.renew.plugin;

import de.renew.plugin.load.SimplePluginLoader;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * This is a standard implementation of the IPlugin interface. When instantiated
 * with its home URL, it uses the SimplePluginLoader's PluginConfigFinder to
 * initialize its PluginProperties (i.e., it reads the plugin.cfg entries).
 *
 * It provides constructors with a PluginProperties argument (for the
 * SimplePluginLoader); it can also be instantiated with a URL. In this case, it
 * calls the PluginConfigFinder of the SimplePluginLoader to create a
 * PluginProperties object for its configuration.
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class PluginAdapter implements IPlugin {

    /** Default logger for instances of this class. **/
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginAdapter.class);

    /**
     * Stores the configuration of this plugin.
     **/
    protected PluginProperties _properties;

    /**
     * Create an <code>PluginAdapter</code> instance.
     * Automatically loads the contents of <code>plugin.cfg</code>,
     * whose location is derived by using the
     * {@link de.renew.plugin.load.SimplePluginLoader.PluginConfigFinder}.
     * Only <code>file</code>-URLs are supported.
     *
     * @param location  an <code>URL</code> pointing to the <code>jar</code>
     *                  file or directory containing the plugin code and
     *                  configuration files.
     *
     * @exception PluginException
     *   if an error occurs while loading the plugin or its configuration.
     *   Possible nested exceptions are:
     *   <ul>
     *   <li>{@link MalformedURLException} if the <code>plugin.cfg</code> URL
     *       could not be derived from the plugin URL.</li>
     *   <li>{@link IOException} if the configuration could not be loaded.</li>
     *   </ul>
     **/
    public PluginAdapter(URL location) throws PluginException {
        this(loadPluginPropertiesFromURL(location));
    }

    /**
     * Creates a PluginAdapter with the given PluginProperties.
     *
     * @param props  the plugin configuration.
     **/
    public PluginAdapter(PluginProperties props) {
        _properties = props;
    }

    /**
     * Automatically loads the contents of <code>plugin.cfg</code>,
     * whose location is derived by using the
     * {@link de.renew.plugin.load.SimplePluginLoader.PluginConfigFinder}.
     * Only <code>file</code>-URLs are supported.
     *
     * @param location  an <code>URL</code> pointing to the <code>jar</code>
     *                  file or directory containing the plugin code and
     *                  configuration files.
     *
     * @return The plugin properties which were loaded.
     * @throws PluginException
     *   if an error occurs while loading the plugin or its configuration.
     *   Possible nested exceptions are:
     *   <ul>
     *   <li>{@link MalformedURLException} if the <code>plugin.cfg</code> URL
     *       could not be derived from the plugin URL.</li>
     *   <li>{@link IOException} if the configuration could not be loaded.</li>
     *   </ul>
     **/
    protected static PluginProperties loadPluginPropertiesFromURL(URL location)
            throws PluginException {
        PluginProperties pluginProps = new PluginProperties(location);
        try {
            InputStream stream = SimplePluginLoader.PluginConfigFinder
                                     .getConfigInputStream(location);
            pluginProps.load(stream);
            stream.close();
        } catch (MalformedURLException e) {
            throw new PluginException("PluginAdapter constructor: trying to find properties",
                                      e);
        } catch (IOException e) {
            throw new PluginException("PluginAdapter constructor: trying to read properties",
                                      e);
        }

        return pluginProps;
    }

    /**
     * {@inheritDoc}
     * @return  The default implementation obtains the name of the plugin
     *          from the associated {@link PluginProperties} object.
     **/
    public String getName() {
        return getProperties().getProperty("name");
    }

    /**
     * {@inheritDoc}
     * @return  The default implementation obtains the alias of the plugin
     *          from the associated {@link PluginProperties} object.
     **/
    public String getAlias() {
        return getProperties().getProperty("alias");
    }

    /**
     * {@inheritDoc}
     * @return  The default implementation returns the name of the plugin,
     *          prepended by the word <code>plugin</code>.
     **/
    public String toString() {
        return "plugin " + getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation does not do anything besides a debug log message.
     * </p>
     **/
    public void init() {
        logger.debug(toString() + ": (default) init method called.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation does not do anything besides a debug log message.
     * </p>
     *
     * @return  The default implementation always returns <code>true</code>.
     **/
    public boolean cleanup() {
        logger.debug(toString() + ": (default) cleanup method called.");
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation does not do anything besides a debug log message.
     * </p>
     *
     * @return  The default implementation always returns <code>true</code>.
     **/
    public boolean canShutDown() {
        logger.debug(toString() + ": (default) canShutDown method called.");
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return  the <code>PluginProperties</code> object determined or given
     *          at the creation of this plugin.
     **/
    public PluginProperties getProperties() {
        return _properties;
    }

    /**
     * Registers this plugin instance as active so that the plugin system
     * will not automatically terminate unless {@link #registerExitOk()} is
     * called.
     *
     * @see PluginManager#blockExit
     **/
    protected void registerExitBlock() {
        logger.debug(toString() + " blocking exit.");
        PluginManager.getInstance().blockExit(this);
    }

    /**
     * Cancels the {@link #registerExitBlock()} registration of this plugin
     * instance.  The plugin system will no longer consider this plugin as
     * active and may terminate automatically (unless other exit blockers
     * are still registered).
     *
     * @see PluginManager#exitOk
     **/
    protected void registerExitOk() {
        logger.debug(toString() + " releasing exit block.");
        PluginManager.getInstance().exitOk(this);
    }

    /**
     * The version is now given in etc/plugin.cfg and will be displayed also when "info nc" is called on the command prompt.
     * @return  The Version information.
     */
    public String getLongVersion() {
        String result = "";
        PluginProperties props = getProperties();
        if (props.containsKey("version")) {
            String versionText = props.getProperty("versionText");
            if (versionText == null) {
                versionText = "Version: ";
            }

            String versionDate = props.getProperty("versionDate");
            if (versionDate == null) {
                versionDate = "no date set";
            }
            result = versionText + " " + getVersion() + "! Date: "
                     + versionDate;
        }
        return result;
    }

    /**
     * The version is now given in etc/plugin.cfg and will be displayed also when "info nc" is called on the command prompt.
     * @return  The Version information.
     */
    public String getVersion() {
        String result = "";
        PluginProperties props = getProperties();
        if (props.containsKey("version")) {
            result = getProperties().getProperty("version");
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see de.renew.plugin.IPlugin#startUpComplete()
     */
    public void startUpComplete() {
    }
}