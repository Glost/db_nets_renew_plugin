package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;


public class NoArgExpression extends ExpressionWithTypeField {
    NoArgFunction function;

    public NoArgExpression(Class<?> targetType, NoArgFunction function) {
        super(targetType);
        this.function = function;
    }

    public boolean isInvertible() {
        // Because there is only one value that this expression
        // can take, there is really no use in starting a search
        // process.
        return false;
    }

    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        return function.function();
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        Variable target = new Variable();
        checker.addCalculated(getType(), target, null, recorder);
        return target.getValue();
    }

    public String toString() {
        return "NoArgExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + function + ")";
    }
}