package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;

import de.renew.util.ReflectionSerializer;
import de.renew.util.Value;

import java.io.IOException;


/**
 * A cast function will attempt a cast whenever possible.
 * If primitive values are casted, this might result in loss of
 * precision.
 */
public class CastFunction implements Function {

    /**
     * This field is not really transient, but as <code>java.lang.Class
     * </code>is not always serializable, we have to store it by
     * ourselves.
     **/
    transient Class<?> clazz;

    public CastFunction(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object function(Object arg) throws Impossible {
        Object result = Value.castOrReturnImpossible(clazz, arg);
        if (result == Value.IMPOSSIBLE_CAST) {
            throw new Impossible();
        }
        return result;
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