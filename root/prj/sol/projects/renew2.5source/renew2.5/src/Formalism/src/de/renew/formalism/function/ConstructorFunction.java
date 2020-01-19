package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import de.renew.util.ReflectionSerializer;

import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class ConstructorFunction implements Function {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConstructorFunction.class);

    /**
     * This field is not really transient, but as the reflection
     * classes are not serializable, we have to store it by
     * ourselves.
     **/
    transient Constructor<?> constr;

    public ConstructorFunction(Constructor<?> constructor) {
        constr = constructor;
    }

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;

        Object[] paramArr = new Object[tuple.getArity()];
        for (int i = 0; i < paramArr.length; i++) {
            paramArr[i] = tuple.getComponent(i);
        }

        try {
            return Executor.executeConstructor(constr, paramArr);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e)
                                                .getTargetException();
                if (logger.isDebugEnabled()) {
                    logger.debug("Constructor call resulted in an exception:"
                                 + targetException + " while executing "
                                 + this.toString(), targetException);
                }
                throw new Impossible("Constructor call resulted in an exception: "
                                     + targetException, targetException);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception occured during constructor call: "
                                 + e + " while executing " + this.toString(), e);
                }
                throw new Impossible("Exception occured during constructor call: "
                                     + e, e);
            }
        } catch (LinkageError e) {
            logger.warn("LinkageError occured during constructor call: " + e
                        + " while executing " + this.toString(), e);
            throw new Impossible("LinkageError occured during constructor call: "
                                 + e, e);
        }
    }

    /**
     * Serialization method, behaves like default writeObject
     * method. Stores the not-really-transient constr field.
     **/
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        ReflectionSerializer.writeConstructor(out, constr);
    }

    /**
     * Deserialization method, behaves like default readObject
     * method. Restores the not-really-transient constr field.
     **/
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        constr = ReflectionSerializer.readConstructor(in);
    }

    public final String toString() {
        return "ConstrFunc: " + constr.toString();
    }
}