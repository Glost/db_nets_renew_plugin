package de.renew.gui;

import CH.ifa.draw.util.Command;


/**
 * Clears all breakpoints in the running simulation.
 *
 * <p></p>
 * ClearAllBreakpointsCommand.java
 * Created: Mon Feb 26  2001
 * (Code moved from BreakpointManager)
 * @author Michael Duvigneau
 */
public class ClearAllBreakpointsCommand extends Command {
    private BreakpointManager manager;
    private CPNSimulation simulation;

    public ClearAllBreakpointsCommand(String name, BreakpointManager manager,
                                      CPNSimulation simulation) {
        super(name);
        this.manager = manager;
        this.simulation = simulation;
    }

    /**
     * @return <code>true</code>, if a simulation is running.
     **/
    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return simulation.isSimulationActive();
    }

    public void execute() {
        manager.deleteAllBreakpoints();
    }
}