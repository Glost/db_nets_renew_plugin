package de.renew.engine.simulator;

import de.renew.database.TransactionSource;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searchqueue.SearchQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class SequentialSimulator implements Simulator, Runnable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SequentialSimulator.class);
    private long simulationRunId;
    private long cycle = 0L;
    private ExecuteFinder finder;
    private Searcher searcher = new Searcher();
    private boolean stepFired = false;
    private boolean isAlive = false;
    private boolean wantBreak = false;
    private SequentialSimulator runThread = null;

    /**
     * If true, the simulator will wait for the event queue
     * before every firing.
     **/
    private boolean wantEventQueueDelay;

    public SequentialSimulator() {
        this(true);
    }

    public SequentialSimulator(boolean wantEventQueueDelay) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.wantEventQueueDelay = wantEventQueueDelay;

        if (logger.isDebugEnabled()) {
            logger.debug(this.getClass().getSimpleName()
                         + ": Starting run with id " + simulationRunId);
        }

        // If possible, prepare a new binding.
        findNewBinding();
    }

    public boolean isActive() {
        return isAlive;
    }

    public synchronized void startRun() {
        // Only create a new thread if none is currently running.
        if (runThread == null) {
            // Create a new thread that will run the simulation.
            runThread = this;

            SimulationThreadPool.getCurrent().execute(this);

            try {
                TransactionSource.simulationStateChanged(true, true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // Gently stop the simulation.
    // This may take a while, because the current search for
    // a activated binding must be completed first.
    // After the completion of this method, no thread is running
    // and runThread==null.
    public synchronized void stopRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    while (runThread != null) {
                        wantBreak = true;
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            // This is expected. But I cannot be sure that
                            // I woke up from a notification.
                        }
                    }

                    try {
                        TransactionSource.simulationStateChanged(true, false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
    }

    // Terminate the simulation once and for all.
    // Do some final clean-up and exit the thread.
    public synchronized void terminateRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    // In this case we can simply call stop run, because that
                    // is sufficient to terminate all running threads.
                    stopRun();

                    try {
                        TransactionSource.simulationStateChanged(false, false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
    }

    private void fire() {
        stepFired = false;
        while (isAlive && !stepFired) {
            // Enforce flow control.
            if (wantEventQueueDelay) {
                SimulatorEventQueue.awaitEmptyQueue();
            }


            // Fire the binding.
            stepFired = finder.execute(nextStepIdentifier(), false);
            findNewBinding();
        }
    }

    // To exclude concurrent access to the searcher, this method may only
    // be called from the running thread or from a
    // synchronized method that has ensured that no thread is running.
    // This method is not synchronized, because it could
    // lock up termination requests.
    // If there are no more enabled transitions, this method simply
    // returns without effect.
    private void findNewBinding() {
        // An ExecuteFinder should not be recycled.
        finder = new ExecuteFinder();


        // Try all transitions that might be enabled.
        // If a successful binding was found (though not
        // neccessarily accepted), requeue this transition
        // at the end of the search.
        // If no bindings are possible, forget about this 
        // transition. A change to a relevant place will
        // trigger its reinsertion.
        while (!finder.isCompleted() && !SearchQueue.isTotallyEmpty()) {
            // Get a searchable.
            Searchable searchable = SearchQueue.extract();


            // If the searchable is not currently enabled, keep
            // track of the earliest time when it might possibly 
            // enabled.
            OverallEarliestTimeFinder timeFinder = new OverallEarliestTimeFinder(finder);

            // Perform the search. The searchable itself is used
            // as the triggerable, because it was taken from the search queue.
            searcher.searchAndRecover(timeFinder, searchable, searchable);

            // Reinsert the searchable back into the search queue,
            // if necessary.
            timeFinder.insertIntoSearchQueue(searchable);
        }


        // Did we find a binding?
        isAlive = finder.isCompleted();
    }

    // This method must be synchronized to avoid concurrent
    // accesses to restart another thread after the stop.
    public synchronized int step() {
        Future<Integer> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Integer>() {
                public Integer call() throws Exception {
                    if (runThread != null) {
                        // Just stop the current thread.
                        stopRun();
                        if (isAlive && !stepFired) {
                            // The current thread did not make a step.
                            fire();
                        }
                    } else {
                        // Try to make another step.
                        fire();
                    }
                    if (stepFired) {
                        if (isAlive) {
                            return statusStepComplete;
                        } else {
                            return statusLastComplete;
                        }
                    } else {
                        return statusDisabled;
                    }
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
        return -1;

    }

    // This method is called in threads that are generated on each
    // run request. The simulator class cannot inherit from thread,
    // because this method might be run multiple times.
    public void run() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        // Do have a prepared binding?
        if (!isAlive) {
            // No, let's try to find one, although there is
            // little hope that we can find it.
            findNewBinding();
        }

        // Make steps until deadlock or until a break is requested.
        while (isAlive && !wantBreak) {
            // Grant other threads a chance to run.
            // Only needed on systems without preemptive multitasking.
            Thread.yield();


            // Fire and search for a new binding.
            fire();
        }


        // Make sure that runThread and wantBreak are updated
        // atomically.
        synchronized (this) {
            // Signal the end of the run.
            runThread = null;


            // Clear the break request.
            wantBreak = false;


            // Notify waiting threads of break.
            notifyAll();
        }
    }

    public synchronized void refresh() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    if (runThread == null) {
                        // No thread is currently running. Make sure to update
                        // the enabled bindings and the time manually.
                        findNewBinding();
                    }
                }
            });
    }

    /**
     * @return <code>true</code>
     **/
    public boolean isSequential() {
        return true;
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

        // We should never return nothing but some error occured befor.
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
        return new long[] { simulationRunId };
    }
}