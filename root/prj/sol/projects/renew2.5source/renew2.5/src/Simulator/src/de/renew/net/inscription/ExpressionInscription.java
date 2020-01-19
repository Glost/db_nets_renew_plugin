package de.renew.net.inscription;

import de.renew.engine.searcher.Occurrence;
import de.renew.engine.searcher.Searcher;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.net.NetInstance;
import de.renew.net.TransitionInscription;

import de.renew.unify.Impossible;

import java.util.Collections;


public class ExpressionInscription implements TransitionInscription {
    static final long serialVersionUID = -411915174051847365L;
    Expression expression;

    public ExpressionInscription(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public java.util.Collection<Occurrence> makeOccurrences(VariableMapper mapper,
                                                            NetInstance netInstance,
                                                            Searcher searcher)
            throws Impossible {
        expression.startEvaluation(mapper, searcher.recorder,
                                   searcher.calcChecker);
        return Collections.emptySet();
    }

    public String toString() {
        return "ExpressionInscription: " + expression;
    }
}