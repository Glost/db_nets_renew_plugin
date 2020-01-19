package de.renew.engine.simulator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The <code>WrappedFutureTask</code> is the counterpart of a
 * {@link BlockingSimulationCallable} that wraps the result.
 * This class is used internally within
 * {@link SimulationThreadPool#submitAndWait(java.util.concurrent.Callable)}
 * to ensure that the blocking behaviour of the {@link BlockingSimulationCallable}
 * is cancelled even in the presence of exceptions.  Otherwise, this class
 * adds nothing to the behaviour of a standard {@link FutureTask}.
 *
 * @author Benjamin Schleinzer
 * @author Michael Duvigneau
 * @author Felix Ortmann
 * @author Fabian Sobanski
 *
 * @param <V> type of the computation result of the callable.
 **/
class WrappedFutureTask<V> extends FutureTask<V> {
    BlockingSimulationCallable<V> callable;

    /**
     * Create a future task with the ability to cancel the given
     * {@link BlockingSimulationCallable} in case of exceptions.
     *
     * @param callable
     *          the callable to cancel in the case of exceptions
     **/
    public WrappedFutureTask(BlockingSimulationCallable<V> callable) {
        super(callable);
        this.callable = callable;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally cancel the blocking behaviour of our callable.
     * </p>
     **/
    public V get() throws InterruptedException, ExecutionException {
        V returnValue = null;
        boolean success = false;
        try {
            returnValue = super.get();
            success = true;
        } finally {
            if (!success) {
                // Something went wrong, we need to ensure that the
                // semaphore blocking the calling thread does not
                // stay blocked forever.
                callable.abort(null);
            }
        }
        return returnValue;

    }

    /**
     * {@inheritDoc}
     * <p>
     * Additionally cancel the blocking behaviour of our callable.
     * </p>
     **/
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        V returnValue = null;
        boolean success = false;
        try {
            returnValue = super.get(timeout, unit);
            success = true;
        } finally {
            if (!success) {
                // Something went wrong, we need to ensure that the
                // semaphore blocking the calling thread does not
                // stay blocked forever.
                callable.abort(null);
            }
        }
        return returnValue;

    }
}