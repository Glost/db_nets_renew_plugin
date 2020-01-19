package de.renew.engine.simulator;

import de.renew.util.Semaphor;

import java.util.concurrent.Callable;


/**
 * This class serves as a way to run a {@link Callable} in a simulation thread
 * and to wait for its {@link Callable#call()} method to finish. Furthermore,
 * this class enables the advanced locking scheme of
 * {@link InheritableSimulationThreadLock} by storing a reference to the calling
 * thread that can be queried via {@link #getAncestor()}.
 * <p>
 * The <code>BlockingSimulationCallable</code> stores a reference to its
 * creating ancestor thread for later use in the executing
 * {@link SimulationThread} and provides a {@link Semaphor} that allows the
 * {@link SimulationThreadPool#submitAndWait(java.util.concurrent.Callable)}
 * implementation to block the calling thread.
 * </p>
 * <p>
 * Upon task completion, <code>BlockingSimulationCallable</code> frees the the
 * {@link Semaphor} and cleans the ancestor reference from the executing
 * {@link SimulationThread}. In presence of exceptions, the cleanup can be
 * requested externally through {@link #abort(Thread)}.
 * </p>
 *
 * @author Benjamin Schleinzer
 * @author Michael Duvigneau
 *
 * @param <T> the return type of the passed Callable
 */
public class BlockingSimulationCallable<T> implements Callable<T> {

    /**
     * The Callable that should be run
     */
    private Callable<T> task;

    /**
     * The semaphor that should be notified if the thread has run successfully
     */
    private Semaphor notifier;

    /**
     * The thread that placed the order for this Callable
     */
    private Thread ancestor;

    /**
     * Create an instance of a
     * <code>BlockingSimulationCallable<code> with a {@link Callable}
     * to run and a {@link Semaphor} that blocks the calling thread.
     *
     * @param taskToRun the task to run
     * @param lock the semaphor that gets notified after execution finished
     * @param ancestor the calling thread (the ancestor)
     */
    public BlockingSimulationCallable(Callable<T> taskToRun, Semaphor lock,
                                      Thread ancestor) {
        this.task = taskToRun;
        this.notifier = lock;
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
     * <code>BlockingSimulationCallable</code> just delegates execution to the
     * nested {@link Callable}.  After execution, it additionally frees the
     * calling thread (ancestor) and cleans the ancestor reference in the
     * executing {@link SimulationThread}.
     * </p>
     * <p>
     * This method should be called by the Executor only since the executor
     * locks the semaphor.
     * </p>
     **/
    @Override
    public T call() throws Exception {
        //Former code returned FutureTask<T> which seemed unnecessary
        T returnValue;
        try {
            if (SimulationThreadPool.logger.isTraceEnabled()) {
                SimulationThreadPool.logger.trace("Executing simulation callable now:          "
                                                  + task + " in "
                                                  + Thread.currentThread());
            }
            returnValue = task.call();
        } finally {
            abort((SimulationThread) Thread.currentThread());
        }
        return returnValue;
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
        notifier.V();
        ancestor = null;
        if (t != null) {
            t.setAncestor(null);
        }
    }
}