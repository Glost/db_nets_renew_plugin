package de.renew.engine.common;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.TransitionInstance;

import de.renew.util.IntegerRangeSet;

import java.util.Collection;


public class RangeEnumeratorOccurrence extends EnumeratorOccurrence {
    private int first;
    private int last;

    public RangeEnumeratorOccurrence(Expression expression, boolean checkBound,
                                     VariableMapper mapper, int first,
                                     int last, TransitionInstance tInstance) {
        super(expression, checkBound, mapper, tInstance);
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.first = first;
        this.last = last;
    }

    public Collection<?extends Object> getCandidates(Object pattern) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return new IntegerRangeSet(first, last);
    }
}