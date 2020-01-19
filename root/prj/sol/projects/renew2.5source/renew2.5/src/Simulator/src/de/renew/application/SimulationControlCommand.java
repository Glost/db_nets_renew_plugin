package de.renew.application;

import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.Simulator;

import de.renew.plugin.command.CLCommand;

import java.io.PrintStream;


/**
 * This command line command provides several subcommand to control the
 * simulation engine. This includes single-step mode and consecutive runs as
 * well as simulation termination.
 * <p>
 * </p>
 * SimulationControlCommand.java Created: Wed Jul 16 2003
 *
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class SimulationControlCommand implements CLCommand {
    private SimulatorPlugin plugin;

    /**
         * Creates a new <code>SimulationControlCommand</code> instance.
         *
         * @param plugin
         *                neccessary reference to the simulator plugin instance
         *                where this command belongs to.
         *
         * @throws NullPointerException
         *                 if <code>plugin</code> is <code>null</code>.
         */
    public SimulationControlCommand(SimulatorPlugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("Need SimulatorPlugin reference.");
        }
        this.plugin = plugin;
    }

    /**
         * Controls a simulation or prints a help text. See this class
         * documentation for further details.
         *
         * @param args
         *                one-element-array containing the simulation control
         *                subcommand (see output of the
         *                <code>showSyntax()</code> method).
         * @param response
         *                the <code>PrintStream</code> for user feedback.
         */
    public void execute(String[] args, PrintStream response) {
        if ((args == null) || (args.length != 1) || (args[0] == null)) {
            response.println("Error: Please give exactly one subcommand. Enter 'help' for a command help.");
        } else {
            handleCommand(args[0], response);
        }
    }

    /**
         * Actually interprets the command given to execute.
         *
         * @param command
         *                the subcommand to interpret.
         * @param response
         *                the <code>PrintStream</code> for user feedback.
         */
    private void handleCommand(final String command, final PrintStream response) {
        if (command.startsWith("help")) {
            showSyntax(response);
        } else if (command.startsWith("term")) {
            plugin.terminateSimulation();
        } else {
            SimulationEnvironment env = plugin.getCurrentEnvironment();
            if (env == null) {
                response.println("Error: There is no current simulation environment.");
            } else {
                final Simulator simulator = env.getSimulator();
                if (command.startsWith("run")) {
                    SimulationThreadPool.getCurrent().execute(new Runnable() {
                            public void run() {
                                simulator.startRun();
                            }
                        });
                } else if (command.startsWith("step") || command.equals("")) {
                    SimulationThreadPool.getCurrent().execute(new Runnable() {
                            public void run() {
                                simulator.step();
                            }
                        });
                } else if (command.startsWith("stop")
                                   || command.startsWith("halt")) {
                    SimulationThreadPool.getCurrent().execute(new Runnable() {
                            public void run() {
                                simulator.stopRun();
                            }
                        });
                } else {
                    response.println("Error: Unknown command. Enter 'help' for a command help.");
                }
            }
        }
    }

    /* Non-JavaDoc: Specified by the CLCommand interface. */
    public String getDescription() {
        return "control the simulation by subcommands (e.g. step, run, stop)";
    }

    /**
         * Prints command-line help for this command to <code>response</code>.
         *
         * @param response
         *                the <code>PrintStream</code> to write the help to.
         */
    public void showSyntax(PrintStream response) {
        response.println("Simulation control commands:");
        response.println("help   Displays this help.");
        response.println("run    Runs the simulation continuously.");
        response.println("step   Executes only one simulation step.");
        response.println("stop   Stops the simulation, but it remains initialized.");
        response.println("halt   Same as stop.");
        response.println("term   Terminates the simulation.");
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "(run|step|halt|term|help)";
    }
}