package de.renew.net;

import de.renew.engine.searcher.Searcher;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.net.event.FiringEvent;
import de.renew.unify.Impossible;
import de.renew.unify.Variable;

import java.util.Objects;

public class DBNetTransitionInstance extends TransitionInstance {

    private final DBNetTransition transition;

    private DBNetTransitionOccurrence occurrence;

    private boolean needRollback = false;

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
        super.firingComplete(fe);
    }

    private void createAndSearchOccurrence(Variable params, Searcher searcher) {
        int checkpoint = searcher.recorder.checkpoint();

        try {
            if (Objects.isNull(occurrence)) {
                occurrence = new DBNetTransitionOccurrence(this, params, searcher);
            }

            searcher.search(occurrence);
        } catch (Impossible e) {
            // When getting the binders, an exception was thrown.
            // The occurrence cannot be enabled.
        } finally {
            searcher.recorder.restore(checkpoint);
        }
    }
}
