package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.TypeConstrainer;
import de.renew.unify.Variable;


public class TypeCheckingExpression extends ExpressionWithTypeField {
    Expression argument;

    public TypeCheckingExpression(Class<?> type, Expression argument) {
        super(type);
        this.argument = argument;
    }

    public Expression getArgument() {
        return argument;
    }

    public boolean isInvertible() {
        return argument.isInvertible();
    }

    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        final Object result = argument.startEvaluation(mapper, recorder, checker);
        TypeConstrainer.constrain(getType(), result, recorder);
        return result;
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        // During an action, no backward information transfer is supposed
        // to happen. Hence we can simply register a new calculation.
        // This has the advantage of showing the correct result type.
        Variable target = new Variable();

        checker.addCalculated(getType(), target,
                              argument.registerCalculation(mapper, recorder,
                                                           checker), recorder);

        return target.getValue();
    }

    public String toString() {
        return "TypeCheckingExpr("
               + de.renew.util.Types.typeToString(getType()) + ": " + argument
               + ")";
    }
}