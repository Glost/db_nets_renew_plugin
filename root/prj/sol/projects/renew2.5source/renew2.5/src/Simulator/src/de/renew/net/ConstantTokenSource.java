package de.renew.net;

import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.expression.VariableMapper;


public class ConstantTokenSource implements TokenSource {
    private Object constant;

    public ConstantTokenSource(Object constant) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        this.constant = constant;
    }

    public Object createToken(VariableMapper mapper) {
        assert SimulationThreadPool.isSimulationThread() : "is not in a simulation thread";
        return constant;
    }
}