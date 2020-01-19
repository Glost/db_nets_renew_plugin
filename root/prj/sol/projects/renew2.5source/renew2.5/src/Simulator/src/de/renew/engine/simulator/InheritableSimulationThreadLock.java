package de.renew.engine.simulator;

import de.renew.util.Lock;


/**
 * This is a specialized lock for simulation threads managed by the
 * {@link SimulationThreadPool}. A simulation thread is allowed to reenter a
 * lock that has been acquired by its ancestor thread. An ancestor thread is
 * defined as the thread that called
 * {@link SimulationThreadPool#executeAndWait(Runnable)} or
 * {@link SimulationThreadPool#submitAndWait(Callable)} and is waiting for the
 * completion of the simulation thread that executes the respective Runnable or
 * Callable.
 *
 * @author Matthias Wester-Ebbinghaus
 * @author Michael Duvigneau
 * @since Renew 2.2
 */
public class InheritableSimulationThreadLock extends Lock {
    protected boolean mayLock(Thread lockingThread) {
        if (lockingThread == Thread.currentThread()) {
            return true;
        } else if (Thread.currentThread() instanceof SimulationThread) {
            SimulationThread st = (SimulationThread) Thread
                                      .currentThread();

            //System.out.println("Current thread: " + st);
            //System.out.println("Ancestor thread: " + st.getAncestor());
            //System.out.println("Locking thread: " + lockingThread);
            boolean result = (lockingThread == st.getAncestor());

            //System.out.println("May lock: " + result);
            return result;
        }
        return false;
    }
}