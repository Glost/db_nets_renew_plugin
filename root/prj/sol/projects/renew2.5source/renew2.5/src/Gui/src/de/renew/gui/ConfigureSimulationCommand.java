package de.renew.gui;

import CH.ifa.draw.util.Command;


/**
 * Opens a configuration dialog to control the simulation features
 * of the Renew simulator.
 * <p>
 * </p>
 * ConfigureSimulationCommand.java
 * Created: Mon Aug 11  2003
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class ConfigureSimulationCommand extends Command {
    private ConfigureSimulationController controller = null;

    // private CPNApplication app;
    public ConfigureSimulationCommand(String name) {
        super(name);
        //        this.app = app;
    }

    /**
     * This command is always executable.
     * @return always <code>true</code>.
     * @see Command#isExecutable()
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }

    /**
     * Displays a dialog to configure the simulation properties.
     **/
    public void execute() {
        if (controller == null) {
            CPNApplication app = GuiPlugin.getCurrent().getGui();
            controller = new ConfigureSimulationController(app);
        }
        controller.showDialog();
    }
}