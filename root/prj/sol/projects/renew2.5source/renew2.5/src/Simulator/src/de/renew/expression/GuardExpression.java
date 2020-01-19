package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.Notifiable;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;
import de.renew.unify.Variable;

import de.renew.util.Value;


public class GuardExpression implements Expression {
    Expression argument;
    Function function;

    public GuardExpression(Expression argument, Function function) {
        this.argument = argument;
        this.function = function;
    }

    public boolean isInvertible() {
        return argument.isInvertible();
    }

    public Class<?> getType() {
        return argument.getType();
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

        if (checker != null) {
            checker.addEarlyVariable(source, recorder);
        }

        Notifiable sourceListener = new Notifiable() {
            public void boundNotify(StateRecorder irecorder)
                    throws Impossible {
                if (Unify.isBound(source)) {
                    Object result = function.function(source.getValue());
                    if (result instanceof Value) {
                        result = ((Value) result).value;
                    }
                    if (!Boolean.TRUE.equals(result)) {
                        throw new Impossible();
                    }
                }
            }
        };
        source.addListener(sourceListener, recorder);

        return source.getValue();
    }

    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        return argument.registerCalculation(mapper, recorder, checker);
    }

    public String toString() {
        return "GuardExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + function + ", " + argument + ")";
    }
}