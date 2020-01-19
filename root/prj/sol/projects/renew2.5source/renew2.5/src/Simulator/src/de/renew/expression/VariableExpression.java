package de.renew.expression;

import de.renew.unify.CalculationChecker;
import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Variable;


/**
 * I represent variables in expressions. Upon evaluation
 * I make sure to map the local variable I was given
 * at creation time to the value of this variable.
 *
 * @author Olaf Kummer
 **/
public class VariableExpression extends ExpressionWithTypeField {

    /**
     * Here I store the name of the local variable
     * that I must evaluate. The special value <tt>null</tt> indicates
     * that I generate anonymous variables.
     **/
    LocalVariable localVariable;

    /**
     * I (a variable expression) am created for a given local variable.
     *
     * @param type
     *   The type of the variable that should be mapped during evaluation.
     * @param localVariable
     *   The local variable or null if an anonymous variable must be
     *   generated.
     **/
    public VariableExpression(Class<?> type, LocalVariable localVariable) {
        super(type);
        this.localVariable = localVariable;
    }

    /**
     * Am I invertible? Of course I am.
     *
     * @return true
     **/
    public boolean isInvertible() {
        return true;
    }

    /**
     * I will return either the value of my associated variable
     * or an appropriate unknown object.
     *
     * @return the current variable value
     **/
    public Object startEvaluation(VariableMapper mapper,
                                  StateRecorder recorder,
                                  CalculationChecker checker)
            throws Impossible {
        if (localVariable == null) {
            return new Variable().getValue();
        } else {
            return mapper.map(localVariable).getValue();
        }
    }

    /**
     * I will return either the value of my associated variable
     * or an appropriate unknown object. Unlike my colleagues,
     * I need not make a distinction between evaluation and
     * registration.
     *
     * @return the current variable value
     **/
    public Object registerCalculation(VariableMapper mapper,
                                      StateRecorder recorder,
                                      CalculationChecker checker)
            throws Impossible {
        return startEvaluation(mapper, recorder, checker);
    }

    public String toString() {
        return "VariableExpr(" + de.renew.util.Types.typeToString(getType())
               + ": " + localVariable + ")";
    }

    public LocalVariable getVariable() {
        return localVariable;
    }
}