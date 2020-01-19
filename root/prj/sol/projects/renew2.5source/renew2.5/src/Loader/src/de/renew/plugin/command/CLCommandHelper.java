package de.renew.plugin.command;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Helper class
 *
 * @author Eva M&uuml;ller
 */
public class CLCommandHelper {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CLCommandHelper.class);

    /**
     * Checks if cl-parameter/flag is set.<br>
     * Flags are considered as starting with a minus
     *
     * @param args [String[]]
     * @param flags [String...] List of flags to check
     * @return boolean
     */
    public static boolean isFlagSet(String[] args, String... flags) {
        if (args == null) {
            return false;
        }
        if (flags == null) {
            return false;
        }
        for (String flag : flags) {
            if (flag == null || flag.trim().length() == 0) {
                return false;
            }
            String flagToCheck = flag.trim();
            if (!flagToCheck.startsWith("-")) {
                flagToCheck = "-" + flagToCheck;
            }

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(flagToCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract plugin name from cl-parameters
     * @param args [String[]]
     * @return String | NULL
     */
    public static String getPluginName(String[] args) {
        if (args == null) {
            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (!args[i].trim().startsWith("-")) {
                return args[i];
            }
        }
        return null;
    }

    /**
     * Check whether the given plugin may be unloaded with regards to
     * require/depend-relation.
     * Returns a list of plugins that are dependent on the given plugin.
     */
    protected static Map<String, Collection<IPlugin>> checkDependencies(IPlugin toCheck,
                                                                        boolean allowMultipleServices) {
        Collection<String> provisions = toCheck.getProperties().getProvisions();
        HashMap<String, Collection<IPlugin>> dependers = new HashMap<String, Collection<IPlugin>>();
        if (provisions != null) {
            Iterator<String> prIt = provisions.iterator();
            while (prIt != null && prIt.hasNext()) {
                String prov = prIt.next();
                boolean getAnother = false;
                Collection<IPlugin> all = PluginManager.getInstance()
                                                       .getPluginsProviding(prov);
                if (all != null && allowMultipleServices) {
                    Iterator<IPlugin> plugins = all.iterator();
                    while (plugins != null && plugins.hasNext()) {
                        IPlugin iPlugin = plugins.next();
                        if (!iPlugin.getName().equals(toCheck.getName())) {
                            getAnother = true;
                            break;
                        }
                    }
                }
                if (!getAnother) {
                    Collection<IPlugin> reqs = getRequirers(prov);
                    if (!reqs.isEmpty()) {
                        dependers.put(prov, reqs);
                    }
                }
            }
        }
        return dependers;
    }

    /*
     * These two methods call themselves, recursively,
     * to find all plugins that are
     */
    public static List<IPlugin> getDependers(IPlugin toCheck,
                                             boolean allowMultipleServices) {
        List<IPlugin> result = new ArrayList<IPlugin>();
        result.add(toCheck);
        Map<String, Collection<IPlugin>> dependers = checkDependencies(toCheck,
                                                                       allowMultipleServices);
        Collection<Collection<IPlugin>> deps = dependers.values();
        result.addAll(getDependers(deps, allowMultipleServices));
        return result;
    }

    private static List<IPlugin> getDependers(Collection<?> plugins,
                                              boolean allowMultipleServices) {
        List<IPlugin> result = new ArrayList<IPlugin>();
        Iterator<?> it = plugins.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            List<IPlugin> toAdd;
            if (o instanceof Collection<?>) {
                Collection<?> toCheck = (Collection<?>) o;
                toCheck.removeAll(result);
                toAdd = getDependers(toCheck, allowMultipleServices);
            } else {
                IPlugin toCheck = (IPlugin) o;
                toAdd = getDependers(toCheck, allowMultipleServices);
            }

            // make sure the depending plugins are not added more than once
            result.removeAll(toAdd);
            result.addAll(toAdd);
        }
        return result;
    }

    private static Collection<IPlugin> getRequirers(String prov) {
        Collection<IPlugin> result = new ArrayList<IPlugin>();
        Collection<IPlugin> plugins = PluginManager.getInstance().getPlugins();

        Iterator<IPlugin> plIt = plugins.iterator();

        // collect all plugins that require the current provision
        while (plIt.hasNext()) {
            IPlugin pl = plIt.next();
            Collection<String> reqs = pl.getProperties().getRequirements();
            if (reqs.contains(prov)) {
                result.add(pl);
            }
        }
        return result;
    }
}