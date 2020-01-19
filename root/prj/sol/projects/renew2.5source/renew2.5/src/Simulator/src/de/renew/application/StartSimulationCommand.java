package de.renew.application;

import de.renew.database.SetupHelper.SimulationState;
import de.renew.database.TransactionSource;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.net.NetInstance;

import de.renew.plugin.command.CLCommand;

import de.renew.shadow.ShadowNetSystem;

import de.renew.util.ParameteredCommandLine;

import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * This command line command sets up a simulation based on the given shadow net
 * system file and primary net name. It can also print a usage help text.
 * <p>
 * This command always tries to reestablish a database backed state if the
 * required properties are set. If such a state is found, the primary net does
 * not get instantiated (as it would be its second instantiation).
 * </p>
 * StartSimulationCommand.java Created: Wed Jul 16 2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class StartSimulationCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StartSimulationCommand.class);
    private SimulatorPlugin plugin;

    /**
     * Creates a new <code>StartSimulationCommand</code> instance.
     *
     * @param plugin
     *            neccessary reference to the simulator plugin instance where
     *            this command belongs to.
     *
     * @throws NullPointerException
     *             if <code>plugin</code> is <code>null</code>.
     */
    public StartSimulationCommand(SimulatorPlugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("Need SimulatorPlugin reference.");
        }
        this.plugin = plugin;
    }

    /**
     * Sets up a simulation or prints a help text. See this class documentation
     * for further details.
     *
     * @param args
     *            arguments to this command, specifying the sns file, the name
     *            of the main net and other optional arguments (see output of
     *            the <code>showSyntax()</code> method).
     * @param response
     *            the <code>PrintStream</code> for user feedback.
     */
    public void execute(final String[] args, final PrintStream response) {
        final ParameteredCommandLine line = new ParameteredCommandLine(args,
                                                                       new String[] { "-h", "-i" },
                                                                       new int[] { 0, 0 });
        final String[] basicArgs = line.getRemainingArgs();

        if (basicArgs.length != 2 || line.hasParameter("-h")) {
            showSyntax(response);
        } else {
            SimulatorPlugin.lock.lock();
            try {
                Future<Object> object = SimulationThreadPool.getNew().submitAndWait(new Callable<Object>() {
                        public Object call() throws Exception {
                            String netSystemFileName = basicArgs[0];
                            String primaryNetName = basicArgs[1];

                            // Load the shadow net system
                            FileInputStream stream = new FileInputStream(netSystemFileName);
                            ShadowNetSystem netSystem;
                            ObjectInput input = null;
                            try {
                                input = new ObjectInputStream(stream);


                                netSystem = (ShadowNetSystem) input.readObject();
                            } catch (StreamCorruptedException e) {
                                throw new IllegalArgumentException("Invalid shadow net system (in file "
                                                                   + netSystemFileName
                                                                   + "): " + e);
                            } catch (ClassCastException e) {
                                throw new IllegalArgumentException("Invalid shadow net system (in file "
                                                                   + netSystemFileName
                                                                   + "): " + e);
                            } catch (ClassNotFoundException e) {
                                throw new IllegalArgumentException("Invalid shadow net system (in file "
                                                                   + netSystemFileName
                                                                   + "): " + e);
                            } finally {
                                try {
                                    if (stream != null) {
                                        stream.close();
                                    }
                                    if (input != null) {
                                        input.close();
                                    }
                                } catch (Exception e) {
                                    logger.error(StartSimulationCommand.class
                                            .getSimpleName() + ": "
                                                 + e.getMessage());
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(StartSimulationCommand.class
                                                     .getSimpleName() + ": ", e);
                                    }
                                }
                            }

                            // Set up the simulation.
                            plugin.setupSimulation(null);
                            plugin.insertNets(netSystem);

                            // Create or restore net instances (depending on
                            // database).
                            SimulationState state = plugin
                                            .restoreStateFromDatabase();
                            if (!state.wasSimulationInited()) {
                                NetInstance primaryInstance = plugin
                                            .createNetInstance(primaryNetName);
                                response.println("Simulation set up, created net instance "
                                                 + primaryInstance + ".");
                            } else {
                                response.println("Simulation set up, restored state from database.");
                            }

                            // Run the simulation, if requested.
                            if (!line.hasParameter("-i")) {
                                SimulationEnvironment env = plugin
                                            .getCurrentEnvironment();
                                env.getSimulator().startRun();
                                response.println("Simulation running.");
                            } else {
                                try {
                                    TransactionSource.simulationStateChanged(true,
                                                                             false);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                            return null;
                        }
                    });

                // Retrieve the null result just to check for exceptions.
                object.get();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof SimulationRunningException) {
                    response.println("Simulation already running");
                } else {
                    logger.debug(t.toString(), e);
                    response.println(t.toString());
                    // Clean up simulation might be running
                    response.println("Cleaning up.");
                    plugin.terminateSimulation();
                }
            } catch (SimulationRunningException e) {
                response.println("Simulation already running");
            } catch (Exception e) {
                logger.debug(e.toString(), e);
                response.println(e.toString());
                // Clean up simulation might be running
                response.println("Cleaning up.");
                plugin.terminateSimulation();
            } finally {
                SimulatorPlugin.lock.unlock();
            }
        }
    }

    /* Non-JavaDoc: Specified by the CLCommand interface. */
    public String getDescription() {
        return "set up a simulation with given nets (-h for help).";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "fileNames";
    }

    /**
     * Prints command-line help for this command to <code>System.out</code>.
     *
     * @param response
     *            the <code>PrintStream</code> for user feedback.
     */
    public void showSyntax(PrintStream response) {
        response.println("Parameters: <net system> <primary net> [-i]");
        response.println("  <net system>  : The file name of the exported shadow net system to load.");
        response.println("  <primary net> : The name of the primary net to create an instance of.");
        response.println("  -i            : Initialize the simulation only, don't run it.");
        response.println("To configure the simulation engine and installed extensions, use the");
        response.println("available plugin properties before starting the simulation (e.g. ");
        response.println(SimulatorPlugin.MODE_PROP_NAME + " or "
                         + SimulatorPlugin.EAGER_PROP_NAME + ").");
    }
}