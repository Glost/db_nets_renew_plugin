package de.renew.util;

class PoolThread extends Thread {
    private Semaphor startSem = new Semaphor();
    private Runnable runnable;

    /**
     * Creates a new <code>PoolThread</code> instance.
     * Such thread start immediately, but will wait until
     * a runnable is passed to them.
     */
    PoolThread() {
        start();
    }

    /**
     * Either execute a runnable or discard the pool thread.
     *
     * @param runnable <code>null</code> to discard this object,
     *   any other value to execute a runnable
     */
    void executeOrDiscard(Runnable runnable) {
        this.runnable = runnable;
        startSem.V();
    }

    public void run() {
        while (true) {
            startSem.P();
            if (runnable == null) {
                return;
            }
            runnable.run();


            // Allow garbage collection.
            runnable = null;


            //Allow reuse of this pool thread.
            ThreadPool.put(this);
        }
    }
}