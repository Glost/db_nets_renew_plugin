package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Inhibiting;
import de.renew.engine.searcher.EarlyExecutable;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;


class InhibitorExecutable implements EarlyExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Object token;
    boolean trace;

    InhibitorExecutable(PlaceInstance placeInstance,
                        TransitionInstance tInstance, Object token,
                        boolean trace) {
        this.pInstance = placeInstance;
        this.tInstance = tInstance;
        this.token = token;
        this.trace = trace;
    }

    public long lockPriority() {
        return pInstance.lockOrder;
    }

    public int phase() {
        // Test before other executables have the chance to remove
        // tokens from the place.
        return INHIBIT;
    }

    /**
     * Locks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void lock() {
        pInstance.lock.lock();
    }

    public void verify(StepIdentifier stepIdentifier) throws Impossible {
        if (pInstance.containsTestableToken(token)) {
            // Undo locking.
            throw new Impossible();
        }
    }

    public void execute(StepIdentifier stepIdentifier) {
        // Does it really make sense to output a trace message?
        if (trace) {
            // log activities on net level
            SimulatorEventLogger.log(stepIdentifier,
                                     new Inhibiting(token, pInstance), pInstance);
        }
    }

    public void rollback() {
        // No harm done.
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void unlock() {
        pInstance.lock.unlock();
    }
}