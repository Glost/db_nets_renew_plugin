package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;

import de.renew.util.Value;


public class ConstantExpression extends ExpressionWithTypeField {
    public static Expression doubleZeroExpression = new ConstantExpression(Double.TYPE,
                                                                           new Value(new Double(0)));
    static final long serialVersionUID = 6457217584489705338L;
    private Object constant;

    public ConstantExpression(Class<?> targetType, Object constant) {
        super(targetType);
        this.constant = constant;
    }

    public Object getConstant() {
        return constant;
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
        return constant;
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        return constant;
    }

    public String toString() {
        return "ConstantExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + constant + ")";
    }
}