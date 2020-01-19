package de.renew.navigator.io;



/**
 * @author Konstantin Simon Maria M??llers
 * @version 0.1
 */
public interface ProgressListener {

    /**
     * Each execution informs the GUI about a new progress.
     *
     * @param progress the current progress.
     * @param max the maximum progress.
     */
    void progress(float progress, int max);

    /**
     * Informs the progress whether its worker got cancelled.
     */
    boolean isWorkerCancelled();
}