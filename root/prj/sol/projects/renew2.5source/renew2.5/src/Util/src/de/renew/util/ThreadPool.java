package de.renew.util;

import java.util.Vector;


/**
 * The <code>ThreadPool</code> runs runnables in threads that
 * are recycled after the runnable completes.
 *
 * @author Olaf Kummer
 */
public class ThreadPool {
    private static int MAXPOOLSIZE = 32;
    private static Vector<PoolThread> pool = new Vector<PoolThread>(MAXPOOLSIZE);

    static void put(PoolThread thread) {
        synchronized (pool) {
            if (pool.size() < MAXPOOLSIZE) {
                pool.addElement(thread);
            } else {
                thread.executeOrDiscard(null);
            }
        }
    }

    static PoolThread get() {
        synchronized (pool) {
            int size = pool.size();
            if (size == 0) {
                return new PoolThread();
            } else {
                PoolThread thread = pool.elementAt(size - 1);
                pool.removeElementAt(size - 1);
                return thread;
            }
        }
    }
}