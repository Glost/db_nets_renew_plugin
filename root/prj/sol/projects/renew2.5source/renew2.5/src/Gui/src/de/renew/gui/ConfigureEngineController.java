/*
 * Created on 16.05.2003
 *
 */
package de.renew.gui;

import de.renew.application.SimulatorPlugin;

import de.renew.plugin.PropertyHelper;

import java.awt.Component;

import java.util.Properties;


/**
 * An option panel controller to configure concurrent features of
 * the simulation engine.
 * @author Michael Duvigneau
 **/
public class ConfigureEngineController
        implements ConfigureSimulationTabController {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConfigureEngineController.class);
    private ConfigureEngineTab tab;

    public ConfigureEngineController() {
        //NOTICEsignature
        this.tab = new ConfigureEngineTab(this);
    }

    public Component getTab() {
        return tab;
    }

    public void commitTab(Properties props) {
        boolean sequential = tab.getSequential();
        int mult = Math.abs(Integer.parseInt(tab.getMultiplicity()));
        if (mult == 0) {
            mult = 1;
        }
        if (sequential) {
            mult = -mult;
        }
        if (mult == 1) {
            props.remove(SimulatorPlugin.MODE_PROP_NAME);
            logger.debug("ConfigureEngineController: Configured simulatorMode=null.");
        } else {
            props.setProperty(SimulatorPlugin.MODE_PROP_NAME,
                              Integer.toString(mult));
            logger.debug("ConfigureEngineController: Configured simulatorMode="
                         + mult + ".");
        }

        mult = Math.abs(Integer.parseInt(tab.getPriority()));
        if (mult == 0) {
            mult = 1;
        }

        //we dont want the priority of the simulator to be too high
        if (mult >= 10) {
            mult = 9;
        }
        props.setProperty(SimulatorPlugin.PRIORITY_PROP_NAME,
                          Integer.toString(mult));


        boolean classReinit = tab.getClassReinit();
        props.setProperty(SimulatorPlugin.REINIT_PROP_NAME,
                          Boolean.toString(classReinit));
        logger.debug("ConfigureEngineController: Configured classReinit="
                     + classReinit + ".");

        boolean eagerSimulation = tab.getEagerSimulation();
        props.setProperty(SimulatorPlugin.EAGER_PROP_NAME,
                          Boolean.toString(eagerSimulation));
        logger.debug("ConfigureEngineController: Configured eagerSimulation="
                     + eagerSimulation + ".");
    }

    public void updateTab(Properties props) {
        int mult = PropertyHelper.getIntProperty(props,
                                                 SimulatorPlugin.MODE_PROP_NAME,
                                                 1);
        boolean sequential = (mult < 0);
        if (sequential) {
            mult = -mult;
        }
        tab.setSequential(sequential);
        tab.setMultiplicity(Integer.toString(mult));

        mult = PropertyHelper.getIntProperty(props,
                                             SimulatorPlugin.PRIORITY_PROP_NAME,
                                             5);
        tab.setPriority(Integer.toString(mult));
        boolean classReinit = PropertyHelper.getBoolProperty(props,
                                                             SimulatorPlugin.REINIT_PROP_NAME);
        tab.setClassReinit(classReinit);

        boolean eagerSimulation = PropertyHelper.getBoolProperty(props,
                                                                 SimulatorPlugin.EAGER_PROP_NAME);
        tab.setEagerSimulation(eagerSimulation);
    }
}