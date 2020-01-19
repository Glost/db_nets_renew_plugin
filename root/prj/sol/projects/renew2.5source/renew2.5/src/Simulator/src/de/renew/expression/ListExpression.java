package de.renew.expression;

import de.renew.unify.Impossible;
import de.renew.unify.List;
import de.renew.unify.StateRecorder;


/**
 * Upon evaluation I will spawn the evaluation of several
 * supexpressions. I will then compose a list from the resulting values.
 *
 * @author Olaf Kummer
 **/
public class ListExpression extends AggregateExpression {

    /**
     * Tells me whether I should create a tailed list.
     **/
    private boolean tailed;

    /**
     * I (a list expression) am created.
     *
     * @param expressions
     *   a list that holds the expressions that must be evaluated
     **/
    public ListExpression(Expression[] expressions, boolean tailed) {
        super(expressions);
        if (tailed && expressions.length < 2) {
            throw new RuntimeException("Cannot create tailed list with less than two elements.");
        }
        this.tailed = tailed;
    }

    /**
     * Create a new list expression from exactly two
     * expressions. Because pairs are so common, it seems
     * desirable to have a short notation for them.
     *
     * @param expr1
     *   the first expression
     * @param expr2
     *   the second expression
     **/
    public ListExpression(Expression expr1, Expression expr2) {
        this(new Expression[] { expr1, expr2 }, true);
    }

    /**
     * I always return lists.
     *
     * @return de.renew.unify.List.class
     **/
    public Class<?> getType() {
        return de.renew.unify.List.class;
    }

    /**
     * Create a list.
     *
     * @param args
     *   the objects to be referenced by the constructed list
     * @param recorder
     *   a state recorder
     * @return the constructed list, possibly incomplete
     **/
    protected Object makeResultAggregate(Object[] args, StateRecorder recorder)
            throws Impossible {
        // Prepare last list item first.
        int n = args.length;
        Object result;
        if (tailed) {
            n--;
            result = args[n];
        } else {
            result = List.NULL;
        }


        // Now process other elements in reverse order, adding
        // a new pair for each new element.
        while (n > 0) {
            n--;
            result = new List(args[n], result, recorder);
        }

        return result;
    }
}