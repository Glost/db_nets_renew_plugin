package de.renew.engine.common;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.TransitionInstance;

import de.renew.unify.Unify;

import java.util.Collection;
import java.util.Collections;


public class SimpleEnumeratorOccurrence extends EnumeratorOccurrence {
    private Collection<Object> collection;

    public SimpleEnumeratorOccurrence(Expression expression,
                                      boolean checkBound,
                                      VariableMapper mapper,
                                      Collection<Object> collection,
                                      TransitionInstance tInstance) {
        super(expression, checkBound, mapper, tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.collection = collection;
    }

    public Collection<Object> getCandidates(Object pattern) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        if (Unify.isBound(pattern)) {
            if (collection.contains(pattern)) {
                return Collections.singleton(pattern);
            } else {
                return Collections.emptySet();
            }
        } else {
            return collection;
        }
    }
}