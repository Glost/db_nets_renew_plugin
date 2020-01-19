package de.renew.net.inscription;

import de.renew.engine.common.RangeEnumeratorOccurrence;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import de.renew.unify.Impossible;

import java.util.Collection;
import java.util.Vector;


public class RangeEnumeratorInscription implements TransitionInscription {
    private Expression expr;
    private boolean checkBound;
    private int first;
    private int last;
    private Transition transition;

    public RangeEnumeratorInscription(Expression expr, int first, int last,
                                      boolean checkBound, Transition transition) {
        this.expr = expr;
        this.first = first;
        this.last = last;
        this.checkBound = checkBound;
        this.transition = transition;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new RangeEnumeratorOccurrence(expr, checkBound, mapper, first,
                                               last,
                                               netInstance.getInstance(transition)));
        return coll;
    }
}