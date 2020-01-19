package de.renew.net.inscription;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInscription;

import java.util.Collection;
import java.util.Vector;


public class ConditionalInscription implements TransitionInscription {
    Expression conditionExpression;
    TransitionInscription inscription;
    Transition transition;

    public ConditionalInscription(Expression conditionExpression,
                                  TransitionInscription inscription,
                                  Transition transition) {
        this.conditionExpression = conditionExpression;
        this.inscription = inscription;
        this.transition = transition;
    }

    public Expression getExpression() {
        return conditionExpression;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher) {
        Collection<Occurrence> coll = new Vector<Occurrence>();
        coll.add(new ConditionalOccurrence(this, mapper, netInstance,
                                           this.transition));
        return coll;
    }
}