package de.renew.plugin;



/**
 * A Log strategy used by the PluginManager to use several ways of configuring the logging
 * mechanism.
 *
 * @author Dominic Dibbern
 * @version 1.0
 * @date 08.02.2012
 *
 */
public interface LogStrategy {
    public void configureLogging();
}