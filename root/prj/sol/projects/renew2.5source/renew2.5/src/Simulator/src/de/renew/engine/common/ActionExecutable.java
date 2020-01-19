package de.renew.engine.common;

import de.renew.engine.searcher.LateExecutable;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.Expression;
import de.renew.expression.VariableMapper;

import de.renew.unify.Impossible;


class ActionExecutable implements LateExecutable {
    Expression expression;
    VariableMapper mapper;

    ActionExecutable(Expression expression, VariableMapper mapper) {
        this.expression = expression;
        this.mapper = mapper;
    }

    // Get the phase during which this executable should fire.
    public int phase() {
        return ACTION;
    }

    // An action might take very long.
    public boolean isLong() {
        return true;
    }

    // Execute this executable.
    public void execute(StepIdentifier stepIdentifier) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        try {
            // No undo past this point, so we may use null
            // as the current state recorder. Also, no
            // calculations should be registered with the checker.
            expression.startEvaluation(mapper, null, null);
        } catch (Impossible e) {
            // That hurts. Probably a method threw an exception.
            // This should have been caught by the compiler earlier on,
            // unless the error is a RuntimeException. So let's
            // be rigrous and throw a RuntimeException again.
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new RuntimeException("Action exception in step "
                                       + stepIdentifier + ": " + e.getMessage(),
                                       cause);
        }
    }

    public void executeAfterException(StepIdentifier stepIdentifier, Throwable t) {
    }
}