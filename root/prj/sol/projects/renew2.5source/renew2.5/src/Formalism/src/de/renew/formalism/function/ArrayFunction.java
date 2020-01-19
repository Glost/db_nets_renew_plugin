package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.Value;

import java.lang.reflect.InvocationTargetException;


public class ArrayFunction implements Function {
    public final static Function FUN = new ArrayFunction();

    private ArrayFunction() {
    }

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 2) {
            throw new Impossible();
        }

        try {
            Object arr = tuple.getComponent(0);
            Object idx = Value.unvalueAndCast(tuple.getComponent(1),
                                              Integer.TYPE);
            return Value.possiblyWrap(java.lang.reflect.Array.get(arr,
                                                                  ((Integer) idx)
                                                                  .intValue()),
                                      arr.getClass().getComponentType()
                                         .isPrimitive());
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                throw new Impossible("InvocationTargetException occured during method call: "
                                     + ((InvocationTargetException) e)
                                         .getTargetException());
            } else {
                throw new Impossible("Exception occured during method call: "
                                     + e);
            }
        }
    }
}