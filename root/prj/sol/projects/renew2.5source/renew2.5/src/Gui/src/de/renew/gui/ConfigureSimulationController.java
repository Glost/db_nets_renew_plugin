package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulatorPlugin;

import de.renew.plugin.PluginManager;

import java.awt.Component;

import java.util.Properties;

import javax.swing.JOptionPane;


/**
 * Manages a configuration dialog for the Renew simulation. The
 * configuration is based on and affects the properties of the
 * Renew Simulator plugin.
 * <p>
 * Several theme-specific option tabs can be added. Each tab has
 * to be managed by a {@link ConfigureSimulationTabController}
 * implementation.
 * </p>
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
class ConfigureSimulationController {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConfigureSimulationController.class);

    /**
     * Refers to the Swing dialog managed by this controller.
     **/
    private ConfigureSimulationDialog dialog;

    /**
     * The displayed {@link ConfigureSimulationTabController} of the dialog
     */
    private ConfigureSimulationTabController[] tabControllers;

    /**
     * Creates the controller and its associated dialog.
     * The frame of the given application is used as parent
     * for the dialog frame. Some well-known option tabs
     * are added to the dialog as well.
     *
     * @param app the application where the dialog should
     *            belong to.
     **/
    public ConfigureSimulationController(CPNApplication app) {
        tabControllers = GuiPlugin.getCurrent().getConfigTabController();
        Component[] initialTabs = new Component[tabControllers.length];
        for (int i = 0; i < tabControllers.length; i++) {
            initialTabs[i] = tabControllers[i].getTab();
        }

        this.dialog = new ConfigureSimulationDialog(app.getFrame(), this,
                                                    initialTabs);
    }

    /**
     * Displays the dialog. Also updates the dialog, if it has not been
     * visible beforehand.
     **/
    public void showDialog() {
        if (!dialog.isVisible()) {
            updateDialog();
            dialog.setVisible(true);
            DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                      .addDialog(DrawPlugin.WINDOWS_CATEGORY_TOOLS, dialog);
        }
    }

    /**
     * Hides the dialog from view.
     **/
    public void closeDialog() {
        DrawPlugin.getCurrent().getMenuManager().getWindowsMenu()
                  .removeDialog(dialog);
        dialog.setVisible(false);
    }

    /**
     * Instructs all option tab controllers to write their
     * current settings to the simulator plugin's properties.
     * This method is intended to be called upon user request
     * only.
     **/
    public void commitDialog() {
        Properties props = getSimulatorPluginProperties();
        for (int x = 0; x < tabControllers.length; x++) {
            tabControllers[x].commitTab(props);
        }
    }

    /**
     * Instructs all option tab controllers to bring their
     * current settings in line with the simulator plugin's
     * properties.
     * This method is intended to be called upon user request
     * or upon dialog initialisation.
     **/
    public void updateDialog() {
        Properties props = getSimulatorPluginProperties();
        for (int x = 0; x < tabControllers.length; x++) {
            tabControllers[x].updateTab(props);
        }
    }

    /**
     * Instructs all option tab controllers to bring their
     * current settings in line with the current simulation's
     * properties.
     * This method is intended to be called upon user request
     * only.
     **/
    public void updateDialogFromSimulation() {
        Properties props = getSimulationProperties();
        if (props != null) {
            for (int x = 0; x < tabControllers.length; x++) {
                tabControllers[x].updateTab(props);
            }
        } else {
            JOptionPane.showMessageDialog(dialog,
                                          "Could not update properties from current simulation:\n"
                                          + "No simulation running.",
                                          "Configure Simulation",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetches the current properties of the Renew Simulator plugin.
     *
     * @return the properties object of the simulator plugin.
     **/
    private static Properties getSimulatorPluginProperties() {
        logger.debug("ConfigureSimulationController: "
                     + "Reading the simulator plugin's properties.");
        return getSimulatorPlugin().getProperties();
    }

    /**
     * Fetches the properties of the current simulation
     * environment. If there is no running simulation,
     * <code>null</code> is returned instead.
     *
     * @return the properties object of the current simulation or
     *         the <code>null</code>, depending on whether there
     *         is a running simulation.
     **/
    private static Properties getSimulationProperties() {
        SimulatorPlugin simulatorPlugin = getSimulatorPlugin();
        Properties props;
        SimulationEnvironment env = simulatorPlugin.getCurrentEnvironment();
        if (env != null) {
            logger.debug("ConfigureSimulationController: "
                         + "Reading the current simulation's properties.");
            props = env.getProperties();
        } else {
            logger.debug("ConfigureSimulationController: "
                         + "No current simulation available to read properties.");
            props = null;
        }
        return props;
    }

    /**
     * Determines the Renew Simulator plugin instance.
     *
     * @return the first plugin providing <code>de.renew.simulator</code>.
     */
    private static SimulatorPlugin getSimulatorPlugin() {
        return (SimulatorPlugin) PluginManager.getInstance()
                                              .getPluginsProviding("de.renew.simulator")
                                              .iterator().next();
    }
}