package de.renew.engine.simulator;

import de.renew.engine.common.StepIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ParallelSimulator implements Simulator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ParallelSimulator.class);
    private static long runCounter = 0;
    private long cycle = 0L;
    public final int multiplicity;
    protected long simulationRunId;
    protected long[] collectedSimulationRunIds;
    private final Simulator[] simulators;

    public ParallelSimulator(int multiplicity, boolean wantEventQueueDelay) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (multiplicity == 0) {
            multiplicity = 1;
        }
        boolean wantConcurrent = multiplicity > 0;
        if (!wantConcurrent) {
            multiplicity = -multiplicity;
        }
        this.multiplicity = multiplicity;

        // create a new simulationRunId
        simulationRunId = (((long) getClass().getName().hashCode()) << 32)
                          + runCounter++;

        if (logger.isDebugEnabled()) {
            logger.debug(this.getClass().getSimpleName()
                         + ": Starting run with id " + simulationRunId);
        }

        simulators = new Simulator[multiplicity];
        for (int i = 0; i < multiplicity; i++) {
            if (wantConcurrent) {
                simulators[i] = new ConcurrentChildSimulator(wantEventQueueDelay,
                                                             this);
            } else {
                simulators[i] = new NonConcurrentChildSimulator(wantEventQueueDelay,
                                                                this);
            }
        }
        collectAllRunIds();
    }

    /**
     * Update {@link #collectedSimulationRunIds} based on the nested simulators.
     * Also include the own {@link #simulationRunId} of this instance.
     */
    private void collectAllRunIds() {
        List<Long> allRunIds = new ArrayList<Long>(multiplicity + 1);
        allRunIds.add(simulationRunId);
        for (int i = 0; i < multiplicity; i++) {
            long[] runIds = simulators[i].collectSimulationRunIds();
            for (long runId : runIds) {
                allRunIds.add(runId);
            }
        }
        collectedSimulationRunIds = new long[allRunIds.size()];
        for (int i = 0; i < allRunIds.size(); i++) {
            collectedSimulationRunIds[i] = allRunIds.get(i);
        }
    }

    public boolean isActive() {
        for (int i = 0; i < multiplicity; i++) {
            if (simulators[i].isActive()) {
                return true;
            }
        }
        return false;
    }

    // Start the simulation in the background.
    public synchronized void startRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < multiplicity; i++) {
                        simulators[i].startRun();
                    }
                }
            });
    }

    // Gently stop the simulation.
    public void stopRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < multiplicity; i++) {
                        simulators[i].stopRun();
                    }
                }
            });
    }

    // Terminate the simulation once and for all.
    // Do some final clean-up and exit all threads.
    public synchronized void terminateRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < multiplicity; i++) {
                        simulators[i].terminateRun();
                    }
                }
            });
    }

    // Perform just one step or terminate the simulation, if
    // it is running in the background. Return true, if
    // another binding could be found.
    public synchronized int step() {
        Future<Integer> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Integer>() {
                public Integer call() throws Exception {
                    for (int i = 0; i < multiplicity; i++) {
                        simulators[i].stopRun();
                    }


                    // Try to fire any simulator and stop on the first success.
                    // This is required when aggregating sequential simulators,
                    // because their enabledness status might be different.
                    // (Sequential simulators hold the enabled transition instance
                    // hostage until the firing is over.)
                    int status = 0; // will be overwritten
                    for (int i = 0; i < multiplicity; i++) {
                        status = simulators[i].step();
                        if (status == statusStepComplete
                                    || status == statusLastComplete) {
                            // One simulator performed a step and all simulators
                            // are in stopped state.
                            return status;
                        }
                    }

                    // No simulator was able to perform a step.
                    return status;
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured before.
        return -1;
        // Stop all simulators except the first.
    }

    public void refresh() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < multiplicity; i++) {
                        simulators[i].refresh();
                    }
                }
            });
    }

    /**
     * @return <code>false</code>
     **/
    public boolean isSequential() {
        return false;
    }

    public StepIdentifier nextStepIdentifier() {
        Future<StepIdentifier> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<StepIdentifier>() {
                public StepIdentifier call() throws Exception {
                    return new StepIdentifier(simulationRunId,
                                              new long[] { ++cycle });
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured before.
        return null;

    }

    public StepIdentifier currentStepIdentifier() {
        Future<StepIdentifier> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<StepIdentifier>() {
                public StepIdentifier call() throws Exception {
                    return new StepIdentifier(simulationRunId,
                                              new long[] { cycle });
                }
            });
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Timeout while waiting for simulation thread to finish",
                         e);
        } catch (ExecutionException e) {
            logger.error("Simulation thread threw an exception", e);
        }

        // We should never return nothing but some error occured befor.
        return null;

    }

    /* (non-Javadoc)
     * @see de.renew.engine.simulator.Simulator#collectSimulationRunIds()
     */
    @Override
    public long[] collectSimulationRunIds() {
        return Arrays.copyOf(collectedSimulationRunIds,
                             collectedSimulationRunIds.length);
    }
}