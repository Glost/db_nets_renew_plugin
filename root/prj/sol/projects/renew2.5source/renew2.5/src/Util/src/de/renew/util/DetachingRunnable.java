package de.renew.util;

class DetachingRunnable implements Runnable {
    private Runnable runnable;

    DetachingRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        runnable.run();
        Detacher.detach();
    }
}