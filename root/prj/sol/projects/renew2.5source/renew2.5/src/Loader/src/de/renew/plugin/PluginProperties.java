package de.renew.plugin;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;


/**
 * An extension of the java.util.Properties class used by the Plugin Loader
 * system. It uses System variables to request property values, if present; if
 * no variable is set, the file "renew.properties" in the "config" directory of
 * the renew installation is used.
 * <p>
 * If there is no entry for the requested property in there either, a default
 * value should be provided by the Plugin (usually configured in the plugin.cfg
 * file). It contains a number of properties specific to the plugin system,
 * "mainClass", "provides", "requires" and "name", that can be requested by
 * dedicated get method (i.e., getName() instead of getProperty("name")).
 * Additionally, typing of properties is allowed: getBoolProperty and
 * getIntProperty try to convert the String object into the respective type.
 * <p>
 * PluginProperties that belong to a specific plugin (i.e., are not the
 * userProperties), provide a list of property names that are used by this
 * plugin. To do this, all properties in the configuration information are
 * considered; consequently, the plugin developer needs to put ALL properties in
 * there. This additionally takes care that a proper set of default values are
 * available.
 */
public class PluginProperties extends Properties {
    public static final String NAME = "name";
    public static final String MAIN_CLASS = "mainClass";
    public static final String REQUIRES = "requires";
    public static final String PROVIDES = "provides";
    public static final String UNKNOWN = "unknown";
    public static final String VERSION = "version";
    public static final String VERSION_TEXT = "versionText";
    public static final String VERSION_DATE = "versionDate";
    protected static PluginProperties _userProperties;
    private URL _sourceURL;
    public static Logger logger = Logger.getLogger(PluginProperties.class);

    /**
     * The keyword to use for known properties without default value in the
     * plugin's configuration file. These properties will be known to the
     * plugin, but not set initially.
     * <p>
     * The actual string is &quot; <code>&lt;not set&gt;</code> &quot; (case
     * and whitespace sensitive).
     * </p>
     * <p>
     * <b>Example: </b> <br>
     * The configuration file contains:
     *
     * <pre>
     *
     *
     *       de.renew.remote.serverClass = &lt;not set&gt;
     *
     *
     * </pre>
     *
     * The methods of this object will return the following results (unless some
     * other source has overridden the default value): <br>-<code>
     *   {@link #getProperty getProperty}(&quot;de.renew.remote.serverClass&quot;) = null
     *   </code>
     * <br>-<code>
     *   {@link #containsKey containsKey}(&quot;de.renew.remote.serverClass&quot;) = false
     *   </code>
     * <br>-<code>
     *   {@link #isKnownProperty isKnownProperty}(&quot;de.renew.remote.serverClass&quot;) = true
     *   </code>
     * <br>
     * </p>
     */
    public static final String NULL_VALUE = "<not set>";
    Collection<Object> _knownProperties = new HashSet<Object>();

    /**
     * This constructor is used to create the PluginProperties with the
     * specified URL, with entries loaded from the given InputStream. It will
     * override these entries with settings from the userProperties and the
     * system properties.
     *
     * @throws IOException
     *             if the properties cannot be loaded from the specified stream.
     *             If that happens, no settings will have been set.
     */
    public PluginProperties(URL source, InputStream loadFrom)
            throws IOException {
        _sourceURL = source;

        load(loadFrom);

        // Add all property names from plugin.cfg to the set of known
        // properties (except mandatory plugin properties).
        setKnownProperties();
        _knownProperties.remove(NAME);
        _knownProperties.remove(MAIN_CLASS);
        _knownProperties.remove(REQUIRES);
        _knownProperties.remove(PROVIDES);
        _knownProperties.remove(VERSION);
        _knownProperties.remove(VERSION_TEXT);
        _knownProperties.remove(VERSION_DATE);

        // Unset known properties marked as {@link #NULL_VALUE}
        removeNullProperties();

        // Add all properties from the configuration file and the command
        // line.
        putAll(getUserProperties());
        putAll(System.getProperties());
    }

    public PluginProperties(URL source) {
        // assume default properties as base for all queried properties
        super(getUserProperties());
        // override with currently set System properties
        putAll(System.getProperties());

        _sourceURL = source;
    }

    /**
     * this constructor is provided so the user properties can be created
     * without infinite recursion.
     *
     */
    protected PluginProperties() {
        super();
        putAll(System.getProperties());
    }

