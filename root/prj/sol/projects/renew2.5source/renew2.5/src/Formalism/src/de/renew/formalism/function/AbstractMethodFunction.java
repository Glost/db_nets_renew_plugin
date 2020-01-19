package de.renew.formalism.function;

import de.renew.expression.Function;

import de.renew.unify.Impossible;
import de.renew.unify.Tuple;

import java.lang.reflect.InvocationTargetException;


public abstract class AbstractMethodFunction implements Function {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(AbstractMethodFunction.class);

    public abstract Object doFunction(Object obj, Object[] paramArr)
            throws Exception;

    public Object function(Object param) throws Impossible {
        Tuple tuple = (Tuple) param;
        if (tuple.getArity() != 2) {
            throw new Impossible();
        }
        Object obj = tuple.getComponent(0);
        Tuple args = (Tuple) tuple.getComponent(1);

        Object[] paramArr = new Object[args.getArity()];
        for (int i = 0; i < paramArr.length; i++) {
            paramArr[i] = args.getComponent(i);
        }

        try {
            return doFunction(obj, paramArr);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e)
                                                .getTargetException();
                if (logger.isDebugEnabled()) {
                    logger.debug("Method call resulted in an exception: "
                                 + targetException + " while executing "
                                 + this.toString() + " on object " + obj
                                 + " with parameters "
                                 + makeParamList(paramArr), targetException);
                }
                throw new Impossible("Method call resulted in an exception: "
                                     + targetException, targetException);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception occured during method call: " + e
                                 + " while executing " + this.toString()
                                 + " on object " + obj + " with parameters "
                                 + makeParamList(paramArr), e);
                }
                throw new Impossible("Exception occured during method call: "
                                     + e, e);
            }
        } catch (LinkageError e) {
            logger.warn("LinkageError occured during method call: " + e
                        + " while executing " + this.toString(), e);
            throw new Impossible("LinkageError occured during method call: "
                                 + e, e);
        }
    }

    private String makeParamList(Object[] paramArr) {
        if (paramArr == null) {
            return "<null>";
        }

        StringBuffer result = new StringBuffer();
        result.append("<");
        result.append(paramArr.length);
        result.append(">(");
        for (int i = 0; i < paramArr.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(paramArr[0]);
        }
        result.append(")");
        return result.toString();
    }
}