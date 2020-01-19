package de.renew.formalism.function;

import de.renew.expression.NoArgFunction;

import de.renew.unify.Impossible;

import de.renew.util.ReflectionSerializer;
import de.renew.util.Value;

import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class StaticFieldFunction implements NoArgFunction {

    /**
     * This field is not really transient, but as the reflection
     * classes are not serializable, we have to store it by
     * ourselves.
     **/
    transient private Field field;

    public StaticFieldFunction(Field field) {
        this.field = field;
    }

    public Object function() throws Impossible {
        try {
            return Value.possiblyWrap(field.get(null),
                                      field.getType().isPrimitive());
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                throw new Impossible("Static field access impossible: non-static field ("
                                     + field + ")", e);
            } else if (e instanceof InvocationTargetException) {
                Throwable te = ((InvocationTargetException) e)
                                   .getTargetException();
                throw new Impossible("Static field access resulted in exception ("
                                     + field + "): " + te, te);
            } else {
                throw new Impossible("Exception occured during static field access ("
                                     + field + "): " + e, e);
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

    public String toString() {
        return "StaticFieldFunc(" + field + ")";
    }
}