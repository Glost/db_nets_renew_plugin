package de.renew.engine.simulator;

import de.renew.util.Semaphor;


/**
 * This class serves as a way to run a {@link Runnable} in a simulation thread
 * and to wait for its {@link Runnable#run()} method to finish. Furthermore,
 * this class enables the advanced locking scheme of
 * {@link InheritableSimulationThreadLock} by storing a reference to the calling
 * thread that can be queried via {@link #getAncestor()}.
 * <p>
 * The <code>BlockingSimulationRunnable</code> stores a reference to its
 * creating ancestor thread for later use in the executing
 * {@link SimulationThread} and provides a {@link Semaphor} that allows the
 * {@link SimulationThreadPool#executeAndWait(Runnable)}
 * implementation to block the calling thread.
 * </p>
 * <p>
 * Upon task completion, <code>BlockingSimulationRunnable</code> frees the the
 * {@link Semaphor} and cleans the ancestor reference from the executing
 * {@link SimulationThread}. In presence of exceptions, the cleanup can be
 * requested externally through {@link #abort(Thread)}.
 * </p>
 *
 * @author Benjamin Schleinzer
 * @author Michael Duvigneau
 */
class BlockingSimulationRunnable implements Runnable {

    /**
     * The Runable that should be run
     */
    private Runnable task;

    /**
     * The semaphor that should be notified if the thread has run successfully
     */
    private Semaphor lock;

    /**
     * The thread that placed the order for this Runnable
     */
    private Thread ancestor;

    /**
     * Create an instance of a
     * <code>BlockingSimulationRunnable<code> with a {@link Runnable}
     * to run and a {@link Semaphor} that blocks the calling thread.
     *
     * @param taskToRun the task to run
     * @param lock the semaphor that gets notified after execution finished
     * @param ancestor the calling thread (the ancestor)
     */
    public BlockingSimulationRunnable(Runnable task, Semaphor lock,
                                      Thread ancestor) {
        this.task = task;
        this.lock = lock;
        this.ancestor = ancestor;
    }

    /**
     * Retrieve the reference to the calling thread.
     * @return  the calling thread
     */
    public Thread getAncestor() {
        return ancestor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <code>BlockingSimulationRunnable</code> just delegates execution to the
     * nested {@link Runnable}.  After execution, it additionally frees the
     * calling thread (ancestor) and cleans the ancestor reference in the
     * executing {@link SimulationThread}.
     * </p>
     * <p>
     * This method should be called by the Executor only since the executor
     * locks the semaphor.
     * </p>
     **/
    @Override
    public void run() {
        try {
            task.run();
        } finally {
            abort((SimulationThread) Thread.currentThread());
        }
    }

    /**
     * Frees the calling thread (ancestor) and cleans the respective reference
     * in the executing {@link SimulationThread}.  This method should be called
     * if something goes wrong during execution to ensure continuation of the
     * calling thread.
     *
     * @param t the executing thread whose ancestor information should be
     *            cleaned.  If <code>null</code>, cleaning is skipped.
     */
    protected void abort(SimulationThread t) {
        lock.V();
        ancestor = null;
        if (t != null) {
            t.setAncestor(null);
        }
    }
}