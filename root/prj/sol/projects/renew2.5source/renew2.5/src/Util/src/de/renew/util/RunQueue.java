package de.renew.util;

import java.util.LinkedList;


/**
 * A <code>RunQueue</code> is responsible for executing
 * <code>Runnable</code> objects in a separate thread fifo-style.
 * The thread is never stopped until it is interrupted. When
 * interrupted, all pending events are discarded.
 *
 * @author <a href="mailto:kummer@informatik.uni-hamburg.de">Olaf Kummer</a>
 */
public class RunQueue implements Runnable {
    private LinkedList<Runnable> runnables = null;
    private boolean terminated = false;

    public RunQueue() {
        runnables = new LinkedList<Runnable>();
    }

    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Enqueues the given event.
     *
     * If queue has been terminated, events that arrive are discarded
     * immediately and silently.
     *
     * @param runnable
     */
    public synchronized void add(Runnable runnable) {
        if (!terminated) {
            runnables.addFirst(runnable);
            notify();
        }
    }

    public void run() {
        Runnable runnable;
        while (!terminated) {
            synchronized (this) {
                if (Thread.currentThread().isInterrupted()) {
                    terminated = true;
                } else if (runnables.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        terminated = true;
                    }
                } else {
                    runnable = runnables.removeLast();
                    runnable.run();
                }
            }
        }
    }
}