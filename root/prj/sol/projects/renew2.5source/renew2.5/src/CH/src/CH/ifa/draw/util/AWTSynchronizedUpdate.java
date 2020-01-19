package CH.ifa.draw.util;

import de.renew.util.Scheduler;

import java.awt.EventQueue;


/**
 * The class schedules a given <code>updateTask</code> for
 * asynchronous execution in the AWT event queue.  Multiple
 * schedule requests are merged into one as long as the previous
 * requests have not been served.
 * <p>
 * The <code>updateTask</code> (that is executed in the AWT event
 * queue when the scheduled requests are served) is configured in
 * the constructor of this class.  It is a simple {@link Runnable}
 * which does not receive any schedule request details.
 * </p>
 * <p>
 * Executions of <code>updateTask</code> can be requested via
 * {@link #scheduleUpdate}.  When the AWT event queue serves the
 * request, <code>updateTask.run()</code> is executed at least once.
 * It will be executed multiple times in a row if an additional
 * scheduling request arrives before <code>updateTask</code>
 * completes its current execution.
 * </p>
 * @author Michael Duvigneau
 * @version 1.0
 * @since Renew 2.1
 **/
public class AWTSynchronizedUpdate implements Runnable {
    private boolean inUpdate = false;
    private boolean anotherTurn = false;
    private Runnable updateTask;
    private long updateDelay = 0;
    private DelayingTask delayingTask;

    /**
     * Creates a new <code>AWTSynchronizedUpdate</code> instance that
     * schedules the given <code>updateTask</code> immediately when
     * requested.
     *
     * @param updateTask  a <code>Runnable</code> that executes the real
     *                    work in the context of the AWT event queue.
     **/
    public AWTSynchronizedUpdate(Runnable updateTask) {
        this(0, updateTask);
    }

    /**
     * Creates a new <code>AWTSynchronizedUpdate</code> instance that
     * schedules the given <code>updateTask</code> when requested.
     *
     * @param updateDelay  the amount of milliseconds to wait between
     *                     task executions.
     * @param updateTask  a <code>Runnable</code> that executes the real
     *                    work in the context of the AWT event queue.
     **/
    public AWTSynchronizedUpdate(long updateDelay, Runnable updateTask) {
        this.updateDelay = updateDelay;
        this.updateTask = updateTask;
        this.delayingTask = new DelayingTask(this);
    }

    /**
     * Schedules the <code>updateTask</code> with the AWT event queue.
     * If there is already a pending update request for this task, the
     * requests are merged into one.  If the scheduled task is running
     * just now, it will be executed again (either immediately or after
     * the configured <code>updateDelay</code>).
     **/
    public synchronized void scheduleUpdate() {
        anotherTurn = true;
        if (!inUpdate) {
            if (updateDelay == 0) {
                EventQueue.invokeLater(this);
            } else {
                Scheduler.instance().executeIn(delayingTask, updateDelay);
            }
            inUpdate = true;
        }
    }

    /**
     * Determines whether there is a pending update request and updates
     * the flags for pending events according to the result.
     * <p>
     * This is a test-and-set method with multiple side effects:
     * <ul>
     * <li> If there are no pending updates (flag <code>anotherTurn</code>
     *      is <code>false</code>), the flag <code>inUpdate</code> is
     *      set to <code>false</code> to indicate that all pending
     *      updates have been handled.
     * </li>
     * <li> In any case, the flag <code>anotherTurn</code> is reset to
     *      <code>false</code>.
     * </li>
     * <li> If immediate execution is configured, the return value indicates
     *      whether the current task execution should loop another time.
     * </li>
     * <li> If an update delay is configured, the next task execution is
     *      scheduled with the specified delay if <code>anotherTurn</code>
     *      was set before.
     * </ul>
     * </p>
     *
     * @return whether it is expected to immediately re-execute the task
     *       (in the case of no configured delay the return value is the
     *        former value of the flag <code>anotherTurn</code>, in case
     *        of a configured delay the return value is always
     *        <code>false</code>).
     **/
    private synchronized boolean testAndResetUpdateRequest() {
        inUpdate = anotherTurn;
        anotherTurn = false;
        if (updateDelay == 0) {
            // return the former value of anotherTurn to decide
            // on immediate re-execution
            return inUpdate;
        } else if (inUpdate) {
            Scheduler.instance().executeIn(delayingTask, updateDelay);
        }
        return false;
    }

    /**
     * Executes the associated <code>updateTask</code> as long
     * as update notifications arrive concurrently.
     **/
    public void run() {
        // One cycle is mandatory, more only if immediate updates are
        // configured and events arrive during execution.
        testAndResetUpdateRequest();
        do {
            updateTask.run();
        } while (testAndResetUpdateRequest());
    }

    /**
     * This <code>Runnable</code> just schedules the real task with the AWT
     * event queue.
     *
     * @author Michael Duvigneau
     **/
    private static class DelayingTask implements Runnable {
        private final AWTSynchronizedUpdate instance;

        public DelayingTask(AWTSynchronizedUpdate instance) {
            this.instance = instance;
        }

        @Override
        public void run() {
            EventQueue.invokeLater(instance);
        }
    }
}