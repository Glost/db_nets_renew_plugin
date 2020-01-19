package de.renew.util;



/**
 * A synchronizer is an object that is notified by a set of concurrent threads
 * that they have started and ended, and that helps another thread to wait for
 * all threads to end.
 */
public class Synchronizer {

    /**
     * The number of currently running threads of the observed set.
     */
    private int running;

    /**
     * Creates a new synchronizer.
     */
    public Synchronizer() {
        running = 0;
    }

    /**
     * Notifies the synchronizer that a thread has ended.
     */
    public synchronized void ended() {
        running--;
        if (running == 0) {
            notifyAll();
        }
    }

    /**
     * Notifies the synchronizer that another thread has started.
     */
    public synchronized void started() {
        running++;
    }

    /**
     * This method returns as soon as all observed threads have ended.
     */
    public synchronized void sync() {
        while (running > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Why ever we were interrupted, but we can safely ignore it.
            }
        }
    }
}