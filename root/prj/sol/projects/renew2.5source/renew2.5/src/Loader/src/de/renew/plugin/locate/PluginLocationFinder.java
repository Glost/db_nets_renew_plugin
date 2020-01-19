package de.renew.plugin.locate;

import de.renew.plugin.PluginProperties;

import java.net.URL;

import java.util.Collection;


/**
 * This interface abstracts the process of looking for plugins
 * (in the file system, remote).
 * It retrieves a Collection which must contain PluginProperties objects.
 * These must be present previous to actually loading the plugins themselves
 * to enable the dependency check.
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 */
public interface PluginLocationFinder {

    /**
     * Returns all plugins this finder knows about.
     *
     * @return a <code>Collection</code> of {@link PluginProperties}.
     **/
    public Collection<PluginProperties> findPluginLocations();

    /**
     * Checks whether the given <code>URL</code> describes a
     * plugin. If this is the case, the method returns the
     * corresponding <code>PluginProperties</code> object.
     * Otherwise, it returns <code>null</code>.
     *
     * @param url  the <code>URL</code> to check
     * @return a <code>PluginProperties</code> value
     **/
    public PluginProperties checkPluginLocation(URL url);
}