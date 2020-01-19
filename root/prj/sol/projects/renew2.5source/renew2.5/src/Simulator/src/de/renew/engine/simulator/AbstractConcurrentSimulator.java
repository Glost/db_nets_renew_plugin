package de.renew.engine.simulator;

import de.renew.database.TransactionSource;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.Searchable;
import de.renew.engine.searcher.Searcher;
import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.searchqueue.SearchQueueListener;

import de.renew.net.event.ListenerSet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A simulator that is generally capable of handling concurrent
 * simulations and a concurrently updated search queue. A switch
 * decides how to execute each individual firing, so that a sequential
 * simulation is still possible. This simulator cannot and will
 * not detect a deadlock at the end of a simulation.
 * <p>
 * This class observes the following global synchronization order:
 * <ul>
 * <li>0th: locally synchronized;
 * <li>1st: lock SearchQueue;
 * <li>2nd: lock threadLock.
 * </ul>
 */
public class AbstractConcurrentSimulator implements Runnable, Simulator,
                                                    SearchQueueListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AbstractConcurrentSimulator.class);
    private static long runCounter = 0;
    protected long simulationRunId;
    private Searcher searcher = new Searcher();

    /**
     * Used to synchronise access to some method calls that start threads.
     * Dont use synchronized for these methods use lock instead.
     */
    static private final Lock lock = new ReentrantLock();

    /**
     * If true, requests that binding be executed concurrently
     * with the search process.
     */
    private boolean wantConcurrentExecution;

    /**
     * If true, the simulator will wait for the event queue
     * implemented in {@link ListenerSet} before every firing.
     **/
    private boolean wantEventQueueDelay;

    // These field may only be accessed by a thread that
    // has grabbed the thread lock.
    private ExecuteFinder executeFinder;
    private AbortFinder abortFinder;

    // The following variables control the background thread.
    private long cycle = 0L;

    // possible modes:
    private static int SIMULATION_TERMINATE = -1;
    private static int SIMULATION_STOP = 0;
    private static int SIMULATION_STEP = 1;
    private static int SIMULATION_RUN = 2;
    private int desiredMode = SIMULATION_STOP;
    private boolean idle = true;
    private int stepStatusCode = 0;
    private boolean searchQueueIsEmpty = false;
    private boolean alreadyRegistered = false;
    private Object threadLock = new Object();

    public AbstractConcurrentSimulator(boolean wantEventQueueDelay,
                                       boolean wantConcurrentExecution) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.wantEventQueueDelay = wantEventQueueDelay;
        this.wantConcurrentExecution = wantConcurrentExecution;

        // create a new simulationRunId
        simulationRunId = (((long) getClass().getName().hashCode()) << 32)
                          + runCounter++;
        if (logger.isDebugEnabled()) {
            logger.debug(this.getClass().getSimpleName()
                         + ": Starting run with id " + simulationRunId);
        }

        setupFinders();

        SimulationThreadPool.getCurrent().execute(this);
