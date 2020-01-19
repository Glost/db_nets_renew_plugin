package de.renew.net.inscription;

import de.renew.engine.common.SimpleEnumeratorOccurrence;
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


public class SimpleEnumeratorInscription implements TransitionInscription {
    private Expression expr;
    private boolean checkBound;
    private Collection<Object> collection;
    private Transition transition;

    public SimpleEnumeratorInscription(Expression expr,
                                       Collection<Object> collection,
                                       boolean checkBound, Transition transition) {
        this.expr = expr;
        this.collection = collection;
        this.checkBound = checkBound;
        this.transition = transition;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new SimpleEnumeratorOccurrence(expr, checkBound, mapper,
                                                collection,
                                                netInstance.getInstance(transition)));
        return coll;
    }
}