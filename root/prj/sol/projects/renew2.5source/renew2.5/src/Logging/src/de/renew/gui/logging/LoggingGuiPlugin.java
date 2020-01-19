package de.renew.gui.logging;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.MenuManager;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenuItem;

import de.renew.engine.common.SimulatorEventLogger;

import de.renew.gui.GuiPlugin;
import de.renew.gui.ModeReplacement;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.awt.event.KeyEvent;

import java.net.URL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Provides a graphical user interface (gui) for logging messages
 * produced during a simulation of petri nets.
 *
 * @author Sven Offermann
 **/
public class LoggingGuiPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(LoggingGuiPlugin.class);

    /**
     * A set of references to the controllers of opened logging frames.
     * The set is used durring the plugin cleanup process to close
     * opened logging frames.
     */
    private static final Set<LoggingController> _LoggingFrameControllers = new HashSet<LoggingController>();

    /**
     * The menu item to open a windows with a simulation trace.
     */
    private CommandMenuItem menu;

    /**
     * The controller of the logging configuration tab in the configure simulation frame.
     */
    private ConfigureLoggingController configTab;

    // ---------------------------------------------------- Initialisation


    /**
     * Instantiates the plugin (and nothing more).
     * @see PluginAdapter#PluginAdapter(URL)
     **/
    public LoggingGuiPlugin(URL location) throws PluginException {
        super(location);
    }

    /**
     * Instantiates the plugin (and nothing more).
     * @see PluginAdapter#PluginAdapter(PluginProperties)
     **/


    //NOTICEthrows
    public LoggingGuiPlugin(PluginProperties props) throws PluginException {
        super(props);
    }

    /**
     * Initializes the plugin.
     * Registers commands and connections to underlying plugins.
     **/
    public void init() {
        logger.debug("initializing Logging GUI plugin.");

        // register ConfigureLoggingController to the list of ConfigurationSimulationTabController
        // used to configure a simulation.
        configTab = new ConfigureLoggingController();
        GuiPlugin.getCurrent().addConfigTabController(configTab);

        // create simulation menu entries
        menu = new CommandMenuItem(new Command("show simulation trace") {
                public boolean isExecutable() {
                    if (!super.isExecutable()) {
                        return false;
                    }
                    return ModeReplacement.getInstance().getSimulation()
                                          .isSimulationActive();
                }

                public void execute() {
                    new LoggingController(_LoggingFrameControllers);
                }
            }, KeyEvent.VK_L);

        MenuManager mm = MenuManager.getInstance();
        SeparatorFactory sepFac = new SeparatorFactory("de.renew.gui.logging");
        mm.registerMenu(GuiPlugin.SIMULATION_MENU, sepFac.createSeparator());
        mm.registerMenu(GuiPlugin.SIMULATION_MENU, menu,
                        "de.renew.gui.logging.showTrace");

        // add a GuiAppender to the simulation root logger
        org.apache.log4j.Logger.getLogger(SimulatorEventLogger.SIM_LOG_PREFIX)
                               .addAppender(new GuiAppender());
    }

    /**
     * Convenience Method for getting the logging gui starter object
     * presently registered in the PluginManager
     */
    public static LoggingGuiPlugin getCurrent() {
        Iterator<IPlugin> it = PluginManager.getInstance()
                                            .getPluginsProviding("de.renew.logging")
                                            .iterator();
        while (it.hasNext()) {
            IPlugin o = it.next();
            if (o instanceof LoggingGuiPlugin) {
                return (LoggingGuiPlugin) o;
            }
        }
        return null;
    }

    // --------------------------------------- Opening and closing the gui
    // Logging -----------------------------------------


    /* (non-Javadoc)
     * @see de.renew.plugin.PluginAdapter#cleanup()
     */
    public boolean cleanup() {
        Iterator<LoggingController> i = _LoggingFrameControllers.iterator();
        while (i.hasNext()) {
            LoggingController c = i.next();
            c.closeFrame();
        }

        DrawPlugin.getCurrent().getMenuManager().unregisterMenu(menu);

        GuiPlugin.getCurrent().removeConfigTabController(configTab);

        return true;
    }

    public void closedLoggingFrame(LoggingController controller) {
        _LoggingFrameControllers.remove(controller);
    }
}