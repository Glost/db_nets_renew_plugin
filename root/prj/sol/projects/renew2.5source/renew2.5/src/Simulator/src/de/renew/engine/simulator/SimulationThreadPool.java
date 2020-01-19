package de.renew.engine.simulator;

import de.renew.util.ClassSource;
import de.renew.util.Semaphor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The <code>SimulationThreadPool</code> uses the factory pattern to provide
 * a way to execute simulation threads.
 * <p>
 * <strong>Thread properties:</strong>
 * All threads created and managed within this pool will have the same context
 * classloader and the same priority.  The <code>classloader</code>
 * is obtained from the {@link ClassSource} whenever the thread pool singleton
 * instance is created.  The priority must be configured after pool creation via
 * {@link #setMaxPriority(int)}.  Simulation threads carry a reference
 * to their current ancestor thread, if the ancestor thread is waiting for the
 * completion of the current task that the simulation thread executes.
 * <p>
 * <strong>Convenience wrapping:</strong>
 * The <code>SimulationThreadPool</code> provides four methods that
 * automatically execute a given piece of code within a simulation thread.
 * Which method to use depends on the amount of information a caller needs
 * about successful execution:
 * a) Is there need for a return value or an exception?
 * b) Is there need to wait until execution completes?
 * The methods are organized as follows (follow the links for more details):
 * </p>
 * <table>
 * <tr><th>                   </th><th>asynchronous    </th><th>wait for completion    </th></tr>
 * <tr><th><code>void</code>  </th><td>{@link #execute}</td><td>{@link #executeAndWait}</td></tr>
 * <tr><th>result or exception</th><td>{@link #submit} </td><td>{@link #submitAndWait} </td></tr>
 * </table>
 * <p>
 * <strong>Softened singleton property:</strong>
 * The <code>SimulationThreadPool</code> follows the Singleton pattern.
 * There may be at most one instance which can be obtained (and created,
 * if neccessary) via the static method {@link #getCurrent}.  The instance
 * can be discarded by calling {@link #cleanup} (with the side effect of
 * terminating all threads belonging to the discarded pool).
 * </p>
 * <p>
 * When a new simulation is set up, a preliminary thread pool may be
 * obtained (and created) via {@link #getNew}.  A subsequent call to
 * {@link #cleanup} makes this preliminary instance the current thread
 * pool instance, so that it replaces the previous one.  If the set
 * up needs to be rolled back, a call to {@link #discardNew()} disposes
 * of the preliminary instance.
 * </p>
 * <p>
 * The static method {@link #isSimulationThread()} may be used to check
 * whether the calling thread belongs to the current <code>SimulationThreadPool</code>
 * singleton.
 * </p>
 *
 * @author Benjamin Schleinzer
 * @author Matthias Wester-Ebbinghaus
 * @author Michael Duvigneau
 */
public class SimulationThreadPool extends ThreadPoolExecutor {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimulationThreadPool.class);

    /**
     * Used to synchronise access to the static <code>singleton</code>
     * variable.
     */
    static private final Object lock = new Object();

    /**
     * Holds a reference to the one and only SimulationThreadPool instance. Set
     * by the method {@link #getCurrent} and reset by the method
     * {@link #cleanup}.
     */
    static private SimulationThreadPool singleton = null;

    /**
     * Holds a temporary reference to the SimulationThreadPool instance that becomes
     * the next one and only singleton. Set
     * by the method {@link #getNew} and reset by the method
     * {@link #cleanup} or {@link #discardNew}.
     */
    static private SimulationThreadPool newSingleton = null;

    /**
     * Holds a reference to the created SimulatorThreadFactory
     */
    private final SimulatorThreadFactory factory;

    /**
     * Create a SimulationThreadPool with given priority
     *
     * @param priority
     *            the maximum allowed priority
     */


    //NOTICEsignature
    private SimulationThreadPool(int priority) {
        super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
              new SynchronousQueue<Runnable>(), new SimulatorThreadFactory());

        this.factory = (SimulatorThreadFactory) getThreadFactory();

    }

    /**
     * Execute a Runnable and return when its finished. If this method is not
     * called from a simulation thread a new thread will be started. Otherwise
     * the old thread will be used.
     * <p>
     * If a separate simulation thread is used for execution, that thread will
     * reference the calling thread as ancestor to enable the advanced locking
     * scheme of {@link InheritableSimulationThreadLock}. The ancestor relation
     * has a lifetime limited to the execution time of this method. The calling
     * Thread is put on hold for exactly this time.
     * </p>
     *
     * @param task the task to execute
     */
    public void executeAndWait(Runnable task) {
        if (isMyThread()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Running simulation runnable directly: " + task);
            }
            task.run();
        } else {
            Semaphor block = new Semaphor();
            BlockingSimulationRunnable thread = new BlockingSimulationRunnable(task,
                                                                               block,
                                                                               Thread
                                                                               .currentThread());
            this.execute(thread);
            if (logger.isTraceEnabled()) {
                logger.trace("Running simulation runnable indirectly: " + task);
            }
            block.P();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Running simulation runnable returned: " + task);
        }
    }

    /**
     * Execute a Callable and return when its Future object when finished. If
     * this method is not called from a simulation thread a new thread will be
     * started. Otherwise the old thread will be used.
     * <p>
     * If a separate simulation thread is used for execution, that thread will
     * reference the calling thread as ancestor to enable the advanced locking
     * scheme of {@link InheritableSimulationThreadLock}. The ancestor relation
     * has a lifetime limited to the execution time of this method. The calling
     * Thread is put on hold for exactly this time.
     * </p>
     *
     * @param task
     *            the task to execute
     * @return the Future object from the executed task
     *
     */
    public <T> Future<T> submitAndWait(Callable<T> task) {
        Future<T> returnValue = null;
        if (isMyThread()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Running simulation callable directly:      "
                             + task + " in " + Thread.currentThread());
            }
            returnValue = new FutureTask<T>(task);
            ((FutureTask<T>) returnValue).run();
        } else {
            //We need to switch to the right thread group
            Semaphor block = new Semaphor();


            //Wrap callable in a callabale that will block until the run method is done 
            BlockingSimulationCallable<T> thread = new BlockingSimulationCallable<T>(task,
                                                                                     block,
                                                                                     Thread
                                                                                     .currentThread());

            //Switch to right thread group
            if (logger.isTraceEnabled()) {
                logger.trace("Running simulation callable indirectly:     "
                             + task + " in " + Thread.currentThread());
            }
            returnValue = this.submit(thread);
            if (logger.isTraceEnabled()) {
                logger.trace("Waiting for simulation callable completion: "
                             + task + " in " + Thread.currentThread());
            }
            //Block till run method of callable is done
            block.P();
        }
        logger.trace("Running simulation callable returned:       " + task
                     + " in " + Thread.currentThread());
        return returnValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the given <code>task</code> is a {@link BlockingSimulationCallable},
     * wrap it as a {@link WrappedFutureTask} to keep the ancestor thread
     * relation intact. In all other cases, behave like the default
     * implementation.
     * </p>
     **/
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        RunnableFuture<T> ftask;
        if ((task != null) && (task instanceof BlockingSimulationCallable)) {
            //We have a wrapped Callable we need to wrap the call to the Futures get() method
            //otherwise the user would get the Future of the BlockingSimulationCallable
            //instead of the expected Future of the submitted Callable
            ftask = new WrappedFutureTask<T>((BlockingSimulationCallable<T>) task);
        } else {
            ftask = super.newTaskFor(task);
        }
        return ftask;
    }

    /**
     * Provides a temporary reference to the SimulationThreadPool instance that
     * becomes the next one and only singleton.
     * <p>
     * The temporary reference becomes final when {@link #cleanup()} is called
     * on the previous instance. If the preparation should be cancelled, the
     * temporary reference must be invalidated by a call to
     * {@link #discardNew()}.
     * </p>
     *
     * @return the active SimulationThreadPool instance, if there is any.
     *         Returns <code>null</code> otherwise.
     *
     * @see SimulationThreadPool class documentation
     */
    public static SimulationThreadPool getNew() {
        synchronized (lock) {
            if (newSingleton == null) {
                newSingleton = new SimulationThreadPool(Thread.NORM_PRIORITY); //NOTICEsignature
            }
            return newSingleton;
        }
    }

    /**
     * Provides a reference to the current SimulationThreadPool instance.
     *
     * @return the active SimulationThreadPool instance.   If there is none,
     *         one is created.
     */
    public static SimulationThreadPool getCurrent() {
        synchronized (lock) {
            if (singleton == null) {
                singleton = new SimulationThreadPool(Thread.NORM_PRIORITY); //NOTICEsignature
            }
            return singleton;
        }
    }

    /**
     * Provides a reference to the current SimulationThreadPool instance.
     *
     * @return the active SimulationThreadPool instance, if there is any.
     *         Returns <code>null</code> otherwise.
     */
    public static SimulationThreadPool getSimulationThreadPool() {
        return SimulationThreadPool.getCurrent();
    }

    /**
     * Checks whether the calling thread is a simulation
     * thread (belongs to the simulation thread group
     * provided by the SimulatorThreadFactory).
     * Use this call the ensure that a given task is being
     * executed (or not being) by a simulation thread.
     * Note: This method does <em>not</em> have the side effect of thread
     * pool instantiation (like, for example, {@link #getCurrent}).
     *
     * @return <code>true<code>, if the calling thread is a simulation thread
     * @return <code>false<code>, otherwise
     */
    public static boolean isSimulationThread() {
        return (singleton != null) && (singleton.isMyThread());
    }

    /**
     * Checks whether the calling thread belongs to this thread pool instance.
     * It is checked whether the thread belongs to the simulation thread group
     * provided by this pool's {@link SimulatorThreadFactory}.
     * <p>
     * For most cases, a direct call to the static method {@link #isSimulationThread}
     * is easier and provides the correct result as well.  The result of this
     * per-instance method may differ only for the short period of time where a
     * second thread pool instance is under preparation.
     * </p>
     * @return <code>true</code>, if the calling thread belongs to this thread pool
     * @return <code>false</code>, otherwise.
     */
    public boolean isMyThread() {
        return (Thread.currentThread().getThreadGroup()
                      .equals(this.factory.getThreadGroup()));
    }

    /**
     * Checks whether the given thread belongs to this thread pool instance.
     * It is checked whether the thread belongs to the simulation thread group
     * provided by this pool's {@link SimulatorThreadFactory}.
     *
     * @return <code>true</code>, if the given thread belongs to this thread pool
     * @return <code>false</code>, otherwise.
     */
    public boolean isMyThread(Thread thread) {
        return (thread.getThreadGroup().equals(this.factory.getThreadGroup()));
    }

    /**
     * Destroys the SimulationThreadPool instance and tries to stop all
     * remaining threads gracefully. The next call of the methods
     * {@link #getCurrent()} and {@link #getSimulationThreadPool()} will
     * result in the creation of a new instance of the SimulationThreadPool.
     *
     * <p>
     * Reminder: If this method is called from within a simulation thread
     * the thread will naturally belong to the set of old threads and
     * should be discarded afterwards.
     * </p>
     *
     * @return true if successful
     */
    public static boolean cleanup() {
        synchronized (lock) {
            // if (result=false) return immediately before releasing
            // the singleton!
            SimulationThreadPool oldSingleton = singleton;
            singleton = newSingleton;
            newSingleton = null;
            oldSingleton.shutdownNow();
        }
        return true;
    }

    /**
     * Roll back an erroneous thread pool setup.  Must be called after
     * preparing a new pool instance via {@link #getNew()}, if the setup should
     * be cancelled for some reason.
     *
     * @see SimulationThreadPool class documentation
     **/
    public static void discardNew() {
        synchronized (lock) {
            SimulationThreadPool oldSingleton = newSingleton;
            newSingleton = null;
            oldSingleton.shutdown();
        }
    }

    /**
     * Set the maximum priority new threads can have. Already running threads
     * are not affected.
     * If the pri argument is less than {@link Thread.MIN_PRIORITY} or greater than
     * {@link Thread.MAX_PRIORITY}, the maximum priority of the group remains unchanged.
     * The defaults are 1 as minimum priority and 10 as maximum priority.
     *
     * @param pri
     *            new maximum priority
     */
    public void setMaxPriority(int pri) {
        this.factory.setMaxPriority(pri);

    }

    /**
     * Returns the maximum priority any new thread can have. Already running
     * threads might have higher priorities
     *
     * @return the maximum thread priority
     */
    public int getMaxPriority() {
        return this.factory.getMaxPriority();
    }

    protected void beforeExecute(Thread t, Runnable r) {
        SimulationThread st = (SimulationThread) t;

        if (r instanceof BlockingSimulationRunnable) {
            st.setAncestor(((BlockingSimulationRunnable) r).getAncestor());
        } else if (r instanceof WrappedFutureTask) {
            WrappedFutureTask<?> wft = (WrappedFutureTask<?>) r;
            st.setAncestor(wft.callable.getAncestor());
        }
    }

    /**
     * Inner class that provides a ThreadFactory with some special methods
     *
     * @author Benjamin Schleinzer
     *
     */
    static class SimulatorThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;
        final ClassLoader classLoader;

        public SimulatorThreadFactory() {
            ThreadGroup top = Thread.currentThread().getThreadGroup();
            while (top.getParent() != null) {
                top = top.getParent();
            }

            // group = new ThreadGroup(top, "simulation-thread-group");
            group = new ThreadGroup("simulation-thread-group");
            namePrefix = "simulation-thread-";
            // Load appropiate ClassLoader
            classLoader = ClassSource.getClassLoader();
        }

        /**
         * {@inheritDoc}
         */
        public Thread newThread(Runnable r) {
            Thread t = new SimulationThread(group, r,
                                            namePrefix
                                            + threadNumber.getAndIncrement(), 0);
            t.setContextClassLoader(classLoader);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            assert (t.getThreadGroup().equals(this.getThreadGroup())) : "Thread must belong to thread group of factory";
            return t;
        }

        public ThreadGroup getThreadGroup() {
            return group;
        }

        /**
         * Set the maximum priority new threads can have. Allready running
         * threads are not affected
         *
         * @param pri
         *            new maximum priority
         */
        public void setMaxPriority(int pri) {
            this.group.setMaxPriority(pri);

        }

        /**
         * Returns the maximum priority any new thread can have. Allready
         * running threads might have higher priorities
         *
         * @return the maximum thread priority
         */
        public int getMaxPriority() {
            return this.group.getMaxPriority();

        }
    }
}