package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.Notifiable;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;


public class InvertibleExpression extends ExpressionWithTypeField {
    // Multiple arguments are modelled by an expression that returns
    // a tuple that is processed by a function.
    Expression argument;
    Function forwardFunction;
    Function backwardFunction;

    public InvertibleExpression(Class<?> targetType, Expression argument,
                                Function forwardFunction,
                                Function backwardFunction) {
        super(targetType);
        this.argument = argument;
        this.forwardFunction = forwardFunction;
        this.backwardFunction = backwardFunction;
    }

    public boolean isInvertible() {
        return argument.isInvertible();
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

        Notifiable sourceListener = new Notifiable() {
            public void boundNotify(StateRecorder irecorder)
                    throws Impossible {
                if (Unify.isBound(source)) {
                    Unify.unify(target,
                                forwardFunction.function(source.getValue()),
                                irecorder);
                }
            }
        };
        source.addListener(sourceListener, recorder);

        Notifiable targetListener = new Notifiable() {
            public void boundNotify(StateRecorder irecorder)
                    throws Impossible {
                if (Unify.isBound(target)) {
                    Unify.unify(source,
                                backwardFunction.function(target.getValue()),
                                irecorder);
                }
            }
        };
        target.addListener(targetListener, recorder);

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

    public String toString() {
        return "InvertibleExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + forwardFunction + ", " + backwardFunction + ", "
               + argument + ")";
    }
}