    /**
     * Removes all property entries with the special value {@link #NULL_VALUE}.
     * The result is like the affected properties would never have been set.
     * This method is intendend to be called during creation time, after the
     * known properties have been determined from the plugin configuration file.
     */
    private void removeNullProperties() {
        Iterator<Entry<Object, Object>> entries = entrySet().iterator();
        while (entries.hasNext()) {
            Entry<Object, Object> entry = entries.next();
            if (NULL_VALUE.equals(entry.getValue())) {
                entries.remove();
            }
        }
    }

    public URL getURL() {
        return _sourceURL;
    }

    public String getMainClass() {
        return getProperty(MAIN_CLASS, "");
    }

    public String getName() {
        return getProperty(NAME, UNKNOWN);
    }

    public String getVersion() {
        return getProperty(VERSION, UNKNOWN);
    }

    public String getVersionDate() {
        return getProperty(VERSION_DATE, UNKNOWN);
    }

    public String getVersionText() {
        return getProperty(VERSION_TEXT, UNKNOWN);
    }

    /**
     * Returns a list of the services provided by the plugin to which this
     * property object belongs.
     */
    public Collection<String> getProvisions() {
        String result = getProperty(PROVIDES, "");
        return PropertyHelper.parseListString(result);
    }

    /**
     * Returns a list of the services required by the plugin to which this
     * property object belongs.
     */
    public Collection<String> getRequirements() {
        String result = getProperty(REQUIRES, "");
        return PropertyHelper.parseListString(result);
    }

    /**
     * Returns an array containing the names of all known properties. A property
     * is <i>known </i>, if it was included in the initial set of properties
     * loaded from the <code>URL</code> given to the constructor of this
     * <code>PluginProperties</code> instance.
     *
     * @return an array of property names.
     */
    public String[] getKnownProperties() {
        return _knownProperties.toArray(new String[_knownProperties.size()]);
    }

    /**
     * Tells whether the given property name is known. A property is <i>known
     * </i>, if it was included in the initial set of properties loaded from the
     * <code>URL</code> given to the constructor of this
     * <code>PluginProperties</code> instance.
     *
     * @param name
     *            the property name for the query
     * @return <code>true</code>, if the property with the given
     *         <code>name</code> is known.
     */
    public boolean isKnownProperty(String name) {
        // TODO: We should add:
        //if(containsKey(name)) return true;
        // and let the _knownProperties only contain the keys not in our keyset.
        return _knownProperties.contains(name);
    }

    public synchronized String toString() {
        return "properties of Plugin " + getName();
    }

    /**
     * Return a String showing name, URL, mainClass of the corresponding Plugin
     * along with its provisions and requirements.
     */
    public String toExtString() {
        String result = "properties of Plugin " + getName() + ":\n";
        result += "loaded from URL " + getURL() + "\n";
        result += "main class is " + getMainClass() + "\n";
        result += "provides " + CollectionLister.toString(getProvisions())
        + "\n";
        result += "requires " + CollectionLister.toString(getRequirements());
        return result;
    }

    /**
     * Return a PluginProperties object that represents user settings that are
     * not specific to one plugin.
     */
    public static PluginProperties getUserProperties() {
        if (_userProperties == null) {
            PluginProperties props = new PluginProperties();


            // Per-installation configuration:
            // ./config/renew.properties
            URL url = null;
            try {
                url = PluginManager.getLoaderLocation();
                String base = url.toExternalForm();
                base = base.substring(0, base.lastIndexOf("/"));
                url = new URL(base + "/config/renew.properties");
            } catch (Exception e) {
                String errorDescription = e.toString();
                logger.warn("Could not determine installation properties file name: "
                            + errorDescription);
                logger.debug(errorDescription, e);
                url = null;
            }
            fillProperties(url, "installation", props);


            // Per-user configuration:
            // ~/.renew.properties
            try {
                String homeDir = System.getProperty("user.home");
                url = new File(homeDir + File.separator + ".renew.properties").toURI()
                                                                              .toURL();
            } catch (Exception e) {
                String errorDescription = e.toString();
                logger.warn("Could not determine user properties file name: "
                            + errorDescription);
                logger.debug(errorDescription, e);
                url = null;
            }
            fillProperties(url, "user", props);

            // now override set values with system properties again
            props.putAll(System.getProperties());

            _userProperties = props;
        }
        return _userProperties;
    }

