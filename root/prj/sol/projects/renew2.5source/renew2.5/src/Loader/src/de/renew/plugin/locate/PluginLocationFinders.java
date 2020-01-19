package de.renew.plugin.locate;

import de.renew.plugin.PluginProperties;
import de.renew.plugin.PropertyHelper;

import java.net.URL;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/**
 * A Composition of PluginLocationFinders so several Finders can be used
 * to look for plugin configuration objects.
 */
public class PluginLocationFinders implements PluginLocationFinder {
    private static PluginLocationFinders _instance;
    private Collection<PluginLocationFinder> _finders = new Vector<PluginLocationFinder>();

    private PluginLocationFinders() {
    }

    public static PluginLocationFinders getInstance() {
        if (_instance == null) {
            _instance = new PluginLocationFinders();
        }
        return _instance;
    }

    public void addLocationFinder(PluginLocationFinder f) {
        _finders.add(f);
    }

    public void removeLocationFinder(PluginLocationFinder f) {
        _finders.remove(f);
    }

    /**
     * Calls findPluginLocations() for all contained PluginLocationFinders,
     * merging the result lists.
     */
    public Collection<PluginProperties> findPluginLocations() {
        Collection<PluginProperties> result = new Vector<PluginProperties>();
        for (PluginLocationFinder finder : _finders) {
            //    		logger.debug (finder + " looking for plug-in locations");
            Collection<PluginProperties> locations = finder
                                                         .findPluginLocations();
            result.addAll(locations);
        }

        PluginProperties userProperties = PluginProperties.getUserProperties();

        if (userProperties.getBoolProperty("de.renew.plugin.autoLoad", true)) {
            // remove all items that the user didnt want to load
            Collection<String> noLoad = PropertyHelper.parseListString(userProperties
                                                                       .getProperty("de.renew.plugin.noLoad",
                                                                                    ""));
            Iterator<PluginProperties> cleanedList = result.iterator();
            while (cleanedList.hasNext()) {
                PluginProperties props = cleanedList.next();
                if (noLoad.contains(props.getName())) {
                    cleanedList.remove();
                }
            }
        } else {
            Collection<String> load = PropertyHelper.parseListString(userProperties
                                                                     .getProperty("de.renew.plugin.load",
                                                                                  ""));
            Iterator<PluginProperties> cleanedList = result.iterator();
            while (cleanedList.hasNext()) {
                PluginProperties props = cleanedList.next();
                if (!load.contains(props.getName())) {
                    cleanedList.remove();
                }
            }
        }

        return result;
    }

    /**
     * Calls <code>checkPluginLocation(url)</code> for all contained
     * <code>PluginLocationFinder</code>s, returning the first result
     * that differs from <code>null</code> (if there is any).
     **/
    public PluginProperties checkPluginLocation(URL url) {
        PluginProperties props = null;
        for (PluginLocationFinder finder : _finders) {
            // logger.debug (finder + " checking plug-in location");
            props = finder.checkPluginLocation(url);
        }
        return props;
    }
}