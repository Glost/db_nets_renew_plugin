package de.renew.application;

import de.renew.database.SetupHelper;
import de.renew.database.SetupHelper.SimulationState;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.ConcurrentSimulator;
import de.renew.engine.simulator.InheritableSimulationThreadLock;
import de.renew.engine.simulator.NonConcurrentSimulator;
import de.renew.engine.simulator.ParallelSimulator;
import de.renew.engine.simulator.SequentialSimulator;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.Simulator;
import de.renew.engine.simulator.SimulatorEventQueue;

import de.renew.net.IDRegistry;
import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.NetNotFoundException;
import de.renew.net.loading.Finder;
import de.renew.net.loading.NetLoader;
import de.renew.net.loading.PathlessFinder;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;
import de.renew.plugin.PropertyHelper;

import de.renew.shadow.DefaultCompiledNetLoader;
import de.renew.shadow.DefaultShadowNetLoader;
import de.renew.shadow.SNSFinder;
import de.renew.shadow.SequentialOnlyExtension;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNetLoader;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;

import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;
import de.renew.util.SingletonException;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This class serves as main facade for the Renew simulator component. It can be
 * used to set up one Renew simulation per virtual machine. The simulation
 * engine can be configured and extended in various ways.
 * <p>
 * This class combines most of the functionality of the old classes
 * <code>de.renew.gui.CPNSimulation</code> and
 * <code>de.renew.application.ShadowSimulator</code>. The old subclasses of
 * <code>ShadowSimulator</code> (<code>de.renew.remote.ServerImpl</code>,
 * <code>de.renew.access.AccessControlledServerImpl</code> and
 * <code>de.renew.workflow.WorkflowServerImpl</code>) are now covered by the
 * extension interface defined along with this class.
 * </p>
 * <p>
 * This class must be used as a singleton (and in fact all constructors and
 * methods enforce this) because some parts of the simulation engine use global
 * states in static fields.
 * </p>
 * <p>
 * One way to setup a simulation might look as follows:
 *
 * <pre>
 *        // These things need to be known beforehand:
 *        String mainNet;                   // The name of the net for
 *                                          // the initial instance.
 *        ShadowNetSystem shadowNetSystem;  // All nets needed for the
 *                                          // simulation initially.
 *
 *        // Get the simulator plugin
 *        SimulatorPlugin simulatorPlugin = SimulatorPlugin.getCurrent();
 *
 *        // Allocate storage for primary net instance reference
 *        NetInstance primaryInstance = null;
 *
 *        // Acquire mutual exclusion for operations on simulator plug-in.
 *        simulatorPlugin.lock.lock();
 *        try {
 *            // Obtain fresh simulation pool thread to set up new simulation.
 *            Future<NetInstance> future = SimulationThreadPool.getNew().submitAndWait(new Callable<NetInstance>() {
 *                public NetInstance call() throws SomeException {
 *
 *                    // Use default net loader.
 *                    simulatorPlugin.setDefaultNetLoader();
 *
 *                    // Initialise the simulation.
 *                    simulatorPlugin.setupSimulation(null);
 *
 *                    // Compile and add nets.
 *                    simulatorPlugin.insertNets(shadowNetSystem);
 *
 *                    // Create the initial net instance.
 *                    NetInstance primaryInstance = simulatorPlugin.createNetInstance(mainNet));
 *
 *                    // Start the simulation.
 *                    simulatorPlugin.getCurrentEnvironment().getSimulator().startRun();
 *
 *                    return primaryInstance;
 *                }
 *            });
 *            primaryInstance = future.get();
 *        } catch (ExecutionException e) {
 *           ...
 *        } finally {
 *                 // Release the mutual exclusion lock under any circumstances!
 *                simulatorPlugin.lock.unlock();
 *        }
 * </pre>
 *
 * </p>
 * SimulatorPlugin.java Created: Mon Jun 2 2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class SimulatorPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimulatorPlugin.class);

    /**
     * Used to synchronise access to the static <code>singleton</code> variable.
     */
    static private Object singletonLock = new Object();

    /**
     * Holds a reference to the one and only SimulatorPlugin instance. Set by
     * the {@link #SimulatorPlugin constructor} and reset by the method
     * {@link #cleanup}. To check whether the current object is still the
     * current singleton instance, call {@link #checkSingleton} at each public
     * method entry point.
     */
    static private SimulatorPlugin singleton = null;

    /**
     * Holds the set of all {@link SimulatorExtension} objects currently
     * registered with this plugin. Can be modified by {@link #addExtension} and
     * {@link #removeExtension}.
     */
    private Queue<SimulatorExtension> extensions = new ConcurrentLinkedQueue<SimulatorExtension>();

    /**
     * Holds all information about the current simulation environment.
     */
    private SimulationEnvironment currentSimulation = null;

    /**
     * Flags that the current simulation is virgin, that no nets have been added
     * yet.
     */
    private boolean virginSimulation;

    /**
     * Remembers the default shadow net loader for the current simulation, if
     * one has been requested.
     */
    private DefaultShadowNetLoader currentShadowNetLoader = null;

    /**
     * Holds the net loader to use in the next simulation setup.
     */
    private NetLoader nextNetLoader = null;

    /**
     * Holds all registered finders for the default shadow net loader. Access to
     * this set is synchronized on the set itself, not on the
     * <code>SimulatorPlugin</code> instance.
     */
    private Set<Finder> registeredFinders = Collections.synchronizedSet(new HashSet<Finder>());

    /**
     * Holds all registered pathless finders for the default shadow net loader.
     * Access to this set is synchronized on the set {@link registeredFinders},
     * not on the <code>SimulatorPlugin</code> instance or the set itself.
     */
    private Set<PathlessFinder> registeredPathlessFinders = new HashSet<PathlessFinder>();

    /**
     * Remembers whether classReinit mode was active in the last run.
     */
    private boolean previousClassReinit = false;

    /**
     * Holds a reference to the SimulationThreadPool
     */
    private SimulationThreadPool simulationPool;

    /**
     * This lock is used to synchronize access to all method calls that operate
     * on the simulation state. Since all these methods include convenience
     * wrapping and locking, users of this plugin need not to worry about it
     * unless they want to ensure atomic execution of multiple operations.
     *
     * <p>
     * When using the lock it must be acquired <em>before</em> wrapping
     * execution within a simulation thread of the simulation thread pool. It is
     * also strongly recommended to include the unlock statement in a
     * try-finally block. Example:
     * </p>
     *
     * <pre>
     *   SimulatorPlugin.lock.lock();
     *   try {
     *       Future future = SimulationThreadPool.getCurrent().submitAndWait(new Callable() {
     *           public Object call()
     *                  throws SomeException {
     *                                 ...
     *                  }
     *       });
     *
     *              return future.get();
     *   } catch (ExecutionException e) {
     *       ...
     *   } finally {
     *           lock.unlock();
     *   }
     * </pre>
     */
    static public final InheritableSimulationThreadLock lock = new InheritableSimulationThreadLock();

    /**
     * Version history:
     * 1 no header, included NetInstances and SearchQueue
     * 2 header(label, version, simulator type), NetInstances, Nets,
     *   SearchQueue - since Renew 1.2 beta 11
     * 3 small changes to ReflectionSerializer (Class[], nulls) can
     *   still read streams of version 2
     * 4 marking and search queue are now saved with time stamps
     *   incompatible change
     * 5 PlaceInstance is now an abstract class with subclasses
     *   incompatible change
     * (6) used in branch agent_serialization
     * 7 Different ID handling for net elements, introduction of remote
     *   layer - incompatible change
     * 8 Decomposed package de.renew.simulator - incompatible change
     * 9 Java version change, added assertions - incompatible change
     */
    private static final int STATE_STREAM_VERSION = 9;

    /**
     * The header identification string for all saved state streams.
     */
    private static final String STATE_STREAM_LABEL = "RenewState";

    /**
     * The name of the property to get the combined simulator mode from,
     * determining simulator class and multiplicity.
     */
    public static final String MODE_PROP_NAME = "de.renew.simulatorMode";

    /**
     * The name of the property to get the priority of the simulator thread
     */
    public static final String PRIORITY_PROP_NAME = "de.renew.simulatorPriority";

    /**
     * The name of the property to get the simulator multiplicity from.
     */
    public static final String MULTIPLICITY_PROP_NAME = "de.renew.simulatorMultiplicity";

    /**
     * The name of the property to get the simulator class name from.
     */
    public static final String CLASS_PROP_NAME = "de.renew.simulatorClass";

    /**
     * The name of the property to get the eager simulation flag from.
     */
    public static final String EAGER_PROP_NAME = "de.renew.eagerSimulation";

    /**
     * The name of the property to get the flag from that tells whether custom
     * classes should be reloaded every simulation run.
     */
    public static final String REINIT_PROP_NAME = "de.renew.classReinit";

    /**
     * Creates an instance of the simulator component facade. There can exist at
     * most one instance at any time, additional constructor calls will throw an
     * exception. To get rid of the instance, call {@link #cleanup}. Any
     * subsequent calls to methods of the old instance will raise runtime
     * exceptions.
     *
     * @param props
     *            a <code>PluginProperties</code> value
     * @exception SingletonException
     *                if there exists another singleton instance.
     *
     */
    public SimulatorPlugin(PluginProperties props) {
        super(props);
        synchronized (singletonLock) {
            if (singleton != null) {
                throw new SingletonException("At most one instance of SimulatorPlugin is allowed.");
            }
            singleton = this;
        }
        simulationPool = SimulationThreadPool.getCurrent();
    }

    /**
     * This method <b>must</b> be called at the entry point of all public
     * methods of this object to ensure that this instance is still the
     * <code>SimulatorPlugin</code> singleton instance.
     *
     * @throws SingletonException
     *             if this instance is not any more the
     *             <code>SimulatorPlugin</code> singleton instance.
     */
    private void checkSingleton() {
        if (singleton != this) {
            throw new SingletonException();
        }
    }

    /**
     * This method should be called at the entry point of all public methods
     * that need an existing simulation setup.
     *
     * @throws NoSimulationException
     *             if no simulation is set up at the time being.
     */
    private void checkSimulation() throws NoSimulationException {
        if ((currentSimulation == null)
                    || (currentSimulation.getSimulator() == null)) {
            throw new NoSimulationException();
        }
    }

    /**
     * This method is called when setting up a new simulation and ensures that
     * no other simulation is running.
     *
     * @throws SimulationRunningException
     *             if a simulation is already running.
     */
    private void checkNoSimulation() throws SimulationRunningException {
        if (currentSimulation != null) {
            throw new SimulationRunningException();
        }
    }

    /**
     * Initialises this plugin after all dependencies to other plugins have been
     * fulfilled.
     *
     * @throws SingletonException
     *             if this instance is not any more the
     *             <code>SimulatorPlugin</code> singleton instance.
     */
    public void init() {
        checkSingleton();
        setDefaultNetLoader();
        // addExtension(new RemoteExtension());
        registerDefaultNetFinder(new SNSFinder());
        PluginManager.getInstance()
                     .addCLCommand("startsimulation",
                                   new StartSimulationCommand(this));
        PluginManager.getInstance()
                     .addCLCommand("simulation",
                                   new SimulationControlCommand(this));
    }

    /**
     * Registers an extension to the simulation component. The extension will
     * become active the next time a new simulation environment is set up.
     * Active extensions will be notified about several events (see
     * {@link SimulatorExtension} interface).
     *
     * @param ext
     *            the extension to register. Duplicate registrations will be
     *            ignored.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public void addExtension(SimulatorExtension ext) {
        checkSingleton();
        logger.debug("SimulatorPlugin: Registering extension " + ext + ".");
        extensions.add(ext);
    }

    /**
     * Deregisters an extension from the simulation engine. The deregistration
     * does not cancel active extensions from any currently running simulation,
     * but they will not be activated again when the next simulation is set up.
     *
     * @param ext
     *            the extension to deregister. If the extension was not
     *            registered before, this method call is ignored.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public void removeExtension(SimulatorExtension ext) {
        checkSingleton();
        logger.debug("SimulatorPlugin: Deregistering extension " + ext + ".");
        extensions.remove(ext);
    }

    /**
     * Sets up a new simulation environment.
     * <ul>
     * <li>the simulation thread pool is reset</li>
     * <li>the set of properties is configured</li>
     * <li>all registered extensions become activated</li>
     * <li>a new simulation engine is set up</li>
     * <li>the initial net instance is created</li>
     * </ul>
     * The simulation will <b>not</b> be started, so no steps will be executed.
     *
     * <p>
     * This method will automatically create a new thread if it is not called
     * from a simulation thread. The disadvantage is that exceptions are not
     * communicated.
     * </p>
     *
     * <p>
     * The behavior of the method has changed from Renew release 2.1 to 2.2. It
     * no longer automatically terminates a running simulation. Instead, an
     * exception is thrown (see below).
     * </p>
     *
     * <p>
     * Callers of this method that switch to a simulation thread by themselves
     * should use {@link SimulationThreadPool#getNew()}. This method
     * automatically discards the new thread pool if simulation setup fails.
     * After successful execution of this method, the new thread pool becomes
     * the current thread pool and the calling thread belongs to the current
     * simulation.
     * </p>
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param props
     *            additional properties that specify this simulation
     *            environment. These properties will override any default values
     *            from the plugin properties. May be <code>null</code>.
     *
     * @exception SingletonException
     *                if this object is not any more the simulator plugin
     *                singleton instance.
     * @exception SimulationRunningException
     *                if a simulation is already running.
     * @see #lock
     */
    public void setupSimulation(final Properties props) {
        checkSingleton();

        lock.lock();

        try {
            SimulationThreadPool.getNew().executeAndWait(new Runnable() {
                    public void run() {
                        // Check that no simulation is running. The old behavior
                        // of terminating a simulation (Renew 2.1) was in conflict
                        // with the SimulationThreadPool (introduced Renew 2.2).
                        // Terminating a simulation is an asynchronous process
                        // in contrast to the immediate replacement of a current
                        // thread pool during setup. It would be probable that
                        // some pending events of the old simulation would be
                        // executed within the new thread pool.
                        try {
                            checkNoSimulation();
                        } catch (SimulationRunningException e) {
                            SimulationThreadPool.discardNew();
                            throw e;
                        }

                        restartThreadPool();
                        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";

                        // Combine the plugin properties with the specified
                        // properties from this method call. Disconnect the
                        // active property set from the plugin properties by
                        // copying all entries.
                        final Properties activeProperties = new Properties();
                        activeProperties.putAll(getProperties());
                        if (props != null) {
                            activeProperties.putAll(props);
                        }
                        int maxPriority = PropertyHelper.getIntProperty(activeProperties,
                                                                        PRIORITY_PROP_NAME,
                                                                        Thread.NORM_PRIORITY);
                        if (simulationPool.getMaxPriority() != maxPriority) {
                            simulationPool.setMaxPriority(maxPriority);
                        }
                        SimulatorEventQueue.initialize();
                        logger.debug("SimulatorPlugin: Setting up simulation.");
                        // Take a snapshot of registered extensions, these are
                        // now our active extensions.
                        SimulatorExtension[] activeExtensions = extensions
                            .toArray(new SimulatorExtension[extensions.size()]);
                        // Configure class reloading, if requested.
                        possiblySetupClassSource(activeProperties);

                        // Ensure that all old nets have been forgotten and
                        // set the new net loader.
                        Net.forgetAllNets();
                        Net.setNetLoader(nextNetLoader);
                        if (nextNetLoader instanceof DelayedDelegationNetLoader) {
                            synchronized (registeredFinders) {
                                logger.debug("SimulatorPlugin: Creating default shadow net loader.");
                                currentShadowNetLoader = new DefaultShadowNetLoader(activeProperties);
                                for (Finder finder : registeredFinders) {
                                    currentShadowNetLoader.registerFinder(finder);
                                }
                                for (PathlessFinder finder : registeredPathlessFinders) {
                                    currentShadowNetLoader
                                        .registerPathlessFinder(finder);
                                }
                            }
                            logger.debug("SimulatorPlugin: Configuring delayed net loader.");
                            ((DelayedDelegationNetLoader) nextNetLoader)
                                .setNetLoader(new DefaultCompiledNetLoader(currentShadowNetLoader));
                        }

                        // Create the simulation engine with respect to the
                        // current properties.
                        Simulator simulator = newSimulator(activeProperties);

                        // Store all information in a new simulation environment.
                        currentSimulation = new SimulationEnvironment(simulator,
                                                                      activeExtensions,
                                                                      activeProperties);
                        virginSimulation = true;

                        // Inform all active extensions about the simulation setup.
                        for (int i = 0; i < activeExtensions.length; i++) {
                            activeExtensions[i].simulationSetup(currentSimulation);
                        }

                        // Register this as exit blocker as long as the simulation
                        // is
                        // active.
                        PluginManager.getInstance()
                                     .blockExit(SimulatorPlugin.getCurrent());
                    }
                });
        } finally {
            lock.unlock();
        }
    }

    /**
     * Create a new simulator. The actual object that is created depends on the
     * given properties. The properties will be updated with respect to the
     * chosen simulator class and multiplicity. A zero multiplicity denotes a
     * sequential simulator, which is minimally concurrent.
     *
     * This method must be called from a simulation thread.
     */
    private static Simulator newSimulator(final Properties props) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        int simulatorMode = PropertyHelper.getIntProperty(props,
                                                          MODE_PROP_NAME, 1);
        int simulatorMultiplicity = PropertyHelper.getIntProperty(props,
                                                                  MULTIPLICITY_PROP_NAME,
                                                                  1);

        Class<?> simulatorClass = PropertyHelper.getClassProperty(props,
                                                                  CLASS_PROP_NAME,
                                                                  Simulator.class);
        if (simulatorClass != null) {
            logger.debug("Using simulator class " + simulatorClass.getName()
                         + " with " + simulatorMultiplicity + " simulators ...");
        }
        if (simulatorClass == null) {
            simulatorMultiplicity = simulatorMode;
            if (simulatorMultiplicity == 0 || simulatorMultiplicity == -1) {
                simulatorMultiplicity = 0;
                logger.info("Using sequential simulator ...");
                simulatorClass = NonConcurrentSimulator.class;
            } else if (simulatorMultiplicity < -1) {
                logger.warn("Using " + (-simulatorMultiplicity)
                            + " sequential simulators ...");
                logger.warn("Caution! This is an experimental feature!");
                simulatorClass = ParallelSimulator.class;
            } else if (simulatorMultiplicity > 1) {
                logger.warn("Using " + simulatorMultiplicity
                            + " concurrent simulators ...");
                logger.warn("Caution! This is an experimental feature!");
                simulatorClass = ParallelSimulator.class;
            } else {
                simulatorClass = ConcurrentSimulator.class;
                logger.info("Using default concurrent simulator ...");
            }
        }
        assert simulatorClass != null : "Simulator class not determined."
        + "Properties were: Mode=" + props.getProperty(MODE_PROP_NAME)
        + " Multiplicity=" + props.getProperty(MULTIPLICITY_PROP_NAME)
        + " Class=" + props.getProperty(CLASS_PROP_NAME);

        boolean eagerSimulation = PropertyHelper.getBoolProperty(props,
                                                                 EAGER_PROP_NAME);
        if (eagerSimulation) {
            logger.info("Using eager simulation mode.");
        }

        Simulator simulator;
        if (SequentialSimulator.class.equals(simulatorClass)) {
            simulator = new SequentialSimulator(!eagerSimulation);
            simulatorMultiplicity = 0;
        } else if (NonConcurrentSimulator.class.equals(simulatorClass)) {
            simulator = new NonConcurrentSimulator(!eagerSimulation);
            simulatorMultiplicity = 0;
        } else if (ConcurrentSimulator.class.equals(simulatorClass)) {
            simulator = new ConcurrentSimulator(!eagerSimulation);
            simulatorMultiplicity = 1;
        } else if (ParallelSimulator.class.equals(simulatorClass)) {
            simulator = new ParallelSimulator(simulatorMultiplicity,
                                              !eagerSimulation);
        } else {
            logger.error("Sorry, only known simulators can be instantiated for the time being.");
            simulator = new ConcurrentSimulator(!eagerSimulation);
            simulatorClass = ConcurrentSimulator.class;
            simulatorMultiplicity = 1;
        }
        assert simulator != null : "Simulator has not been set!?";

        props.setProperty(CLASS_PROP_NAME, simulatorClass.getName());
        props.setProperty(MULTIPLICITY_PROP_NAME,
                          Integer.toString(simulatorMultiplicity));

        return simulator;
    }

    /**
     * Tells whether the simulation is currently active. If this method returns
     * <code>true</code>, a simulation has been set up <i>and</i> is still
     * active (see documentation of {@link Simulator#isActive}).
     *
     * This method will automatically create a new thread if it is not called
     * from a simulation thread
     *
     * @return <code>true</code>, if the simulation is active (see above).
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public boolean isSimulationActive() {
        checkSingleton();

        Boolean returnValue;
        synchronized (new Object()) {
            returnValue = currentSimulation != null
                          && currentSimulation.getSimulator().isActive();
        }
        return returnValue;

    }

    /**
     * Returns the current simulation environment, if a simulation has been set
     * up.
     * <p>
     * Do not expect that the data provided here has any guaranteed life span -
     * if you want to be informed about the termination of the simulation, write
     * and register a {@link SimulatorExtension}.
     * </p>
     *
     * @return a <code>SimulationEnvironment</code> object describing the actual
     *         simulation setup.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public SimulationEnvironment getCurrentEnvironment() {
        return currentSimulation;
    }

    /**
     * Adds net templates based on the given shadow nets to the current
     * simulation.
     * <p>
     * When this method is called on a fresh simulation setup, the given net
     * system's informations about shadow net loader and compiler are extracted
     * and kept for the simulation lifetime. In this case, the given net system
     * <i>must</i> be configured with a <code>ShadowNetCompiler</code>. However,
     * the setting of the <code>ShadowNetLoader</code> is a <i>can</i> option.
     * </p>
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param netSystem
     *            holds all nets to be compiled into this simulation
     *            environment. The state of the net system will change during
     *            the insertion process: nets are marked as compiled, and the
     *            default shadow net loader is configured (optional).
     *
     * @return the lookup resulting from the compilation.
     *
     * @exception SyntaxException
     *                if an error occurs during the compilation process.
     *
     * @exception NullPointerException
     *                if the <code>netSystem</code> is <code>null</code>.
     *                <p>
     *                This exception could also be thrown if the
     *                {@link DefaultCompiledNetLoader} has been requested for
     *                this simulation setup (via {@link #setDefaultNetLoader}),
     *                this is the first <code>netSystem</code> and there is no
     *                net loader configured within the net system.
     *                </p>
     *
     * @exception NoSimulationException
     *                if there is no simulation set up.
     *
     * @exception SingletonException
     *                if this object is not any more the simulator plugin
     *                singleton instance.
     * @see #lock
     */
    public ShadowLookup insertNets(final ShadowNetSystem netSystem)
            throws SyntaxException, NoSimulationException {
        checkSingleton();

        lock.lock();
        try {
            Future<ShadowLookup> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<ShadowLookup>() {
                    public ShadowLookup call()
                            throws SyntaxException, NoSimulationException {
                        ShadowLookup lookup;
                        checkSimulation();

                        if (netSystem == null) {
                            throw new NullPointerException("Missing shadow net system.");
                        }

                        // Apply default net loader if requested.
                        ShadowNetLoader netLoader = netSystem
                                              .getNetLoader();
                        if ((netLoader == null)
                                    && (currentShadowNetLoader != null)) {
                            logger.debug("SimulatorPlugin: Applying default shadow net loader to net system.");
                            netSystem.setNetLoader(currentShadowNetLoader);
                        }

                        // Compile nets.
                        if (virginSimulation) {
                            logger.debug("SimulatorPlugin: Compiling first net system.");
                            lookup = netSystem.compile();
                        } else {
                            logger.debug("SimulatorPlugin: Adding another net system.");
                            lookup = netSystem.compileMore();
                        }
                        logger.debug("SimulatorPlugin: Compilation result lookup: "
                                     + lookup);

                        // Check whether compilation result fits into
                        // the
                        // current
                        // simulation with respect to sequential step
                        // requirements.
                        SequentialOnlyExtension seqEx = SequentialOnlyExtension
                                                        .lookup(lookup);
                        boolean sequentialOnly = seqEx.getSequentialOnly();
                        if (sequentialOnly
                                    && !currentSimulation.getSimulator()
                                                                 .isSequential()) {
                            throw new SyntaxException("Some nets need a sequential simulator.");
                            // TODO: add error objects by asking seqEx
                        }

                        // Now we are sure that the nets can be added to
                        // the
                        // simulation.
                        // Unset the virgin flag.
                        virginSimulation = false;

                        // Inform all active extensions about the new
                        // nets.
                        SimulatorExtension[] activeExtensions = currentSimulation
                                                                .getExtensions();
                        for (int i = 0; i < activeExtensions.length; i++) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(SimulatorPlugin.class.getName()
                                             + ": Active Extension compile net "
                                             + activeExtensions[i].toString());
                            }
                            activeExtensions[i].netsCompiled(lookup);
                        }

                        // Insert all compiled nets into the running
                        // simulation.
                        lookup.makeNetsKnown();
                        return lookup;
                    }
                });

            return future.get();
        } catch (InterruptedException e) {
            logger.info("Simulation ended");
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof SyntaxException) {
                throw ((SyntaxException) t);
            } else if (t instanceof NoSimulationException) {
                throw ((NoSimulationException) t);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }

        // We should never return nothing but some error occurred before.
        return null;

    }

    /**
     * If the corresponding properties have been set, the simulation will be
     * connected to a database engine and the last state from the database will
     * be restored. The net structures on which the state is based have to be
     * known before this method is called.
     * <p>
     * If the database properties are <i>not</i> set, this method will return
     * silently without modifying the simulation state. The return value will
     * then be the
     * {@link de.renew.database.SetupHelper.SimulationState#TERMINATED_STATE}
     * object. This object is also returned when the database connection failed.
     * If the database is configured correctly, the result will be an individual
     * state object, whether or not there was a state to restore.
     * </p>
     * <p>
     * If this method is called after the current simulation has already
     * performed some steps, i.e. created its own state, the result is
     * undefined.
     * </p>
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @return A <code>SimulationState</code> object describing the result of
     *         the operation (see comments above).
     *
     * @exception NoSimulationException
     *                if there is no simulation set up.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     *
     * @see SetupHelper#setup
     * @see #lock
     */
    public SimulationState restoreStateFromDatabase() {
        checkSingleton();

        lock.lock();
        try {
            Future<SimulationState> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<SimulationState>() {
                    public SimulationState call() throws NoSimulationException {
                        SimulationState state;
                        checkSimulation();
                        state = SetupHelper.setup(currentSimulation
                                                 .getProperties());
                        return state;
                    }
                });
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof NoSimulationException) {
                throw (NoSimulationException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Writes the given <code>NetInstance</code>s as well as all known
     * <code>Net</code>s and the <code>SearchQueue</code> contents and some
     * additional information to the stream. The written information is
     * sufficient to continue the simulation from the same state after
     * deserialization.
     * <p>
     * If the given <code>ObjectOutput</code> is a <b>
     * {@link de.renew.util.RenewObjectOutputStream}</b>, its feature of cutting
     * down the recursion depth by delaying the serialization of some fields
     * will be used.
     * </p>
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param output
     *            target stream (see note about
     *            <code>RenewObjectOutputStream</code> above).
     *
     * @param instances
     *            an array of net instances to be explicitly included in the
     *            saved state (e.g. instances displayed to the user).
     *
     * @exception IOException
     *                if an error occurs during the serialisation to the output
     *                stream.
     *
     * @exception NoSimulationException
     *                if there is no simulation set up.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     *
     * @see #lock
     */
    public void saveState(final ObjectOutput output,
                          final NetInstance[] instances)
            throws IOException {
        checkSingleton();

        lock.lock();
        try {
            Future<Object> future = simulationPool.submitAndWait(new Callable<Object>() {
                    public Object call()
                            throws IOException, NoSimulationException {
                        checkSimulation();
                        currentSimulation.getSimulator().stopRun();

                        // Use the domain trace feature of the
                        // RenewObjectOutputStream, if available.
                        RenewObjectOutputStream rOut = null;
                        if (output instanceof RenewObjectOutputStream) {
                            rOut = (RenewObjectOutputStream) output;

                        }
                        if (rOut != null) {
                            rOut.beginDomain(SimulatorPlugin.class);
                        }

                        // Write the header, which contains:
                        // - label
                        // - file format version number
                        // - type of simulator used
                        output.writeObject(STATE_STREAM_LABEL);
                        output.writeInt(STATE_STREAM_VERSION);
                        output.writeInt(PropertyHelper.getIntProperty(currentSimulation
                                                                      .getProperties(),
                                                                      MULTIPLICITY_PROP_NAME));

                        // First part: save all NetInstances explicitly
                        // named.
                        // They are not necessarily sufficient to describe
                        // the
                        // simulation state completely.
                        output.writeInt(instances.length);
                        for (int i = 0; i < instances.length; i++) {
                            output.writeObject(instances[i]);
                        }

                        // If a RenewObjectOutputStream is used, write
                        // all delayed fields NOW.
                        if (rOut != null) {
                            rOut.writeDelayedObjects();

                        }

                        // Second part: save all Nets currently known by
                        // the Net.forName() lookup mechanism.
                        // This ensures that the static part of all compiled
                        // nets will be available on deserialization.
                        Net.saveAllNets(output);

                        // Third part: add all entries from the SearchQueue.
                        // These entries alone are sufficient to describe
                        // the
                        // current simulation state completely.
                        // But this information does not neccessarily
                        // include
                        // all
                        // nets possibly required by future simulation
                        // steps.
                        SearchQueue.saveQueue(output);

                        // Last part: Restore the ID registry with its
                        // well-known
                        // IDs for every token.
                        IDRegistry.save(output);

                        if (rOut != null) {
                            rOut.endDomain(SimulatorPlugin.class);
                        }
                        return null;
                    }
                });
            future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) {
                throw ((IOException) t);
            } else if (t instanceof NoSimulationException) {
                throw (NoSimulationException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restores a simulation saved by {@link #saveState}. Reads all stored
     * <code>Net</code>s and <code>NetInstance</code>s. The result is a
     * ready-to-run simulation setup. Some net instances (those that have been
     * explicitly referenced in the <code>saveState</code> call) are returned so
     * they can be processed specially (e.g. open instance drawing windows).
     * <p>
     * If the given <code>ObjectInput</code> is a <b>
     * {@link de.renew.util.RenewObjectInputStream}</b>, the neccessary steps to
     * cover delayed serialization will be made. The ObjectInputStream will be
     * read using <code>de.renew.util.ClassSource</code> to provide its ability
     * of reloading all user defined classes.
     * </p>
     *
     * <p>
     * This method will automatically create a new thread if it is not called
     * from a simulation thread. Contrary to the method
     * {@link #setupSimulation(Properties)}, exceptions are communicated anyway.
     * </p>
     *
     * <p>
     * The behavior of the method has changed from Renew release 2.1 to 2.2. It
     * no longer automatically terminates a running simulation. Instead, an
     * exception is thrown (see below).
     * </p>
     *
     * <p>
     * Callers of this method that switch to a simulation thread by themselves
     * should use {@link SimulationThreadPool#getNew()}. This method
     * automatically discards the new thread pool if simulation setup fails.
     * After successful execution of this method, the new thread pool becomes
     * the current thread pool and the calling thread belongs to the current
     * simulation.
     * </p>
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param input
     *            source stream (see note about
     *            <code>RenewObjectInputStream</code> above).
     *
     * @param props
     *            additional properties that specify this simulation
     *            environment. These properties will override any default values
     *            from the plugin properties. May be <code>null</code>.
     *
     * @return array of explicitly stored net instances (e.g. to be displayed to
     *         the user).
     *
     * @exception IOException
     *                if an error occurs during reading the input stream
     *
     * @exception ClassNotFoundException
     *                if an unknown object type was included in the state
     *
     * @exception SingletonException
     *                if this object is not any more the simulator plugin
     *                singleton instance.
     * @exception SimulationRunningException
     *                if a simulation is already running.
     * @see #lock
     */
    public NetInstance[] loadState(final ObjectInput input,
                                   final Properties props)
            throws IOException, ClassNotFoundException,
                           SimulationRunningException {
        checkSingleton();

        lock.lock();
        try {
            Future<NetInstance[]> future = SimulationThreadPool.getNew().submitAndWait(new Callable<NetInstance[]>() {
                    public NetInstance[] call()
                            throws IOException, ClassNotFoundException,
                                           SimulationRunningException {
                        List<NetInstance> explicitInstances;
                        try {
                            logger.debug("Loading simulation state...");

                            setupSimulation(props);

                            // Check for valid header, which includes:
                            // - label
                            // - file format version number
                            // - type of simulator used
                            String streamLabel = (String) input.readObject();

                            if (!streamLabel.equals(STATE_STREAM_LABEL)) {
                                throw new StreamCorruptedException("Stream does not seem to contain renew state data.");
                            }
                            int streamVersion = input.readInt();

                            if (streamVersion == STATE_STREAM_VERSION) {
                                // that's ok.
                            } else {
                                // that's most probably not ok.
                                throw new StreamCorruptedException("State data is of different version "
                                                                   + "("
                                                                   + streamVersion
                                                                   + ") than the current version ("
                                                                   + STATE_STREAM_VERSION
                                                                   + ").");
                            }

                            int simulatorMultiplicity = PropertyHelper
                                               .getIntProperty(currentSimulation
                                                               .getProperties(),
                                                               MULTIPLICITY_PROP_NAME,
                                                               1);
                            int streamSimulatorMultiplicity = input.readInt();

                            if (streamSimulatorMultiplicity != simulatorMultiplicity) {
                                logger.warn("Simulation state was saved "
                                            + "using a different simulator multiplicity "
                                            + "(" + streamSimulatorMultiplicity
                                            + ") "
                                            + "than currently selected ("
                                            + simulatorMultiplicity + ").");
                            }

                            // First part: read all NetInstances stored
                            // explicitly.
                            int count = input.readInt();
                            explicitInstances = new ArrayList<NetInstance>(count);
                            try {
                                for (int i = 0; i < count; i++) {
                                    explicitInstances.add((NetInstance) de.renew.util.ClassSource
                                                          .readObject(input));
                                }
                            } catch (ClassCastException e) {
                                logger.debug(e.getMessage(), e);
                                throw new StreamCorruptedException("Object other than NetInstance found "
                                                                   + "when looking for net instances: "
                                                                   + e
                                               .getMessage());
                            }

                            // If a RenewObjectInputStream is used, read
                            // all delayed fields NOW.
                            if (input instanceof RenewObjectInputStream) {
                                ((RenewObjectInputStream) input)
                                               .readDelayedObjects();
                            }

                            // Second part: read all compiled Nets
                            Net.loadNets(input);

                            // Third part: add all neccessary entries to the
                            // SearchQueue.
                            SearchQueue.loadQueue(input);

                            // Last part: reestablish the global Id registry
                            // for
                            // token IDs.
                            IDRegistry.load(input);
                        } catch (SimulationRunningException e) {
                            // If a RunningSimulationException occurs,
                            // pass it to the caller. We assume that
                            // the running simulation is still intact.
                            throw e;
                        } catch (IOException e) {
                            // If an exception occurs, pass it to the
                            // caller.
                            // But restore a known state first - the 'no
                            // simulation active' state.
                            terminateSimulation();
                            throw e;
                        } catch (ClassNotFoundException e) {
                            // The same as above, but for another exception
                            // type...
                            terminateSimulation();
                            throw e;
                        } catch (StackOverflowError e) {
                            // The same as above, but for another exception
                            // type...
                            terminateSimulation();
                            throw e;
                        }

                        // Return the list of read NetInstances.
                        return explicitInstances.toArray(new NetInstance[explicitInstances
                                                                         .size()]);
                    }
                });
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof SimulationRunningException) {
                throw new SimulationRunningException(e);
            } else if (t instanceof IOException) {
                throw ((IOException) t);
            } else if (t instanceof ClassNotFoundException) {
                throw new ClassNotFoundException(t.getMessage(), e);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }

        // We should never return nothing but some error occurred before.
        return null;
    }

    /**
     * Configure the <code>NetLoader</code> to use in the next simulation setup.
     * There is no way to change the net loader in an already set up simulation.
     *
     * @param loader
     *            the <code>NetLoader</code> to use in the next simulation. If
     *            <code>null</code>, no net loader will be used during the next
     *            simulation.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public synchronized void setNetLoader(NetLoader loader) {
        checkSingleton();
        logger.debug("SimulatorPlugin: Configuring net loader " + loader + ".");
        nextNetLoader = loader;
    }

    /**
     * Configure the {@link DefaultCompiledNetLoader} to be used in the next
     * simulation setup. This net loader will be connected to the plugin's
     * shadow net system.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public synchronized void setDefaultNetLoader() {
        setNetLoader(new DelayedDelegationNetLoader());
    }

    /**
     * Registers a finder for the default shadow net loader. Finders are without
     * effect if the default shadow net loader is overridden (either as plug-in
     * configuration or by an individual shadow net system).
     *
     * @param finder
     *            the shadow net file finder to add to the default shadow net
     *            loader.
     *
     * @see DefaultShadowNetLoader#registerFinder
     */
    public void registerDefaultNetFinder(final Finder finder) {
        synchronized (registeredFinders) {
            registeredFinders.add(finder);
            if (currentShadowNetLoader != null) {
                currentShadowNetLoader.registerFinder(finder);
            }
        }
    }

    /**
     * Deregisters a finder from the default shadow net loader.
     *
     * @param finder
     *            the shadow net file findet to remove from the default shadow
     *            net loader.
     *
     * @see DefaultShadowNetLoader#removeFinder
     */
    public void removeDefaultNetFinder(final Finder finder) {
        synchronized (registeredFinders) {
            registeredFinders.remove(finder);
            if (currentShadowNetLoader != null) {
                currentShadowNetLoader.removeFinder(finder);
            }
        }
    }

    /**
     * Registers a pathless finder for the default shadow net loader. Finders
     * are without effect if the default shadow net loader is overridden (either
     * as plug-in configuration or by an individual shadow net system).
     *
     * @param finder
     *            the shadow net file finder to add to the default shadow net
     *            loader.
     *
     * @see DefaultShadowNetLoader#registerPathlessFinder
     */
    public void registerDefaultPathlessFinder(final PathlessFinder finder) {
        synchronized (registeredFinders) {
            registeredPathlessFinders.add(finder);
            if (currentShadowNetLoader != null) {
                currentShadowNetLoader.registerPathlessFinder(finder);
            }
        }
    }

    /**
     * Deregisters a pathless finder from the default shadow net loader.
     *
     * @param finder
     *            the shadow net file finder to remove from the default shadow
     *            net loader.
     *
     * @see DefaultShadowNetLoader#removePathlessFinder
     */
    public void removeDefaultPathlessFinder(final PathlessFinder finder) {
        synchronized (registeredFinders) {
            registeredFinders.remove(finder);
            if (currentShadowNetLoader != null) {
                currentShadowNetLoader.removePathlessFinder(finder);
            }
        }
    }

    /**
     * Creates a net instance within the current simulation.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param net
     *            the name of the net template for the net instance to build. If
     *            <code>null</code>, then no net instance will be created.
     *
     * @return the created netInstance. Returns <code>null</code> if the
     *         instance creation failed.
     *
     * @throws NetNotFoundException
     *             if the instance creation failed because no net with the given
     *             name could be found.
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     * @see #lock
     */
    public NetInstance createNetInstance(final String net)
            throws NetNotFoundException, NoSimulationException {
        checkSingleton();

        lock.lock();
        try {
            Future<NetInstance> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<NetInstance>() {
                    public NetInstance call()
                            throws NetNotFoundException, NoSimulationException {
                        NetInstance netInstance = null;
                        checkSimulation();

                        // Create the first net instance
                        if (net != null) {
                            Net netTemplate = Net.forName(net);
                            if (netTemplate != null) {
                                netInstance = netTemplate.buildInstance(currentSimulation.getSimulator()
                                                                                         .nextStepIdentifier());
                                currentSimulation.getSimulator().refresh();
                            }
                        }
                        return netInstance;
                    }
                });

            return future.get();
        } catch (InterruptedException e) {
            logger.info("Creation of NetInstances was aborted");
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof NetNotFoundException) {
                throw (NetNotFoundException) t;
            } else if (t instanceof NoSimulationException) {
                throw (NoSimulationException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }

        // We should never return nothing but some error occurred before.
        return null;

    }

    /**
     * Terminates the current simulation. If no simulation has been set up,
     * nothing happens.
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     *
     * @see #lock
     */
    public void terminateSimulation() {
        checkSingleton();

        lock.lock();

        try {
            SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                    public void run() {
                        if (currentSimulation != null) {
                            logger.debug("SimulatorPlugin: Stopping simulation.");

                            SimulatorExtension[] exts = currentSimulation
                                .getExtensions();
                            for (int i = 0; i < exts.length; i++) {
                                exts[i].simulationTerminating();
                            }

                            // Stop the engine.
                            currentSimulation.getSimulator().terminateRun();

                            exts = currentSimulation.getExtensions();
                            for (int i = 0; i < exts.length; i++) {
                                exts[i].simulationTerminated();
                            }

                            // Go back to simulation time 0 and
                            // clear all outstanding search requests.
                            // SearchQueue.reset(0);
                            SearchQueue.reset(0);
                            // Clear all token IDs that might be
                            // still registered.
                            IDRegistry.reset();

                            // Forget all net structures.
                            Net.forgetAllNets();

                            // Retract our exit blocker because the simulation is
                            // over.
                            PluginManager.getInstance()
                                         .exitOk(SimulatorPlugin.getCurrent());

                            // We should not clean the thread pool here.
                            // Simulation is terminated asynchronously.
                            // There still may be requests for a simulation thread.
                            // SimulationThreadPool.getCurrent().cleanup();
                            currentSimulation = null;
                            currentShadowNetLoader = null;
                        }
                    }
                });
        } finally {
            lock.unlock();
        }
    }

    private void restartThreadPool() {
        SimulationThreadPool.cleanup();
        simulationPool = SimulationThreadPool.getSimulationThreadPool();
    }

    /**
     * Stops any running simulation and clears all data used by the simulator.
     * If the cleanup was successful (returns <code>true</code>) this
     * <code>SimulatorPlugin</code> instance is rendered useless and all future
     * method calls will throw <code>SingletonException</code>s.
     *
     * @return <code>true</code>, if the cleanup was succesfully finished and
     *         this object has lost its singleton status. <br>
     *         <code>false</code>, if the cleanup failed. This object is still
     *         the singleton for any simulator access.
     *
     * @throws SingletonException
     *             if this object has already lost the singleton instance status
     *             before.
     */
    public synchronized boolean cleanup() {
        synchronized (singletonLock) {
            checkSingleton();
            terminateSimulation();

            // if (result=false) return immediately before releasing
            // the singleton!
            singleton = null;
        }
        return true;
    }

    /**
     * Describe <code>canShutDown</code> method here.
     *
     * @return a <code>boolean</code> value
     *
     * @throws SingletonException
     *             if this object is not any more the simulator plugin singleton
     *             instance.
     */
    public synchronized boolean canShutDown() {
        checkSingleton();
        return true;
    }

    /**
     * Executes the given {@link Callable} in a simulation thread and waits for
     * its computation to finish.
     * <p>
     * This facade method just delegates to the current
     * {@link SimulationThreadPool} instance.  For the caller's convenience, the
     * resulting {@link Future} is unpacked immediately.  If an
     * {@link ExecutionException} occurs, the causing exception is unpacked or
     * converted into a {@link RuntimeException}.  If there is no simulation
     * running, this method exits with a {@link NoSimulationException}.
     * </p>
     *
     * @param task  the computation to be executed in a simulation thread.
     *
     * @return  the computation result.
     *
     * @throws SingletonException
     *             if this object is not any more the simulator
     *             plugin singleton instance.
     *
     * @throws NoSimulationException
     *             if there is no simulation set up.
     *
     * @see SimulationThreadPool#submitAndWait(Callable)
     **/
    public <T> T submitAndWait(Callable<T> task) throws InterruptedException {
        checkSingleton();
        lock.lock();
        try {
            checkSimulation();
            Future<T> futureObj = simulationPool.submitAndWait(task);
            return futureObj.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof NoSimulationException) {
                throw (NoSimulationException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception: " + t, e);
                throw new RuntimeException(e);
            }
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    /**
     * Make sure that the user-level classes get reloaded for the next compiler
     * run, if the class reinit mode has been configured. This method is called
     * automatically by {@link #setupSimulation}.
     *
     * This method will automatically create a new thread if it is not called
     * from a simulation thread
     *
     * <p>
     * Access to this method is exclusive. The Java synchronized mechanism is
     * replaced by a specialized {@link #lock lock}. How to achieve
     * synchronization across multiple methods is explained there.
     * </p>
     *
     * @param props
     *            the configuration to extract the {@link #REINIT_PROP_NAME}
     *            property from.
     * @throws IllegalStateException
     * @throws Exception
     *
     * @exception IllegalStateException
     *                if there is an active simulation.
     *
     * @exception SingletonException
     *                if this object is not any more the simulator plugin
     *                singleton instance.
     *
     * @see #lock
     */
    public void possiblySetupClassSource(final Properties props)
            throws IllegalStateException {
        checkSingleton();

        lock.lock();
        try {
            Future<Object> result = simulationPool.submitAndWait(new Callable<Object>() {
                    public Object call() throws IllegalStateException {
                        if (isSimulationActive()) {
                            throw new IllegalStateException("Reconfiguration of class source is "
                                                            + "not allowed while a simulation is running.");
                        }
                        boolean classReinit = PropertyHelper.getBoolProperty(props,
                                                                             REINIT_PROP_NAME);
                        if (classReinit) {
                            if (classReinit != previousClassReinit) {
                                logger.info("Using classReinit mode.");
                            } else {
                                logger.debug("SimulatorPlugin: Re-initialising class loader.");
                            }

                            // In Renew 2.x SelectiveClassLoader was
                            // replaced by
                            // BottomClassLoader.
                            //
                            // SelectiveClassLoader classLoader = new
                            // SelectiveClassLoader();
                            // classLoader.setSelectors(new String[] {
                            // "de.renew.util.ReloadableDeserializerImpl",
                            // "-java",
                            // "-collections.", "-CH.ifa.draw.",
                            // "-de.renew.",
                            // "-de.uni_hamburg.fs." });
                            ClassLoader classLoader = PluginManager.getInstance()
                                                                   .getNewBottomClassLoader();
                            de.renew.util.ClassSource.setClassLoader(classLoader);
                        } else if (previousClassReinit) {
                            logger.debug("classReinit mode disabled.");
                            de.renew.util.ClassSource.setClassLoader(null);
                        }
                        previousClassReinit = classReinit;

                        return null;
                    }
                });

            result.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IllegalStateException) {
                throw ((IllegalStateException) t);
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                logger.error("Simulation thread threw an exception", e);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Provides a reference to the current Renew Simulator plugin instance. The
     * instance is queried from the plugin management system. So the result will
     * be <code>null</code>, if the simulator plugin is not activated.
     *
     * @return the active simulator plugin instance, if there is any. Returns
     *         <code>null</code> otherwise.
     */
    public static SimulatorPlugin getCurrent() {
        Iterator<IPlugin> plugins = PluginManager.getInstance()
                                                 .getPluginsProviding("de.renew.simulator")
                                                 .iterator();
        while (plugins.hasNext()) {
            IPlugin plugin = plugins.next();
            if (plugin instanceof SimulatorPlugin) {
                return (SimulatorPlugin) plugin;
            }
        }
        return singleton;
    }

    /**
     * This net loader serves as placeholder. It denies all
     * <code>loadNet()</code> requests until the real net loader has been
     * configured.
     */
    private class DelayedDelegationNetLoader implements NetLoader {
        private NetLoader netLoader = null;

        public void setNetLoader(NetLoader netLoader) {
            this.netLoader = netLoader;
        }

        public Net loadNet(final String netName) throws NetNotFoundException {
            if (netLoader == null) {
                throw new NetNotFoundException("No net loader configured.");
            } else {
                return netLoader.loadNet(netName);
            }
        }
    }
}