    /**
     * Loads all information from the properties file at the given
     * <code>URL</code> into the given <code>PluginProperties</code>
     * object.
     *
     * @param url   the location of the properties file.
     *              If <code>null</code>, nothing happens and
     *              <code>false</code> is returned.
     * @param kind  the kind of the configuration file
     *              (e.g. <code>"installation"</code> or <code>"user"</code>)
     * @param props the properties object to update
     * @return   <code>true</code> if the properties file exists and has
     *           successfully been read.
     **/
    private static boolean fillProperties(final URL url, final String kind,
                                          PluginProperties props) {
        if (url == null) {
            return false;
        }
        try {
            InputStream stream = url.openStream();
            logger.info("Reading " + kind + " properties from "
                        + url.toExternalForm());
            props.load(stream);
            props._sourceURL = url;
            return true;
        } catch (FileNotFoundException e) {
            logger.info("No " + kind + " properties: " + e.getMessage());
        } catch (IOException e) {
            String errorDescription = e.toString();
            logger.warn("Could not read " + kind + " properties "
                        + url.toExternalForm() + ": " + errorDescription);
            logger.debug(errorDescription, e);
        }
        return false;
    }

    /**
     * Returns the property as a bool with a default of "false".
     *
     * If the property is set to an empty String, "true" is returned. This is to
     * reflect the fact that it is true that the property is set.
     *
     * @param property
     *            the property to convert to boolean
     * @return the boolean value of the property, false if not set.
     * @see PropertyHelper#getBoolProperty(Properties, String)
     */
    public boolean getBoolProperty(String property) {
        return PropertyHelper.getBoolProperty(this, property);
    }

    /**
     * Returns the property as a bool. Returns the value of the "def" parameter
     * if property is not set.
     *
     * If the property is set to an empty String, "true" is returned. This is to
     * reflect the fact that it is true that the property is set.
     *
     * @param property
     *            the property to convert to boolean
     * @param def
     *            the default paramater that is returned if the property is not set
     * @return the boolean value of the property, false if not set.
     * @see PropertyHelper#getBoolProperty(Properties, String)
     */
    public boolean getBoolProperty(String property, boolean def) {
        return PropertyHelper.getBoolProperty(this, property, def);
    }

    /**
     * Returns the property as an int. Returns the value of the "def" parameter
     * if property is not set.
     *
     * @param property
     *            the property to convert to int
     * @param def
     *            the default paramater that is returned if the property is not set
     * @return the parsed int value of the property
     */
    public int getIntProperty(String property, int def) {
        return PropertyHelper.getIntProperty(this, property, def);
    }

    /**
     * Returns the property as an int. Returns -1 if the property is not set or
     * an error occurs
     *
     * @return the int value of the given property
     */
    public int getIntProperty(String property) {
        return PropertyHelper.getIntProperty(this, property);
    }

    /**
     * Returns the property as a List.
     *
     * @param propName
     *            the property to be retrieved.
     * @see PropertyHelper#getListProperty(Properties, String)
     */
    public List<String> getListProperty(String propName) {
        return PropertyHelper.getListProperty(this, propName);
    }

    /**
     * Returns a property with a given suffix.
     *
     * @param filter  name suffix of the property to be retrieved.
     * @see PropertyHelper#getListProperty(Properties, String)
     */
    public String getFilteredProperty(String filter) {
        Enumeration<Object> en = keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            if (key.equals(filter)) {
                return (String) get(key);
            }
        }
        en = keys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            logger.debug("PluginProperties@getFilteredProperty key: " + key
                         + " filter: " + filter + " " + key.endsWith(filter));
            if (key.endsWith(filter)) {
                return (String) get(key);
            }
        }
        return "";
    }

    // public static boolean debug() {
    // return false;
    // }


    /**
     * @return
     */
    public Collection<String> getUnsetProperties() {
        String[] props = getKnownProperties();
        Collection<String> unsetProperties = new HashSet<String>();
        for (int i = 0; i < props.length; i++) {
            logger.debug("PluginProperies@getUnsetProperties: checking for null: "
                         + props[i]);
            if (get(props[i]) == null) {
                unsetProperties.add(props[i]);
            }
        }
        return unsetProperties;
    }

    /**
     * Adds the keySet to the known properties.
     */
    public void setKnownProperties() {
        _knownProperties.addAll(keySet());
    }

    /**
     * Get all keys of this {@link PluginProperties}.
     *
     * @return {@code Collection<String>} The keys as collection.
     *
     * @author Eva Mueller
     * @date Nov 26, 2010
     * @version 0.1
     */
    public Collection<String> getKeys() {
        Enumeration<Object> tmp = keys();
        Collection<String> coll = new Vector<String>();
        while (tmp.hasMoreElements()) {
            coll.add((String) tmp.nextElement());
        }
        return coll;
    }
}