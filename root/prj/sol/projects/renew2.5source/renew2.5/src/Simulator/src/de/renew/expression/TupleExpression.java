package de.renew.expression;

import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Tuple;


/**
 * Upon evaluation I spawn the evaluation of several
 * supexpressions. Afterwards, I compose a tuple from the resulting values.
 *
 * @author Olaf Kummer
 **/
public class TupleExpression extends AggregateExpression {

    /**
     * I (a tuple expression) am created.
     *
     * @param expressions
     *   a tuple that holds the expressions that must be evaluated
     **/
    public TupleExpression(Expression[] expressions) {
        super(expressions);
    }

    /**
     * Create a new tuple expression from exactly one
     * expression. Unary tuples are not used very often,
     * but they do occur sometimes.
     *
     * @param expr1
     *   the first expression
     **/
    public TupleExpression(Expression expr1) {
        this(new Expression[] { expr1 });
    }

    /**
     * Create a new tuple expression from exactly two
     * expressions. Because pairs are so common, it seems
     * desirable to have a short notation for them.
     *
     * @param expr1
     *   the first expression
     * @param expr2
     *   the second expression
     **/
    public TupleExpression(Expression expr1, Expression expr2) {
        this(new Expression[] { expr1, expr2 });
    }

    /**
     * Create a new tuple expression from exactly three
     * expressions. Because triples are still farily common, it seems
     * desirable to have a short notation for them.
     *
     * @param expr1
     *   the first expression
     * @param expr2
     *   the second expression
     * @param expr3
     *   the third expression
     **/
    public TupleExpression(Expression expr1, Expression expr2, Expression expr3) {
        this(new Expression[] { expr1, expr2, expr3 });
    }

    /**
     * I always return tuples.
     *
     * @return de.renew.unify.Tuple.class
     **/
    public Class<?> getType() {
        return de.renew.unify.Tuple.class;
    }

    /**
     * Create a Tuple.
     *
     * @param args
     *   the objects to be referenced by the constructed tuple
     * @param recorder
     *   a state recorder
     * @return the constructed tuple, possibly incomplete
     **/
    protected Object makeResultAggregate(Object[] args, StateRecorder recorder)
            throws Impossible {
        return new Tuple(args, recorder);
    }
}