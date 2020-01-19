package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.Notifiable;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;


public class CallExpression extends ExpressionWithTypeField {
    // Multiple arguments are modelled by an expression that returns
    // a tuple that is processed by a function.
    Expression argument;
    Function function;

    public CallExpression(Class<?> targetType, Expression argument,
                          Function function) {
        super(targetType);
        this.argument = argument;
        this.function = function;
    }

    public boolean isInvertible() {
        return false;
    }

    public Expression getArgument() {
        return argument;
    }

    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        final Variable source = new Variable(argument.startEvaluation(mapper,
                                                                      recorder,
                                                                      checker),
                                             recorder);
        final Variable target = new Variable();

        if (checker != null) {
            checker.addEarlyVariable(source, recorder);
        }

        expressionConstraint(target, function, source, recorder);

        return target.getValue();
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        Variable source = new Variable(argument.registerCalculation(mapper,
                                                                    recorder,
                                                                    checker),
                                       recorder);
        Variable target = new Variable();

        checker.addLateVariable(source, recorder);
        checker.addCalculated(getType(), target, source.getValue(), recorder);

        return target.getValue();
    }

    // Ensure that target==function(source) after unification.
    // The equation is guaranteed whenever source is bound.
    public static void expressionConstraint(final Variable target,
                                            final Function function,
                                            final Variable source,
                                            StateRecorder recorder)
            throws Impossible {
        // Create a listener.
        Notifiable listener = new Notifiable() {
            public void boundNotify(StateRecorder irecorder)
                    throws Impossible {
                if (Unify.isBound(source)) {
                    Unify.unify(target, function.function(source.getValue()),
                                irecorder);
                }
            }
        };
        source.addListener(listener, recorder);
    }

    public String toString() {
        return "CallExpr(" + de.renew.util.Types.typeToString(getType()) + ": "
               + function + ", " + argument + ")";
    }
}