//        Thread thread = new Thread(this);


        // Run the simulation thread in the background.
        //      thread.setPriority(Thread.MIN_PRIORITY);


        // Start the thread.
        //    thread.start();
    }

    private void setupFinders() {
        // Protect the finders against concurrent use.
        synchronized (threadLock) {
            executeFinder = new ExecuteFinder();
            abortFinder = new AbortFinder(executeFinder);
        }
    }

    // In the concurrent simulator, you never know if there might
    // be more enabled transitions. So I am always active.
    public boolean isActive() {
        return true;
    }

    public void searchQueueNonempty() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        synchronized (threadLock) {
            searchQueueIsEmpty = false;
            alreadyRegistered = false;


            // Notify threads that may be waiting for a nonempty search queue.
            threadLock.notifyAll();
        }
    }

    private void registerAtSearchQueue() {
        synchronized (SearchQueue.class) {
            // Now synchronize locally to access fields safely.
            synchronized (threadLock) {
                searchQueueIsEmpty = SearchQueue.isTotallyEmpty();
                if (searchQueueIsEmpty && !alreadyRegistered) {
                    SearchQueue.insertListener(this);
                    alreadyRegistered = true;
                }
            }
        }
    }

    // Terminate the simulation once and for all.
    // Do some final clean-up and exit the thread.
    public void terminateRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    // Notify the simulation thread about the new mode.
                    requestMode(SIMULATION_TERMINATE);

                    try {
                        TransactionSource.simulationStateChanged(false, false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
    }

    // This method is not synchronized, so that it may
    // interrupt a run or step request.
    public void stopRun() {
        SimulationThreadPool.getCurrent().executeAndWait(new Runnable() {
                public void run() {
                    // Notify the simulation thread about the new mode.
                    requestMode(SIMULATION_STOP);

                    try {
                        TransactionSource.simulationStateChanged(true, false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
    }

    // By synchronizing I can guarantee that no other
    // thread spoils the value in stepStatusCode.
    public int step() {
        Future<Integer> future = SimulationThreadPool.getCurrent().submitAndWait(new Callable<Integer>() {
                public Integer call() throws Exception {
                    lock.lock();
                    // Require one more step.
                    requestMode(SIMULATION_STEP);
                    try {
                        TransactionSource.simulationStateChanged(true, false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    lock.unlock();
                    return stepStatusCode;
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

    public void startRun() {
        SimulationThreadPool.getCurrent().execute(new Runnable() {
                public void run() {
                    // Tell the thread to start running.
                    requestMode(SIMULATION_RUN);

                    try {
                        TransactionSource.simulationStateChanged(true, true);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
    }

    // This method used to be synchronized, but there is
    // really no point to it. All accessed fields are
    // controlled by the thread, which will also make
    // sure the this method is not executed concurrently.
    private void requestMode(int mode) {
        synchronized (threadLock) {
            // If another thread has already requested a termination,
            // that request will not be taken back.
            if (desiredMode > SIMULATION_TERMINATE) {
                // Set the mode.
                desiredMode = mode;
                // Set the running state.
                if (mode > SIMULATION_STOP) {
                    idle = false;
                } else {
                    // Just in case a search is currently running, abort it
                    // by notifying the finder.
                    abortFinder.abortSearch();
                }


                // Get the thread out of the sleeping state, if it happens to be there.
                threadLock.notifyAll();
            }

            // Now the calling method must wait for the simulation thread to stop running,
            // if we do not want to let it run indefinitely.
            // Also check desiredMode because it might have changed since we set it.
            // (Note that it is never changed once it was set to SIMULATION_TERMINATE.)
            // TODO In the case of mode == SIMULATION_STEP and desiredMode == SIMULATION_RUN
            //      it will not even wait until a single step is done. Is that OK?
            if (mode < SIMULATION_RUN && desiredMode < SIMULATION_RUN) {
                while (!idle) {
                    try {
                        threadLock.wait();
                    } catch (InterruptedException e) {
                        //TODO this should never happen see http://www-128.ibm.com/developerworks/java/library/j-jtp05236.html
                        // This is expected. <-- yes but its handles wrong
                    }
                }
            }
        }
    }

    public void run() {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        while (desiredMode >= SIMULATION_STOP) {
            // Wait until there is a reasonable chance that
            // a transition instance might be enabled.
            synchronized (threadLock) {
                while (searchQueueIsEmpty && desiredMode == SIMULATION_RUN) {
                    try {
                        threadLock.wait();
                    } catch (InterruptedException e) {
                        //TODO this should never happen see http://www-128.ibm.com/developerworks/java/library/j-jtp05236.html
                        // This is expected. <-- yes but its handles wrong
                    }
                }
            }


            // We note that the search was stopped explicitly.
            // Whenever this is not the case, we will find out
            // later and correct the code accordingly.
            stepStatusCode = statusStopped;


            Searchable searchable = null;
            while (desiredMode > SIMULATION_STOP) {
                // Try to get a transition instance from the search queue.
                // this might fail, because another thread might have emptied the
                // queue concurrently.
                searchable = SearchQueue.extract();
                if (searchable == null) {
                    break;
                }

                // Create a new finder. Earlier during the execution another
                // thread might have placed a stop request that has been
                // processed in the mean time.
                //
                // While we are creating the new finder, another thread
                // might accesse the old finder and place a stop
                // request in it. We solve this problem by rechecking
                // the desired mode afterwards.
                setupFinders();

                // Do we still want to search for a binding?
                if (desiredMode > SIMULATION_STOP) {
                    // If the searchable is not currently enabled, keep
                    // track of the earliest time when it might possibly 
                    // enabled. Use the abort finder, so that
                    // the search may be interrupted.
                    OverallEarliestTimeFinder timeFinder = new OverallEarliestTimeFinder(abortFinder);

                    // Search for a binding.
                    searcher.searchAndRecover(timeFinder, searchable, searchable);

                    // Grant other threads a chance to run.
                    // Only needed on systems without preemptive multitasking.
                    Thread.yield();

                    if (abortFinder.isCompleted()) {
                        // Ok, reinsert this searchable. This must be done if either
                        // the search was successful or if the search was interrupted.
                        SearchQueue.includeNow(searchable);
                    } else {
                        // Reinsert the triggerable at the point of time when it 
                        // might be enabled.
                        timeFinder.insertIntoSearchQueue(searchable);
                    }

                    if (executeFinder.isCompleted()) {
                        // Enforce flow control.
                        if (wantEventQueueDelay) {
                            SimulatorEventQueue.awaitEmptyQueue();
                        }


                        // Execute the binding asynchronously.
                        executeFinder.execute(nextStepIdentifier(),
                                              wantConcurrentExecution);


                        // Provide a new finder so that the old finder does not
                        // forbid garbage collection.
                        setupFinders();


                        // Make a note that we found a binding.
                        stepStatusCode = statusStepComplete;
                        // Stop if in single step mode.
                        synchronized (threadLock) {
                            if (desiredMode == SIMULATION_STEP) {
                                desiredMode = SIMULATION_STOP;
                            }
                        }
                    }
                }
            }

            if (searchable != null) {
                // I have to put back the searchable
                // object that I extracted, because
                // I have not yet found time to analyse it.
                SearchQueue.includeNow(searchable);


                // Forget the searchable. It might have to be garbage collected.
                searchable = null;
            }


            // If the queue is empty, let's register there. It's free.
            //
            // This is done even if desiredMode is SIMULATION_STOP, because I would have to
            // lock threadLock to safely access desiredMode. But after locking
            // threadLock, it is forbidden to access SearchQueue.
            //
            // On the other hand, I must not lock SearchQueue all the time,
            // otherwise I might inhibit notifications from SearchQueue
            // that might keep me running.
            registerAtSearchQueue();


            // I must synchronize on the thread lock, otherwise
            // notifications might get lost.
            synchronized (threadLock) {
                // Did we fail to find any bindings in single step mode?
                if (desiredMode == SIMULATION_STEP) {
                    // Request no further firings.
                    desiredMode = SIMULATION_STOP;


                    // Leave a note that there are currently no enabled transitions.
                    stepStatusCode = statusCurrentlyDisabled;
                }

                // Did the user request a break?
                if (desiredMode <= SIMULATION_STOP) {
                    // I am no longer running.
                    idle = true;


                    // I must send a notification, just in case a stopper waits.
                    // If there are multiple stoppers, I'll notify them all.
                    threadLock.notifyAll();
                }

                // Now we must stick here until someone allows us to proceed.
                while (desiredMode == SIMULATION_STOP) {
                    try {
                        threadLock.wait();
                    } catch (InterruptedException e) {
                        // This is expected.
                    }
                }
            }
        }

        // Ok, exit the simulator because someone requested its termination.
        synchronized (threadLock) {
            // Make sure the simulator is marked as idle, otherwise the
            // requestMode(SIMULATION_TERMINATE) call and thus the
            // terminateRun() call might never return.
            if (!idle) {
                idle = true;
                threadLock.notifyAll();
            }
        }
    }

    // No need to refresh. I do not cache variables.
    public void refresh() {
    }

    /**
     * @return depends on the value of <code>wantConcurrentExecution</code>
     * at creation of this simulator.
     * @see #AbstractConcurrentSimulator(boolean, boolean)
     **/
    public boolean isSequential() {
        return !wantConcurrentExecution;
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