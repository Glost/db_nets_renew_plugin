package de.renew.net.arc;

import de.renew.engine.common.SimulatorEventLogger;
import de.renew.engine.common.StepIdentifier;
import de.renew.engine.events.Clearing;
import de.renew.engine.searcher.EarlyExecutable;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import de.renew.util.Value;

import java.lang.reflect.Array;

import java.util.Vector;


class ClearArcExecutable implements EarlyExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Variable variable;
    ClearArc arc;
    Vector<Object> removedTokens;
    Vector<Double> removedTimeStamps;

    ClearArcExecutable(PlaceInstance pInstance, TransitionInstance tInstance,
                       Variable variable, ClearArc arc) {
        this.pInstance = pInstance;
        this.tInstance = tInstance;
        this.variable = variable;
        this.arc = arc;
    }

    public long lockPriority() {
        return pInstance.lockOrder;
    }

    public int phase() {
        return CLEAR;
    }

    /**
     * Locks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void lock() {
        pInstance.lock.lock();
    }

    public void verify(StepIdentifier stepIdentifier) throws Impossible {
        // Ensure large size increments.
        removedTokens = new Vector<Object>(8, 0);
        removedTimeStamps = new Vector<Double>(8, 0);

        pInstance.extractAllTokens(removedTokens, removedTimeStamps);

        Object result = Array.newInstance(arc.elementType, removedTokens.size());
        for (int i = 0; i < removedTokens.size(); i++) {
            Object value = removedTokens.elementAt(i);
            if (arc.elementType.isPrimitive()) {
                value = Value.unvalueAndCast(value, arc.elementType);
            }
            Array.set(result, i, value);
        }

        Unify.unify(variable, result, null);
    }

    public void execute(StepIdentifier stepIdentifier) {
        if (arc.getTrace()) {
            // log activities on net level
            SimulatorEventLogger.log(stepIdentifier, new Clearing(pInstance),
                                     pInstance);
        }
    }

    public void rollback() {
        // We have to undo the previous removal. We cannot do this
        // without notifying the observers and listeners, because
        // it was not done silently. However, the database must not log
        // this modification.
        for (int i = 0; i < removedTokens.size(); i++) {
            Object token = removedTokens.elementAt(i);
            double time = removedTimeStamps.elementAt(i).doubleValue();
            pInstance.internallyInsertToken(token, time, false);
        }
        removedTokens = null;
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void unlock() {
        pInstance.lock.unlock();
    }
}