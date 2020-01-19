package de.renew.util;



/**
 * A concurrency preventer is notified when normally an action shall be run.
 * Instead, the preventer checks that the action is not already running, and
 * if it is running, the action is not run twice, but it is run again after
 * the other one ended. If several notifications arrive during an action
 * execution, the action is rerun only once.
 * This is useful for GUI updating and similar tasks.
 */
public class ConcurrencyPreventer {

    /**
     * The runnable to be run on a notification.
     */
    private Runnable runnable;

    /**
     * Whether the runnable is currently running.
     */
    private boolean running;

    /**
     * Whether the runnable shall be rerun after the current round ended.
     */
    private boolean rerun;

    /**
     * Creates a new concurrency preventer.
     * @param runnable The runnable to be run on a notification.
     */
    public ConcurrencyPreventer(Runnable runnable) {
        this.runnable = runnable;
        running = false;
        rerun = false;
    }

    /**
     * Requests the preventer that the runnable shall be run now,
     * or if it is currently running, it shall be run afterwards.
     */
    public void requestRun() {
        final ConcurrencyPreventer preventer = this;
        synchronized (preventer) {
            if (running) {
                rerun = true;
                return;
            }

            running = true;
        }

        new Thread() {
                public void run() {
                    while (true) {
                        runnable.run();
                        synchronized (preventer) {
                            if (!rerun) {
                                running = false;
                                break;
                            }

                            rerun = false;
                        }
                    }
                }
            }.start();
    }
}