package de.renew.gui;

import de.renew.application.IllegalCompilerException;
import de.renew.application.NoSimulationException;
import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulationRunningException;
import de.renew.application.SimulatorExtensionAdapter;
import de.renew.application.SimulatorPlugin;

import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.Simulator;

import de.renew.net.NetInstance;
import de.renew.net.NetNotFoundException;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.RemotePlugin;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class CPNSimulation {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNSimulation.class);
    protected ShadowNetSystem netSystem;
    private boolean sequentialOnly;
    private BreakpointManager breakpointManager = null;
    private CPNDrawingLoader drawingLoader;
    private SimulatorPlugin simulatorPlugin;
    public Exception lastSyntaxException;
    public boolean inGuiSetup;
    private SimulatorGuiCompilationExtension compilationExtension;

    /**
     * Creates an instance of CPNSimulation.
     * <p>
     * The object has nearly-singleton character. It is mandatory to call {
     * {@link #dispose()} before the object is released.
     * </p>
     *
     * @param onlySequential
     *            - documents whether simulations should be regarded as
     *            sequential. This class does not use the value directly. It
     *            seems that the parameter might be obsolete as there exists one
     *            constructor call only with a fixed value of <code>false</code>
     *            : {@link ModeReplacement#getSimulation()}.
     * @param loader
     *            - a reference to the central loader and manager of
     *            CPNDrawings. Needed to compile all drawings on demand.
     */
    public CPNSimulation(boolean onlySequential, CPNDrawingLoader loader) {
        this.simulatorPlugin = SimulatorPlugin.getCurrent();
        this.sequentialOnly = onlySequential;
        this.drawingLoader = loader;
        this.inGuiSetup = false;
        this.lastSyntaxException = null;
        newNetSystem();
        this.compilationExtension = new SimulatorGuiCompilationExtension(this);
        simulatorPlugin.addExtension(compilationExtension);
    }

    public boolean isStrictlySequential() {
        return sequentialOnly;
    }

    public void buildAllShadows() {
        newNetSystem();
        boolean again = true;
        while (again) {
            again = false;
            Iterator<CPNDrawing> drawings = drawingLoader.loadedDrawings();

            try {
                // Build new shadows of all nets and all elements.
                // Do everything in a fine order:
                while (drawings.hasNext()) {
                    CPNDrawing currentDrawing = drawings.next();
                    logger.debug("CPNSimulation: Building shadow for drawing "
                                 + currentDrawing + ".");
                    currentDrawing.buildShadow(netSystem);
                }
            } catch (ConcurrentModificationException e) {
                logger.error("CPNSimulation.buildAllShadows(): Concurrent modification. Redo from start...");
                again = true;
            }
        }
    }

    /**
     * Creates a new ShadowNetSystem, sets the information about compiler and
     * net loader, and updates the private instance variable {@link #netSystem}.
     **/
    protected void newNetSystem() {
        try {
            simulatorPlugin.possiblySetupClassSource(simulatorPlugin
                .getProperties());
        } catch (IllegalStateException e) {
            logger.warn("CPNSimulation: Cannot configure classReinit mode while a simulation is running.");
        }

        ShadowCompilerFactory compilerFactory = ModeReplacement.getInstance()
                                                               .getDefaultCompilerFactory();
        if (compilerFactory == null) {
            logger.warn("CPNSimulation: cannot start, got no compiler.");
        }

        netSystem = new ShadowNetSystem(compilerFactory);

        logger.debug("CPNSimulation: New net system created.");
    }

    public ShadowNetSystem getNetSystem() {
        // Make sure that all shadows are correctly set up.
        buildAllShadows();
        // Return the shadow net system.
        return netSystem;
    }

    public CPNDrawingLoader getDrawingLoader() {
        return drawingLoader;
    }

    public void setBreakpointManager(BreakpointManager newBreakpointManager) {
        BreakpointManager oldBreakpointManager = breakpointManager;
        breakpointManager = newBreakpointManager;
        if (oldBreakpointManager != null) {
            simulatorPlugin.removeExtension(oldBreakpointManager);
        }
        simulatorPlugin.addExtension(newBreakpointManager);
    }

    /**
     * Returns the actual breakpoint manager. Attention: May be
     * <code>null</code>!
     */
    public BreakpointManager getBreakpointManager() {
        return breakpointManager;
    }

    public SimulatorPlugin getSimulatorPlugin() {
        return simulatorPlugin;
    }

    public void syntaxCheckOnly() throws SyntaxException {
        // Make sure to recreate all shadows, so that changes of
        // the drawings are incorporated.
        buildAllShadows();

        // Compile and flatten the lookup table.
        netSystem.compile();
    }

    /**
     * Start a new simulation. Return the initially created NetInstance, if the
     * creation of the initial instance was successful, or null, if no initial
     * creation took place.
     *
     * @param mainNet
     *            the name of the net to create the first net instance from.
     *
     * @throws SyntaxException
     *             if the compiler found any errors in any net drawing.
     *
     * @throws NetNotFoundException
     *             if no net with the given name <code>mainNet</code> exists.
     */
    public NetInstanceAccessor initSimulation(String mainNet)
            throws SyntaxException, NetNotFoundException, NoSimulationException {
        // Remember that we are causing the current simulation setup.
        // Actually a concurrent setup from other sources might interfere
        // until we acquire the SimulatorPlugin lock in the try block below.
        // However, there won't be much damage unless our
        // SimulatorGuiCompilationExtension is called before we could call
        // buildAllShadows - then we might insert an empty, outdated, or
        // partially built sns into the simulation.
        inGuiSetup = true;
        lastSyntaxException = null;
        logger.trace("CPNSimulation: initializing simulation...");
        buildAllShadows();
        try {
            // Set the simulation up
            simulatorPlugin.setDefaultNetLoader();
            try {
                logger.trace("CPNSimulation: calling setupSimulation...");
                simulatorPlugin.setupSimulation(null);
                logger.trace("CPNSimulation: finished setupSimulation...");
            } catch (SimulationRunningException e) {
                logger.warn("Simulation already running, inserting nets anyway");
            }

            // The simulation setup should trigger our
            // SimulatorGuiCompilationExtension.
            // If something goes wrong, terminate the simulation.
            if (lastSyntaxException != null) {
                logger.trace("CPNSimulation: forwarding (rethrowing) exception: "
                             + lastSyntaxException);
                simulatorPlugin.terminateSimulation();
                if (lastSyntaxException instanceof SyntaxException) {
                    throw (SyntaxException) lastSyntaxException;
                } else if (lastSyntaxException instanceof IllegalCompilerException) {
                    throw (IllegalCompilerException) lastSyntaxException;
                } else {
                    throw new RuntimeException("Unexpected Exception type: "
                                               + lastSyntaxException,
                                               lastSyntaxException);
                }
            }

            logger.trace("CPNSimulation: instantiating net " + mainNet + "...");
            NetInstanceAccessor primaryInstance = null;

            if (RemotePlugin.getInstance() != null) {
                primaryInstance = RemotePlugin.getInstance()
                                              .wrapInstance(simulatorPlugin
                                      .createNetInstance(mainNet));
            }
            if (primaryInstance == null) {
                logger.trace("CPNSimulation: instantiation of net " + mainNet
                             + " failed.");
                simulatorPlugin.terminateSimulation();
            }
            return primaryInstance;
        } catch (NetNotFoundException e) {
            logger.trace("CPNSimulation: catching and rethrowing exception: "
                         + e);
            simulatorPlugin.terminateSimulation();
            throw e;
        } catch (NoSimulationException e) {
            logger.info("CPNSimulation: Simulation terminated externally.");
            throw e;
        } catch (RuntimeException e) {
            logger.trace("CPNSimulation: catching and rethrowing exception: "
                         + e);
            simulatorPlugin.terminateSimulation();
            throw e;
        } catch (Error e) {
            logger.trace("CPNSimulation: catching and rethrowing exception: "
                         + e);
            simulatorPlugin.terminateSimulation();
            throw e;
        } finally {
            inGuiSetup = false;
        }
    }

    /**
     * Writes all currently known <code>NetInstance</code>s, <code>Net</code>s,
     * the <code>SearchQueue</code> contents and some additional information to
     * the stream. The written information is sufficient to continue the
     * simulation from the same state after deserialization.
     * <p>
     * <b>Side effect:</b> Any open <code>BindingSelectionFrame</code> will be
     * closed on execution of this method!
     * </p>
     * <p>
     * With Renew 2.0, most of this method's functionality has moved to the
     * <code>SimulatorPlugin</code>.
     * </p>
     *
     * @param output
     *            target stream
     *
     * @see SimulatorPlugin#saveState
     **/
    public void saveState(java.io.ObjectOutput output)
            throws java.io.IOException {
        // We have to ensure that the simulation is stopped.
        // Yes, the simulator plugin enforces this, too, but we
        // need to freeze the state before we collect the local
        // net instances and close the selection frame.
        synchronized (simulatorPlugin) {
            simulatorPlugin.getCurrentEnvironment().getSimulator().stopRun();

            // Close the BindingSelectionFrame (otherwise it would
            // be stored with its transition).
            BindingSelectionFrame.close();

            // Collect all local instances from open drawings.
            NetInstance[] instances = CPNInstanceDrawing.getAllLocalInstances();

            // Let the simulator plugin save the state.
            simulatorPlugin.saveState(output, instances);
        }
    }

    /**
     * Restores a simulation saved by <code>saveState()</code>. This method
     * mostly delegates to {@link SimulatorPlugin#loadState}. It additionally
     * tries to reopen the explicitly named net instances within
     * <code>CPNInstanceDrawing</code>s.
     * <p>
     * With Renew 2.0, most of this method's functionality has moved to the
     * <code>SimulatorPlugin</code>.
     * </p>
     *
     * @param input
     *            source stream
     *
     * @see SimulatorPlugin#loadState
     */
    public void loadState(java.io.ObjectInput input)
            throws java.io.IOException, ClassNotFoundException,
                           SimulationRunningException {
        NetInstance[] instances = simulatorPlugin.loadState(input, null);

        // Now try to open CPNInstanceDrawings
        // for all net instances which were loaded.
        NetInstanceAccessor currentInstance;
        CPNApplication editor = GuiPlugin.getCurrent().getGui();
        RemotePlugin remote = RemotePlugin.getInstance();
        for (int i = 0; i < instances.length; i++) {
            currentInstance = remote.wrapInstance(instances[i]);
            if (editor != null) {
                editor.openInstanceDrawing(currentInstance);
            }
        }
    }

    public boolean isSimulationActive() {
        return simulatorPlugin.isSimulationActive();
    }

    public void simulationTerminate() {
        simulatorPlugin.terminateSimulation();
    }

    /**
     * To be called after an external firing of a transition was initiated.
     **/
    public void simulationRefresh() {
        SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() {
                    SimulationEnvironment env = simulatorPlugin
                        .getCurrentEnvironment();
                    Simulator simulator = (env == null) ? null
                                                        : env.getSimulator();
                    if (simulator != null) {
                        simulator.refresh();
                    }
                    return null;
                }
            });
    }

    public void simulationStop() {
        SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() {
                    SimulationEnvironment env = simulatorPlugin
                        .getCurrentEnvironment();
                    Simulator simulator = (env == null) ? null
                                                        : env.getSimulator();
                    if (simulator != null) {
                        simulator.stopRun();
                    }
                    return null;
                }
            });
    }

    public void simulationRun() {
        SimulationThreadPool.getCurrent().submitAndWait(new Callable<Object>() {
                public Object call() {
                    SimulationEnvironment env = simulatorPlugin
                        .getCurrentEnvironment();
                    Simulator simulator = (env == null) ? null
                                                        : env.getSimulator();

                    // To avoid simulation concurrent to serialization
                    synchronized (this) {
                        if (breakpointManager != null) {
                            breakpointManager.clearLog();
                        }
                        if (simulator != null) {
                            simulator.startRun();
                        }
                    }
                    return null;
                }
            });
    }

    public int simulationStep() {
        Future<Integer> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Integer>() {
                public Integer call() {
                    SimulationEnvironment env = simulatorPlugin
                                     .getCurrentEnvironment();
                    Simulator simulator = (env == null) ? null
                                                        : env.getSimulator();

                    // To avoid simulation concurrent to serialization
                    synchronized (this) {
                        if (breakpointManager != null) {
                            breakpointManager.clearLog();
                        }
                        if (simulator != null) {
                            return simulator.step();
                        } else {
                            return Simulator.statusDisabled;
                        }
                    }
                }
            });
        try {
            return future.get().intValue();
        } catch (InterruptedException e) {
            logger.info("Net step aborted");
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            logger.error("Exception during execution of simulation step");
        }
        return Simulator.statusDisabled;
    }

    public void dispose() {
        simulatorPlugin.removeExtension(compilationExtension);
        compilationExtension = null;
    }

    private static class SimulatorGuiCompilationExtension
            extends SimulatorExtensionAdapter {
        private CPNSimulation sim;

        public SimulatorGuiCompilationExtension(CPNSimulation sim) {
            this.sim = sim;
            logger.trace("SimGuiCompilationExt created.");
        }

        @Override
        public void simulationSetup(SimulationEnvironment env) {
            super.simulationSetup(env);
            ShadowNetSystem netSystem;
            if (sim.inGuiSetup) {
                logger.trace("SimGuiCompilationExt: setup called within guiSetup.");
                netSystem = sim.netSystem; // SNS has already been prepared
            } else {
                logger.trace("SimGuiCompilationExt: setup called concurrently.");
                netSystem = sim.getNetSystem(); // trigger full SNS generation
            }
            try {
                SimulatorPlugin.getCurrent().insertNets(netSystem);
                logger.trace("SimGuiCompilationExt: finished initial compilation.");
            } catch (NoSimulationException e) {
                logger.error("This is absurd: no simulation during simulation setup",
                             e);
            } catch (SyntaxException e) {
                if (sim.inGuiSetup) {
                    // Store the exception because the user expects feedback.
                    sim.lastSyntaxException = e;
                } else {
                    logger.warn("SyntaxException in editor nets during non-gui simulation setup:\n"
                                + e.getMessage(), e);
                }
            } catch (IllegalCompilerException e) {
                if (sim.inGuiSetup) {
                    // Store the exception because the user expects feedback.
                    sim.lastSyntaxException = e;
                } else {
                    logger.warn("IllegalCompilerException in editor nets during non-gui simulation setup:\n"
                                + e.getMessage(), e);
                }
            }
        }
    }
}