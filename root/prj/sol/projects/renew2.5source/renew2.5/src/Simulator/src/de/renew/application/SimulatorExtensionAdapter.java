package de.renew.application;

import de.renew.shadow.ShadowLookup;


/**
 * Default implementation template for extensions to the
 * simulation engine. All methods are implemented by doing simply
 * nothing.
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public class SimulatorExtensionAdapter implements SimulatorExtension {
    /* Non-JavaDoc: specified by interface SimulatorExtension. */
    public void simulationSetup(SimulationEnvironment env) {
    }

    /* Non-JavaDoc: specified by interface SimulatorExtension. */
    public void netsCompiled(ShadowLookup lookup) {
    }

    /* Non-JavaDoc: specified by interface SimulatorExtension. */
    public void simulationTerminated() {
    }

    /* Non-JavaDoc: specified by interface SimulatorExtension. */
    public void simulationTerminating() {
    }
}