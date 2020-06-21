package de.renew.net;

import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.net.event.FiringEvent;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.concurrent.Semaphore;

/**
 * The db-net's transition's instance for the simulation.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetTransitionInstance extends TransitionInstance {

    /**
     * The semaphore for locking the transition's instance's binders.
     */
    private final Semaphore semaphore = new Semaphore(1);

    /**
     * Stores whether the transition's action's rollback is requested currently or not.
     */
    private boolean needRollback = false;

    /**
     * The db-net's transition's instance's constructor.
     *
     * @param netInstance The db-net's control layer's instance.
     * @param transition The db-net's transition.
     */
    public DBNetTransitionInstance(DBNetControlLayerInstance netInstance, DBNetTransition transition) {
        super(netInstance, transition);
    }

    /**
     * Returns whether the transition's action's rollback is requested currently or not.
     *
     * @return true if the transition's action's rollback is requested currently, false otherwise.
     */
    public boolean needRollback() {
        return needRollback;
    }

    /**
     * Sets whether the transition's action's rollback is requested currently or not.
     *
     * @param needRollback true if the transition's action's rollback is requested currently, false otherwise.
     */
    public void setNeedRollback(boolean needRollback) {
        this.needRollback = needRollback;
    }

    /**
     * Acquires the semaphore's permit for locking the transition's instance's binders.
     */
    public void acquire() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the binding search for this db-net's transition's instance.
     * Based on the {@link super#startSearch(Searcher)} implementation.
     *
     * @param searcher The searcher instance.
     */
    @Override
    public void startSearch(Searcher searcher) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        createAndSearchOccurrence(null, searcher);
    }

    /**
     * Handles the transition firing complete event.
     * Releases the transition's semaphore.
     *
     * @param fe The firing event instance.
     */
    @Override
    synchronized void firingComplete(FiringEvent fe) {
        semaphore.release();
        super.firingComplete(fe);
    }

    /**
     * Creates the db-net's transition's occurrence and start binding search in this.
     *
     * @param params The param variables of the transition.
     * @param searcher The searcher instance.
     */
    private void createAndSearchOccurrence(Variable params, Searcher searcher) {
        int checkpoint = searcher.recorder.checkpoint();

        try {
            searcher.search(new DBNetTransitionOccurrence(this, params, searcher));
        } catch (Impossible e) {
            // When getting the binders, an exception was thrown.
            // The occurrence cannot be enabled.
        } finally {
            searcher.recorder.restore(checkpoint);
        }
    }
}
