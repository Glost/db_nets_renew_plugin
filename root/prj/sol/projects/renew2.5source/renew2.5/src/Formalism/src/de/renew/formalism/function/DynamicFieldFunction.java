package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;

import de.renew.util.Value;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class DynamicFieldFunction implements Function {
    String field;

    public DynamicFieldFunction(String field) {
        this.field = field;
    }

    public Object function(Object param) throws Impossible {
        try {
            if (param.getClass().isArray()) {
                if (field.equals("length")) {
                    return new Value(new Integer(Array.getLength(param)));
                } else {
                    throw new Impossible();
                }
            }

            Field theField = param.getClass().getField(field);

            return Value.possiblyWrap(theField.get(param),
                                      theField.getType().isPrimitive());
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable te = ((InvocationTargetException) e)
                                   .getTargetException();
                throw new Impossible("Dynamic field access resulted in exception ("
                                     + field + " on object " + param + "): "
                                     + te, te);
            } else {
                throw new Impossible("Exception occured during dynamic field access ("
                                     + field + " on object " + param + "): "
                                     + e, e);
            }
        }
    }

    public String toString() {
        return "DynFieldFunc(" + field + ")";
    }
}