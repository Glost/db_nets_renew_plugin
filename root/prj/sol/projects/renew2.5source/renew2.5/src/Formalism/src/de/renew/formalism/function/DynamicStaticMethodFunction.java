package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;


public class DynamicStaticMethodFunction implements Function {
    String method;

    /**
     * This field is not really transient, but as <code>java.lang.Class
     * </code>is not always serializable, we have to store it by
     * ourselves.
     **/
    transient Class<?> clazz;

    public DynamicStaticMethodFunction(String method, Class<?> clazz) {
        this.method = method;
        this.clazz = clazz;
    }

    public Object function(Object param) throws Impossible {
        Tuple args = (Tuple) param;

        Object[] paramArr = new Object[args.getArity()];
        for (int i = 0; i < paramArr.length; i++) {
            paramArr[i] = args.getComponent(i);
        }

        try {
            return Executor.executeMethod(clazz, null, method, paramArr);
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