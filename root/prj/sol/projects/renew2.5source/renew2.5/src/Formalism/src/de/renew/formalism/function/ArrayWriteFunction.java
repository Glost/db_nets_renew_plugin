package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.Value;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;


public class ArrayWriteFunction implements Function {
    public final static Function FUN = new ArrayWriteFunction();

    private ArrayWriteFunction() {
    }

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 3) {
            throw new Impossible();
        }

        try {
            Object arr = tuple.getComponent(0);
            Object idx = Value.unvalueAndCast(tuple.getComponent(1),
                                              Integer.TYPE);
            Object value = Value.unvalueAndCast(tuple.getComponent(2),
                                                arr.getClass().getComponentType());

            Array.set(arr, ((Integer) idx).intValue(), value);

            return value;
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