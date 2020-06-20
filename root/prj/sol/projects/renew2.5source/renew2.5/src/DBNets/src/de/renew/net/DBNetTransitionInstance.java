package de.renew.net;

import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.net.event.FiringEvent;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DBNetTransitionInstance extends TransitionInstance {

    private final DBNetTransition transition;

    private final Lock lock = new ReentrantLock();

    private final Semaphore semaphore = new Semaphore(1);

    private DBNetTransitionOccurrence occurrence;

    private boolean needRollback = false;

    private boolean isBound = false;

    public DBNetTransitionInstance(DBNetControlLayerInstance netInstance, DBNetTransition transition) {
        super(netInstance, transition);
        this.transition = transition;
    }

    public boolean needRollback() {
        return needRollback;
    }

    public void setNeedRollback(boolean needRollback) {
        this.needRollback = needRollback;
    }

    public boolean isBound() {
        return isBound;
    }

    public void setBound(boolean bound) {
        isBound = bound;
    }

    public void resetOccurence() {
//        occurrence = null;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void acquire() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: ...
        }
    }

    public void release() {
        semaphore.release();
    }

    @Override
    public void startSearch(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        createAndSearchOccurrence(null, searcher);
    }

    @Override
    synchronized void firingStarted(FiringEvent fe) {
        super.firingStarted(fe);
    }

    @Override
    synchronized void firingComplete(FiringEvent fe) {
//        occurrence = null;
//        isBound = false;
        semaphore.release();
        super.firingComplete(fe);
    }

    private void createAndSearchOccurrence(Variable params, Searcher searcher) {
        int checkpoint = searcher.recorder.checkpoint();

        try {
//            if (Objects.isNull(occurrence)) {
                occurrence = new DBNetTransitionOccurrence(this, params, searcher);
//            }

            searcher.search(occurrence);
        } catch (Impossible e) {
            // When getting the binders, an exception was thrown.
            // The occurrence cannot be enabled.
        } finally {
            searcher.recorder.restore(checkpoint);
        }
    }
}
