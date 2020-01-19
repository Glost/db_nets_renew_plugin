package de.renew.net.inscription;

import de.renew.engine.common.ActionOccurrence;
import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


public class ActionInscription implements TransitionInscription {
    Expression expression;
    Transition transition;

    public ActionInscription(Expression expression, Transition transition) {
        this.expression = expression;
        this.transition = transition;
    }

    public Expression getExpression() {
        return expression;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new ActionOccurrence(expression, mapper,
                                      netInstance.getInstance(this.transition)));
        return coll;
    }
}