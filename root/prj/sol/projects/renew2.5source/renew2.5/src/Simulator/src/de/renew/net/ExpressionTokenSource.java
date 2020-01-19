package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.unify.Impossible;
import de.renew.unify.StateRecorder;
import de.renew.unify.Unify;


public class ExpressionTokenSource implements TokenSource {
    private Expression expression;
    private boolean trace;

    public ExpressionTokenSource(Expression expression) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.expression = expression;
        trace = true;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean getTrace() {
        return trace;
    }

    public Object createToken(VariableMapper mapper) throws Impossible {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        Object value = expression.startEvaluation(mapper, new StateRecorder(),
                                                  null);
        if (!Unify.isBound(value)) {
            throw new Impossible("Cannot bind expression " + expression);
        }
        return value;
    }
}