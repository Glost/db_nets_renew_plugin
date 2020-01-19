package de.renew.formalism.function;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;

import java.lang.reflect.Method;


public class MethodFunction extends AbstractMethodFunction {

    /**
     * This field is not really transient, but as the reflection
     * classes are not serializable, we have to store it by
     * ourselves.
     **/
    transient Method method;

    public MethodFunction(Method method) {
        this.method = method;
    }

    public Object doFunction(Object obj, Object[] paramArr)
            throws Exception {
        return Executor.executeMethod(method, obj, paramArr);
    }

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient method field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeMethod(out, method);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient method field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        method = ReflectionSerializer.readMethod(in);
    }

    public String toString() {
        return "MethodFunc: " + method.toString();
    }
}