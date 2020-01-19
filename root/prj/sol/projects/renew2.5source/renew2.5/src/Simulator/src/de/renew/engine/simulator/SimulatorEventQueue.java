package de.renew.engine.simulator;

import de.renew.util.RunQueue;
import de.renew.util.Semaphor;


/**
 * The event queue is a static service for running jobs
 * asynchronously in a dedicated thread. Formalisms are encouraged
 * to use this queue for delivering events. If they do, simulators
 * may use the method {@link #awaitEmptyQueue()} in this class to
 * avoid a listener overrun in the case of overly fast simulations.
 */
public class SimulatorEventQueue {

    /**
     * This class is completely static. No instances.
     */
    private SimulatorEventQueue() {
    }

    /**
     * The <code>queue</code> is a global object that dispatches
     * events to those listener that do not want to be notified
     * synchronously.
     **/
    private static RunQueue queue = new RunQueue();

    public static void initialize() {
        queue = new RunQueue();
        SimulationThreadPool.getCurrent().execute(queue);
    }

    /**
     * Delay the execution of the calling thread until
     * all currently pending messages are delivered.
     * New messages may be concurrently inserted into the
     * queue, though. They will not affect the time of
     * return.
     **/
    public static void awaitEmptyQueue() {
        final Semaphor s = new Semaphor();
        try {
            queue.add(new Runnable() {
                    public void run() {
                        s.V();
                    }
                });
        } finally {
            s.P();
        }
    }

    public static void enqueue(Runnable runnable) {
        queue.add(runnable);
    }
}