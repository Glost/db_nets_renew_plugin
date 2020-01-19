package de.renew.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * In essence this class duplicates the functionality of the
 * synchronized statement. But locks can be used without
 * a textual matching in the source code, which allows
 * more flexible (and hence more dangerous) synchronization
 * schemes.
 **/
public class Lock implements Serializable {
    public static Logger logger = Logger.getLogger(Lock.class);
    private transient List<Throwable> lastLockTraces;
    private transient Thread lockingThread;
    private transient int lockCount;

    public Lock() {
        lockingThread = null;
        lockCount = 0;
        lastLockTraces = new ArrayList<Throwable>();
    }

    public synchronized void lock() {
        Thread currentThread = Thread.currentThread();
        while (lockingThread != null && !mayLock(lockingThread)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(new LockDebugLogEvent("Thread "
                                                       + Thread.currentThread()
                                                       + " has to wait for lock.",
                                                       this, lockCount,
                                                       lastLockTraces,
                                                       new Throwable("Waiting thread trace")));
                }
                wait();
            } catch (InterruptedException e) {
            }
        }

        lockCount++;
        if (logger.isTraceEnabled()) {
            logger.trace("Locked " + this.toString() + " by Thread: "
                         + currentThread.toString());
        }
        if (lockCount == 1) {
            lockingThread = currentThread;
        }
        if (logger.isDebugEnabled()) {
            lastLockTraces.add(new Throwable("Locking thread trace no. "
                                             + lockCount));
        }
    }

    public synchronized void unlock() {
        verify();
        lockCount--;
        if (logger.isTraceEnabled()) {
            logger.trace("Unlocked " + this.toString() + " by Thread: "
                         + Thread.currentThread().toString());
        }
        if (lockCount == 0) {
            lockingThread = null;
            lastLockTraces.clear();
        }
        notify();
    }

    public synchronized void verify() {
        if (!mayLock(lockingThread)) {
            if (lockingThread == null) {
                throw new IllegalStateException("The lock is not locked.");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(new LockDebugLogEvent("The lock was locked by another thread.",
                                                       this, lockCount,
                                                       lastLockTraces,
                                                       new Throwable("Wrong unlocking thread trace")));
                }
                throw new IllegalStateException("The lock was locked by another thread ("
                                                + lockingThread + ",hc="
                                                + lockingThread.hashCode()
                                                + " instead of "
                                                + Thread.currentThread()
                                                + ",hc="
                                                + Thread.currentThread()
                                                        .hashCode() + ").");
            }
        }
    }

    /**
     * Determines whether the current thread is allowed to lock
     * this lock which is already assigned to <code>lockingThread</code>.
     * This method is intended to be overridden by subclasses to implement
     * different locking policies.
     *
     * @param lockingThread The thread that currently holds this lock.
     *
     * @return true if the current thread is allowed to re-acquire the lock, false
     * otherwise
     *
     */
    protected boolean mayLock(Thread lockingThread) {
        return Thread.currentThread() == lockingThread;
    }

    /**
     * Serialization method, behaves like default writeObject
     * method except checking additional error conditions.
     * Throws NotSerializableException if the lock is in use.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        synchronized (this) {
            if ((lockCount == 0) && (lockingThread == null)) {
                out.defaultWriteObject();
            } else {
                throw new NotSerializableException("de.renew.util.Lock: "
                                                   + this + " is in use.");
            }
        }
    }

    /**
     * Deserialization method, behaves like default readObject
     * method except restoring additional transient fields.
     * Resets lockCount and lockingThread to null.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lockCount = 0;
        lockingThread = null;
        lastLockTraces = new ArrayList<Throwable>();
    }

    private static class LockDebugLogEvent {
        private final String situation;
        private final Lock lock;
        private final int lockCount;
        private final List<Throwable> lastLockTraces;
        private final Throwable currentTrace;

        public LockDebugLogEvent(String situation, Lock lock, int lockCount,
                                 List<Throwable> lastLockTraces,
                                 Throwable currentTrace) {
            this.situation = situation;
            this.lock = lock;
            this.lockCount = lockCount;
            this.lastLockTraces = new ArrayList<Throwable>(lastLockTraces);
            this.currentTrace = currentTrace;
        }

        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);

            out.println(situation);
            out.println("Lock: " + lock);
            out.println("Current lock count: " + lockCount);
            currentTrace.printStackTrace(out);
            for (Iterator<Throwable> traces = lastLockTraces.iterator();
                         traces.hasNext();) {
                Throwable trace = traces.next();
                if (trace != null) {
                    trace.printStackTrace(out);
                }
            }
            return writer.toString();
        }
    }
}