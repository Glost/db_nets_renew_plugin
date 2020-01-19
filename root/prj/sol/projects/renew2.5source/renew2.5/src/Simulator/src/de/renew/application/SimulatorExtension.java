package de.renew.application;

import de.renew.shadow.ShadowLookup;


/**
 * Interface for all extensions to the simulation engine.
 * <p>
 * </p>
 * SimulatorExtension.java
 * Created: Wed Jun  4  2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface SimulatorExtension {

    /**
     * Informs the extension that a new simulation environment
     * has been set up. The engine has not been started yet.
     * Unless some SimulatorExtension has done so, no nets have
     * been added to the simulation yet.
     *
     * @param env  holds information about all parameters of the
     *             simulation setup.
     **/
    public void simulationSetup(SimulationEnvironment env);

    /**
     * Informs the extension that some nets are being added to
     * the simulation. The nets have already been compiled, but
     * not yet been published to the simulation engine.
     *
     * @param lookup  a mapping from shadow nets to compiled nets that
     *                includes exactly the set of added shadow nets.
     *                However, it can include other compiled nets.
     **/
    public void netsCompiled(ShadowLookup lookup);

    /**
     * Informs the extension that the current simulation
     * environment is being terminated. The engine has already
     * been stopped, but all nets and net instances still exist
     * (as far as they are reachable).
     **/
    public void simulationTerminated();

    /**
     * Informs the extension that the current simulation
     * environment is about to being terminated. The engine is still running
     * and all nets and net instances still exist
     * (as far as they are reachable).
     **/
    public void simulationTerminating();
}