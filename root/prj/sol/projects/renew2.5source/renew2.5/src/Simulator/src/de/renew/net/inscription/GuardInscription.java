package de.renew.net.inscription;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;

import de.renew.unify.Impossible;
import de.renew.unify.Unify;

import de.renew.util.Value;

import java.util.Collections;


public class GuardInscription implements TransitionInscription {
    Expression expression;

    public GuardInscription(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible {
        Unify.unify(expression.startEvaluation(mapper, searcher.recorder,
                                               searcher.calcChecker),
                    new Value(Boolean.TRUE), searcher.recorder);
        return Collections.emptySet();
    }
}