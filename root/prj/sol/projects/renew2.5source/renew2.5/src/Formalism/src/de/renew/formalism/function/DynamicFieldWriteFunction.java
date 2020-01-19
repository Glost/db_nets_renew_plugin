package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.Value;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class DynamicFieldWriteFunction implements Function {
    String field;

    public DynamicFieldWriteFunction(String field) {
        this.field = field;
    }

    public Object function(Object param) throws Impossible {
        try {
            // param must be a tuple consisting of the object
            // and the new value.
            Tuple tuple = (Tuple) param;
            Object obj = tuple.getComponent(0);
            Object val = tuple.getComponent(1);


            // Unlike in FieldFunction we need not deal with the special
            // field length for arrays, which cannot be assigned.
            Field theField = obj.getClass().getField(field);


            // Possibly unwrap, then assign.
            theField.set(obj, Value.unvalueAndCast(val, theField.getType()));

            // Return the assigned value.
            return val;
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