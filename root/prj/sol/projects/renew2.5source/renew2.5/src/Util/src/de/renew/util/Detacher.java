package de.renew.util;

import java.util.Hashtable;


// I attach one thread to another until the other thread
// requires me to detach it. The attach call must precede
// the detach call.
public class Detacher {
    private static Hashtable<PoolThread, Semaphor> attachedThreads = new Hashtable<PoolThread, Semaphor>();

    /**
     * Execute the runnable either in this thread
     * or in a separate thread. This method returns after
     * the <code>detach</code> method is called from the runnable,
     * if the runnable is started in a new thread. This method returns
     * in any case after the runnable completes its run.
     *
     * @see #detach()
     *
     * @param runnable the runnable to be executed
     * @param wantNewThread true, if execution should proceed in
     *   a separate thread, but initially attached
     */
    public final static void possiblyStartAttached(Runnable runnable,
                                                   boolean wantNewThread) {
        if (wantNewThread) {
            startAttached(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Execute the runnable in a separate thread. This method returns after
     * the <code>detach</code> method is called from the runnable or
     * after the runnable completes its run.
     *
     * @see #detach()
     *
     * @param runnable the runnable to be executed
     */
    public final static void startAttached(Runnable runnable) {
        PoolThread thread = ThreadPool.get();
        Semaphor sem = new Semaphor();
        synchronized (attachedThreads) {
            attachedThreads.put(thread, sem);
        }
        thread.executeOrDiscard(new DetachingRunnable(runnable));
        sem.P();
    }

    /**
     * Detach the current thread. Multiple calls to this
     * method have no effect.
     */
    public final static void detach() {
        Thread thread = Thread.currentThread();
        synchronized (attachedThreads) {
            Semaphor lock = attachedThreads.get(thread);
            if (lock != null) {
                lock.V();
                attachedThreads.remove(thread);
            }
        }
    }
}