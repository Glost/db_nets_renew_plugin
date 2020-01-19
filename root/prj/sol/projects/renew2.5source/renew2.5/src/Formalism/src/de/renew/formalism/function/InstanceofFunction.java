package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Calculator;
import de.renew.unify.Impossible;

import de.renew.util.ReflectionSerializer;
import de.renew.util.Types;
import de.renew.util.Value;

import java.io.IOException;


public class InstanceofFunction implements Function {

    /**
     * This field is not really transient, but as <code>java.lang.Class
     * </code>is not always serializable, we have to store it by
     * ourselves.
     **/
    transient private Class<?> clazz;
    private boolean allowsNull;
    private boolean allowsCalculator;

    public InstanceofFunction(Class<?> clazz, boolean allowsNull,
                              boolean allowsCalculator) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        this.clazz = clazz;
        this.allowsNull = allowsNull;
        this.allowsCalculator = allowsCalculator;
    }

    public Object function(Object param) throws Impossible {
        boolean result;
        if (param instanceof Calculator) {
            if (allowsCalculator) {
                Class<?> paramType = ((Calculator) param)
                                         .getType();
                if (clazz.isPrimitive()) {
                    result = (clazz == paramType);
                } else {
                    result = (clazz == paramType)
                             || Types.allowsReferenceWidening(paramType, clazz);
                }
            } else {
                result = false;
            }
        } else if (param instanceof Value) {
            if (clazz.isPrimitive()) {
                result = Types.objectify(clazz).isInstance(((Value) param).value);
            } else {
                result = false;
            }
        } else if (param == null) {
            result = allowsNull;
        } else {
            result = clazz.isInstance(param);
        }
        return new Value(new Boolean(result));
    }

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient clazz field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeClass(out, clazz);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient clazz field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        clazz = ReflectionSerializer.readClass(in);
    }
}