package de.renew.application;

import de.renew.engine.simulator.Simulator;

import java.util.Properties;


/**
 * Information container, comprises all parameters of the running
 * simulation.
 * <p>
 * </p>
 * SimulationEnvironment.java
 * Created: Thu Jun  5  2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class SimulationEnvironment {
    private Simulator simulator;
    private SimulatorExtension[] extensions;
    private Properties properties;

    /**
     * Creates a new <code>SimulationEnvironment</code>
     * object that comprises all of the given information.
     * Any objects given as parameters are stored by
     * reference, so there is no cloning or other type of
     * information hiding.
     *
     * @param simulator  the simulation engine used in this simulation.
     * @param extensions array of active extensions in this simulation.
     * @param properties the properties used to set up this simulation.
     **/
    public SimulationEnvironment(Simulator simulator,
                                 SimulatorExtension[] extensions,
                                 Properties properties) {
        this.simulator = simulator;
        this.extensions = extensions;
        this.properties = properties;
    }

    /**
     * Gets the simulation engine of this environment.
     **/
    public Simulator getSimulator() {
        return simulator;
    }

    /**
     * Gets the active extensions in this simulation
     * environment.
     **/
    public SimulatorExtension[] getExtensions() {
        return this.extensions;
    }

    /**
     * Gets the properties used in this simulation
     * environment.
     **/
    public Properties getProperties() {
        return properties;
    }
}