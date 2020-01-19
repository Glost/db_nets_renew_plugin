package de.renew.formalism.function;

import de.renew.expression.NoArgFunction;

import de.renew.unify.Impossible;


public class ConstantFunction implements NoArgFunction {
    private Object obj;

    public ConstantFunction(Object obj) {
        this.obj = obj;
    }

    public Object function() throws Impossible {
        return obj;
    }
}