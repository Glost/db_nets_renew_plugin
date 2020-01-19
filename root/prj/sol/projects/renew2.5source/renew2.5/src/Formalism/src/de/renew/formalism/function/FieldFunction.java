package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;

import de.renew.util.ReflectionSerializer;
import de.renew.util.Value;

import java.io.IOException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class FieldFunction implements Function {

    /**
     * This field is not really transient, but as the reflection
     * classes are not serializable, we have to store it by
     * ourselves.
     **/
    transient Field field;

    public FieldFunction(Field field) {
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

            return Value.possiblyWrap(field.get(param),
                                      field.getType().isPrimitive());
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

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient field field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeField(out, field);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient field field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        field = ReflectionSerializer.readField(in);
    }
}