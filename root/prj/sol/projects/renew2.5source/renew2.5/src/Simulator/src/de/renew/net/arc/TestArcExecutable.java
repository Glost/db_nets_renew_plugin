package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.QuicklyTesting;
import de.renew.engine.events.Testing;
import de.renew.engine.searcher.EarlyExecutable;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;


class TestArcExecutable implements EarlyExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Object token;
    boolean trace;
    boolean releaseImmediately;

    TestArcExecutable(PlaceInstance pInstance, TransitionInstance tInstance,
                      Object token, boolean releaseImmediately, boolean trace) {
        this.pInstance = pInstance;
        this.tInstance = tInstance;
        this.token = token;
        this.trace = trace;


        // If releaseImmediately is false, the tested token will remain
        // tested until it is explicitly untested.
        this.releaseImmediately = releaseImmediately;
    }

    public long lockPriority() {
        return pInstance.lockOrder;
    }

    public int phase() {
        return TEST;
    }

    /**
     * Locks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void lock() {
        pInstance.lock.lock();
    }

    public void verify(StepIdentifier stepIdentifier) throws Impossible {
        pInstance.testToken(token);
    }

    public void execute(StepIdentifier stepIdentifier) {
        if (releaseImmediately) {
            if (trace) {
                SimulatorEventLogger.log(stepIdentifier,
                                         new QuicklyTesting(token, pInstance),
                                         pInstance);
            }
            pInstance.untestToken(token);
        } else {
            if (trace) {
                SimulatorEventLogger.log(stepIdentifier,
                                         new Testing(token, pInstance),
                                         pInstance);
            }
        }
    }

    public void rollback() {
        pInstance.untestToken(token);
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void unlock() {
        pInstance.lock.unlock();
    }
}