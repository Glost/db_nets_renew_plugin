package de.renew.net.arc;

import de.renew.engine.common.AssignBinder;
import de.renew.engine.searcher.TriggerableCollection;

import de.renew.net.PlaceInstance;

import de.renew.unify.Variable;

import java.util.Collection;


class ArcAssignBinder extends AssignBinder {
    private PlaceInstance placeInstance;
    private boolean wantTest;

    public ArcAssignBinder(Variable variable, PlaceInstance placeInstance,
                           boolean wantTest) {
        super(variable, false);
        this.placeInstance = placeInstance;
        this.wantTest = wantTest;
    }

    /**
     * Locks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public final void lock() {
        placeInstance.lock.lock();
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public final void unlock() {
        placeInstance.lock.unlock();
    }

    public Collection<Object> getCandidates(Object pattern) {
        if (wantTest) {
            return placeInstance.getDistinctTestableTokens(pattern);
        } else {
            return placeInstance.getDistinctTokens(pattern);
        }
    }

    public TriggerableCollection getTriggerables() {
        return placeInstance.triggerables();
    }
}