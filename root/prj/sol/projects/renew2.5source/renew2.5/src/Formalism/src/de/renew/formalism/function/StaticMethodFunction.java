package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StaticMethodFunction implements Function {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(StaticMethodFunction.class);

    /**
     * This field is not really transient, but as the reflection
     * classes are not serializable, we have to store it by
     * ourselves.
     **/
    transient Method method;

    public StaticMethodFunction(Method method) {
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            throw new RuntimeException("Method expected to be static.");
        }
        this.method = method;
    }

    public Object function(Object param) throws Impossible {
        Tuple args = (Tuple) param;

        Object[] paramArr = new Object[args.getArity()];
        for (int i = 0; i < paramArr.length; i++) {
            paramArr[i] = args.getComponent(i);
        }

        try {
            return Executor.executeMethod(method, null, paramArr);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e)
                                                .getTargetException();
                if (logger.isDebugEnabled()) {
                    logger.error(targetException.getMessage(), targetException);
                    logger.error("while executing " + method + ".");
                }
                throw new Impossible("Method call resulted in an exception: "
                                     + targetException, targetException);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.error(e.getMessage(), e);
                    logger.error("while executing " + method + ".");
                }
                throw new Impossible("Exception occured during method call: "
                                     + e, e);
            }
        }
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
}