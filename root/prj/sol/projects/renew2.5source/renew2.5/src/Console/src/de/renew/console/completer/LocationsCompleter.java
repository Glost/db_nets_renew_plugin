/**
 *
 */
package de.renew.console.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;

import de.renew.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/** Offers completions for plugin jar files.
 *
 * @author cabac
 *
 */
public class LocationsCompleter implements Completer {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LocationsCompleter.class);

    public LocationsCompleter() {
    }

    /* (non-Javadoc)
     * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return new StringsCompleter(getPluginLocations()).complete(buffer,
                                                                   cursor,
                                                                   candidates);
    }

    public ArrayList<String> getPluginLocations() {
        PluginManager pm = PluginManager.getInstance();
        ArrayList<String> locations = new ArrayList<String>();
        Iterator<IPlugin> it = pm.getPlugins().iterator();
        while (it.hasNext()) {
            IPlugin iPlugin = (IPlugin) it.next();
            String pluginName = iPlugin.getName();
            IPlugin plugin = pm.getPluginByName(pluginName);
            if (plugin == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[" + LocationsCompleter.class.getSimpleName()
                                 + "]: Could not find plugin for name: "
                                 + pluginName);
                }
            } else {
                locations.add(StringUtil.getExtendedFilename(plugin.getProperties()
                                                                   .getURL()
                                                                   .getFile()));
            }
        }
        return locations;
    }
}