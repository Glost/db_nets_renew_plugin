package de.renew.plugin.load;



/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-10
 */
public class PluginInstantiationException extends Exception {
    public PluginInstantiationException(String pluginName, Throwable cause) {
        super(buildMessage(pluginName, cause), cause);
    }

    private static String buildMessage(String pluginName, Throwable cause) {
        return String.format("Could not instantiate %s, caused by %s.",
                             pluginName, cause);
    }
}