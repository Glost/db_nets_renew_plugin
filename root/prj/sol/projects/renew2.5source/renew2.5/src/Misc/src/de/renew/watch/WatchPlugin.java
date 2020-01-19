package de.renew.watch;

import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulatorExtensionAdapter;
import de.renew.application.SimulatorPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.util.Collection;


/**
 * The watch plugin supports the observation and control of
 * activated transitions in a simulation.
 * <p>
 * </p>
 * WatchPlugin.java
 * Created: Mon Jul 21  2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class WatchPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(WatchPlugin.class);

    /**
     * References the simulator plugin where the watch extension
     * is registered.
     **/
    private SimulatorPlugin simPlugin;

    /**
     * Our simulator extension.
     **/
    private WatchExtension watchExtension;

    /**
     * Creates an instance of the watch plugin object.
     *
     * @param props   this <code>PluginProperties</code> object
     *                contains the plugin's meta information.
     **/
    public WatchPlugin(PluginProperties props) {
        super(props);
    }

    /**
     * Registers the watch extension with the simulator plugin.
     **/
    public void init() {
        PluginManager pluginManager = PluginManager.getInstance();
        Collection<IPlugin> simPlugins = pluginManager.getPluginsProviding("de.renew.simulator");
        this.simPlugin = (SimulatorPlugin) simPlugins.iterator().next();
        this.watchExtension = new WatchExtension();
        simPlugin.addExtension(watchExtension);
    }

    /**
     * Unregisters the watch extension from the simulator plugin.
     **/
    public boolean cleanup() {
        if ((simPlugin != null) && (watchExtension != null)) {
            simPlugin.removeExtension(watchExtension);
            simPlugin = null;
            watchExtension = null;
        }
        return true;
    }

    /**
     * Resets the channel supervisor every time the simulation is
     * set up or terminated.
     **/
    private class WatchExtension extends SimulatorExtensionAdapter {
        public void simulationSetup(SimulationEnvironment env) {
            logger.debug("WatchPlugin: Resetting channel supervisor.");
            //FIXME: This is too early.  The simulator has probably not
            //       been started yet.  In the case of a restored simulation
            //       state, the simulation objects are not complete at this
            //       point of time.  We need a different event like 
            //       'simulation started'...
            ChannelSupervisor.activate();
        }

        public void simulationTerminated() {
            logger.debug("WatchPlugin: Resetting channel supervisor.");
            ChannelSupervisor.reset();
        }
    }
}