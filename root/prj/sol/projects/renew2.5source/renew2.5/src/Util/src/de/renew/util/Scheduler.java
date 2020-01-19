package de.renew.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;


/**
 * The Scheduler class allows to delay the execution of runnables.
 * Every runnable is executed in the scheduler thread.
 * <p>
 * The scheduler thread is a non-daemon thread and will not
 * terminate as long as any runnable is scheduled. If you want
 * the Java VM to shut down after all application threads have
 * finished their execution, you have to take care that all
 * scheduled runnables get {@link #cancel cancelled}. Otherwise
 * the Scheduler thread will prevent the Java VM from termination
 * until all scheduled runnables have been executed.
 * </p>
 **/
public class Scheduler implements Runnable {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(Scheduler.class);

    /**
     * The singleton {@link Scheduler} instance.
     */
    private static Scheduler instance;

    /**
     * The thread used for execution of the scheduler and all
     * scheduled runnables. The thread is created on demand
     * (i.e. when the first runnable gets registered) and stopped
     * when no runnables are left. When stopped, this variable is
     * set back to <code>null</code>.
     **/
    private Thread schedulerThread = null;

    /**
     * A map from time stamps represented as longs
     * to pairs of time stamp and queued elements. This map is always
     * accessed by take and insert methods.
     */
    private TreeMap<Long, SchedulerPair> queues = new TreeMap<Long, SchedulerPair>();

    private Scheduler() {
        // nothing to do.
        // The scheduler thread gets started whenever the first
        // runnable is enqueued.
        if (logger.isDebugEnabled()) {
            logger.debug(this + ": created instance.");
        }
    }

    public static synchronized Scheduler instance() {
        if (instance == null) {
            instance = new Scheduler();
        }
        return instance;
    }

    private void ensureRunningThread() {
        if (schedulerThread == null) {
            schedulerThread = new Thread(this, "Renew Scheduler");
            schedulerThread.start();
            if (logger.isDebugEnabled()) {
                logger.debug(this + ": created thread.");
            }
        }
    }

    private SchedulerPair takeEarliest() {
        if (queues.isEmpty()) {
            return null;
        } else {
            Long key = queues.firstKey();
            return queues.remove(key);
        }
    }

    private SchedulerPair takePairAt(long timeMillis) {
        Long key = new Long(timeMillis);
        SchedulerPair result = queues.remove(key);
        return result == null ? new SchedulerPair(key) : result;
    }

    /**
     * Reinsert the given scheduler pair if it contains
     * at least one runnable.
     *
     * @param pair the <code>SchedulerPair</code> to be inserted
     */
    private void putPair(SchedulerPair pair) {
        if (!pair.list.isEmpty()) {
            queues.put(pair.key, pair);
        }
    }

    /**
     * Delays the execution of a runnable for a given period of time
     * A runnable that is executed may call this method to reinsert itself
     * or to schedule other runnables.
     *
     * @param runnable the <code>Runnable</code> to be executed
     * @param deltaMillis the number of milliseconds to delay the execution.
     */
    public synchronized void executeIn(Runnable runnable, long deltaMillis) {
        executeAt(runnable, System.currentTimeMillis() + deltaMillis);
    }

    /**
     * Delays the execution of a runnable until a given point of time
     * A runnable that is executed may call this method to reinsert itself
     * or to schedule other runnables.
     *
     * @param runnable the <code>Runnable</code> to be executed
     * @param timeMillis the time in milliseconds of desired the execution.
     */
    public synchronized void executeAt(Runnable runnable, long timeMillis) {
        SchedulerPair pair = takePairAt(timeMillis);
        pair.list.addFirst(runnable);
        putPair(pair);
        if (logger.isTraceEnabled()) {
            logger.trace(this + ": scheduled " + runnable + " for "
                         + timeMillis);
        }


        // Make sure that the scheduler thread wakes up to process
        // the new runnnable, if it happens to be the earliest runnable.
        ensureRunningThread();
        notifyAll();
    }

    /**
     * Cancels any scheduled excecution for the given
     * Runnable. The cancellation will not affect a current
     * execution of the Runnable. This method has no effect if
     * the Runnable was not scheduled.
     *
     * @param runnable the <code>Runnable</code> to be removed
     *                 from the schedule
     */
    public synchronized void cancel(Runnable runnable) {
        if (logger.isTraceEnabled()) {
            logger.trace(this + ": request to cancel " + runnable);
        }
        Iterator<SchedulerPair> allElements = queues.values().iterator();
        Vector<Long> toRemove = new Vector<Long>(queues.size());
        while (allElements.hasNext()) {
            SchedulerPair pair = allElements.next();
            pair.list.remove(runnable);
            if (pair.list.isEmpty()) {
                toRemove.add(pair.key);
            }
        }
        Enumeration<Long> removeElements = toRemove.elements();
        while (removeElements.hasMoreElements()) {
            queues.remove(removeElements.nextElement());
        }


        // Make sure that the scheduler thread wakes up to check
        // whether any runnables are left in the queue.
        notifyAll();
    }

    public synchronized void run() {
        SchedulerPair pair;
        do {
            pair = takeEarliest();
            if (pair != null) {
                long timeMillis = pair.key.longValue();
                long now = System.currentTimeMillis();
                if (timeMillis > now) {
                    // No runnables are ready yet.
                    putPair(pair);
                    try {
                        wait(timeMillis - now);
                    } catch (InterruptedException e) {
                        // This is expected.
                    }
                } else {
                    // Some runnables have become executable.
                    // After this point of time, we must not assume
                    // that the map is unchanged, because the
                    // invoked runnables might have inserted further
                    // items into the queue.
                    while (!pair.list.isEmpty()) {
                        Runnable runnable = pair.list.removeLast();
                        if (logger.isTraceEnabled()) {
                            logger.trace(this + ": executing at " + now + ": "
                                         + runnable);
                        }
                        runnable.run();
                    }

                    // No wait required. Check next time stamp immediately.
                }
            }
        } while (pair != null);

        if (logger.isDebugEnabled()) {
            logger.debug(this + ": quitting thread.");
        }

        // No runnables in the queue
        // Instead of waiting, stop the thread.
        schedulerThread = null;
    }
}