package de.renew.engine.simulator;

import java.util.concurrent.Callable;


/**
 * A simulation thread is able to store a reference to its ancestor thread.
 * An ancestor thread is
 * defined as the thread that called
 * {@link SimulationThreadPool#executeAndWait(Runnable)} or
 * {@link SimulationThreadPool#submitAndWait(Callable)} and is waiting for the
 * completion of the simulation thread that executes the respective Runnable or
 * Callable.
 * <p>
 * Since simulation threads are re-used to carry out independent tasks, the
 * ancestor reference may change (or disappear) multiple times during the thread's
 * life-time.  However, the SimulationThreadPool guarantees that the ancestor
 * reference is immutable during the execution of a single task.
 * </p>
 *
 * @author Matthias Wester-Ebbinghaus
 * @author Michael Duvigneau
 *
 */
class SimulationThread extends Thread {

    /**
     * The reference to the ancestor thread, if there is one.
     **/
    private Thread ancestor;

    public SimulationThread(ThreadGroup group, Runnable r, String name,
                            int priority) {
        super(group, r, name, priority);
    }

    /**
     * Set the current ancestor thread for this thread.
     * @param anc  the thread to reference as ancestor.
     *      if <code>null</code>, the reference is cleared.
     */
    void setAncestor(Thread anc) {
        ancestor = anc;
    }

    /**
     * Retrieve the current ancestor thread of this thread.
     *
     * @return  the ancestor thread.  May be <code>null</code>.
     */
    public Thread getAncestor() {
        return ancestor;
    }
}