package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;


public class EqualsExpression extends ExpressionWithTypeField {
    Expression left;
    Expression right;

    public EqualsExpression(Class<?> targetType, Expression left,
                            Expression right) {
        super(targetType);
        this.left = left;
        this.right = right;
    }

    public boolean isInvertible() {
        return true;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        // The left result must be stored in a variable, because
        // it might be an unknown that changes its value
        // during the evluation of the right hand side.
        Variable result = new Variable(left.startEvaluation(mapper, recorder,
                                                            checker), recorder);

        Object rightResult = right.startEvaluation(mapper, recorder, checker);

        Unify.unify(result, rightResult, recorder);

        return result.getValue();
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        // The left result must be stored in a variable, because
        // it might be an unknown that changes its value
        // during the evluation of the right hand side.
        Variable result = new Variable(left.registerCalculation(mapper,
                                                                recorder,
                                                                checker),
                                       recorder);
        Object rightObject = right.registerCalculation(mapper, recorder, checker);

        checker.addCalculated(getType(), result.getValue(), rightObject,
                              recorder);

        return result.getValue();
    }

    public String toString() {
        return "EqualsExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + left + " = " + right + ")";
    }
}