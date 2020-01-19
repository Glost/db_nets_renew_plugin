package de.renew.net.arc;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.EarlyExecutable;
import de.renew.engine.searchqueue.SearchQueue;

import de.renew.net.PlaceInstance;
import de.renew.net.TransitionInstance;

import de.renew.unify.Impossible;

import java.util.Vector;


class FlexibleInArcExecutable implements EarlyExecutable {
    PlaceInstance pInstance;
    TransitionInstance tInstance;
    Vector<Object> tokens;
    FlexibleArc arc;
    int numRemoved;
    Vector<Double> removedTimes;

    FlexibleInArcExecutable(PlaceInstance placeInstance,
                            TransitionInstance tInstance,
                            Vector<Object> tokens, FlexibleArc arc) {
        this.pInstance = placeInstance;
        this.tInstance = tInstance;
        this.tokens = tokens;
        this.arc = arc;
        numRemoved = 0;
        removedTimes = new Vector<Double>();
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
            for (int i = 0; i < tokens.size(); i++) {
                Object token = tokens.elementAt(i);
                double removedTime = pInstance.removeToken(token, 0);
                removedTimes.addElement(new Double(removedTime));
                numRemoved++;
            }
        } catch (Exception e) {
            // Undo all reservations.
            rollback();

            throw new Impossible();
        }
    }

    public void execute(StepIdentifier stepIdentifier) {
        for (int i = 0; i < numRemoved; i++) {
            Object token = tokens.elementAt(i);
            if (arc.arcType == FlexibleArc.fastBoth) {
                if (arc.trace) {
                    InputArcExecutable.traceInArc(stepIdentifier, true, token,
                                                  pInstance, tInstance); //NOTICEsignature
                }
                pInstance.insertToken(token, SearchQueue.getTime());
            } else {
                if (arc.trace) {
                    InputArcExecutable.traceInArc(stepIdentifier, false, token,
                                                  pInstance, tInstance); //NOTICEsignature
                }
            }
        }
    }

    public void rollback() {
        // We have to undo the previous removals. We cannot do this
        // without notifying the observers and listeners, because
        // it was not done silently. However, the database must not log
        // this modification.
        for (int i = 0; i < numRemoved; i++) {
            pInstance.internallyInsertToken(tokens.elementAt(i),
                                            removedTimes.elementAt(i)
                                                        .doubleValue(), false);
        }
    }

    /**
     * Unlocks the <code>PlaceInstance</code> associated with this arc.
     * @see PlaceInstance#lock
     **/
    public void unlock() {
        pInstance.lock.unlock();
    }
}