package de.renew.gui;

import java.awt.Component;

import java.util.Properties;


/**
 * An implementation of this interface is used for each option
 * tab managed by the {@link ConfigureSimulationController}.
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface ConfigureSimulationTabController {

    /**
     * Returns the Swing component associated with this
     * controller. It is expected that this method always returns
     * the same object during the lifetime of the controller.
     *
     * @return a Swing component to be included in the
     *         configuration dialog.
     **/
    public Component getTab();

    /**
     * Instructs this controller to update its gui component in
     * accordance to the given properties.
     *
     * @param props the properties to get actual values from.
     *              This will most probably be the current
     *              simulation's properties.
     **/
    public void updateTab(Properties props);

    /**
     * Instructs this controller to update the given properties
     * in accordance to the current settings of the controller's
     * gui component.
     *
     * @param props the properties to write the current settings
     *              to. This will most probably be the properties
     *              of the Renew Simulator plugin.
     **/
    public void commitTab(Properties props);
}