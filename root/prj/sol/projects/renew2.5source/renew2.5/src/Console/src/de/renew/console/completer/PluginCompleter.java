/**
 *
 */
package de.renew.console.completer;

import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author cabac
 *
 */
public class PluginCompleter implements Completer {
    public PluginCompleter() {
    }

    /* (non-Javadoc)
     * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        return new StringsCompleter(getPluginNames()).complete(buffer, cursor,
                                                               candidates);
    }

    public ArrayList<String> getPluginNames() {
        PluginManager pm = PluginManager.getInstance();
        ArrayList<String> pNames = new ArrayList<String>();
        Iterator<IPlugin> it = pm.getPlugins().iterator();
        while (it.hasNext()) {
            String pluginName = it.next().getName();
            pNames.add(pluginName.trim().replaceAll(" ", "_"));
        }
        return pNames;
    }
}