package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Checking;
import de.renew.engine.events.Removing;
import de.renew.engine.searcher.EarlyExecutable;
import de.renew.engine.searchqueue.SearchQueue;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;


class InputArcExecutable implements EarlyExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Object token;
    double delay;
    boolean trace;
    boolean releaseImmediately;

    // This field will store the time stamp of the removed token
    // after a successful removal.
    double removeTime;

    InputArcExecutable(PlaceInstance pInstance, TransitionInstance tInstance,
                       Object token, double delay, boolean releaseImmediately,
                       boolean trace) {
        this.pInstance = pInstance;
        this.tInstance = tInstance;
        this.token = token;
        this.delay = delay;
        this.trace = trace;


        // If releaseImmediately is true, the arc behaves like
        // a double arc that puts the token back as soon as possible.
        this.releaseImmediately = releaseImmediately;
    }

    public long lockPriority() {
        return pInstance.lockOrder;
    }

    public int phase() {
        return INPUT;
    }

    /**
     * Locks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void lock() {
        pInstance.lock.lock();
    }

    public void verify(StepIdentifier stepIdentifier) throws Impossible {
        try {
            removeTime = pInstance.removeToken(token, delay);
        } catch (Impossible e) {
            // No reservation actually took place.
            throw e;
        }
    }

    static void traceInArc(StepIdentifier stepIdentifier, boolean checking,
                           Object token, PlaceInstance pInstance,
                           TransitionInstance tInstance) { //NOTICEsignature
        if (checking) {
            SimulatorEventLogger.log(stepIdentifier,
                                     new Checking(token, pInstance), pInstance);
        } else {
            SimulatorEventLogger.log(stepIdentifier,
                                     new Removing(token, pInstance), pInstance);
        }
    }

    public void execute(StepIdentifier stepIdentifier) {
        if (releaseImmediately) {
            if (trace) {
                traceInArc(stepIdentifier, true, token, pInstance, tInstance); //NOTICEsignature
            }


            // It would be conceivable to put the token back at
            // a different time.
            pInstance.insertToken(token, SearchQueue.getTime());
        } else {
            if (trace) {
                traceInArc(stepIdentifier, false, token, pInstance, tInstance); //NOTICEsignature
            }
        }
    }

    public void rollback() {
        // We have to undo the previous removal. We cannot do this
        // without notifying the observers and listeners, because
        // the removal was not done silently. However, the database must not log
        // this modification.
        pInstance.internallyInsertToken(token, removeTime, false);
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void unlock() {
        pInstance.lock.unlock();
    }
}