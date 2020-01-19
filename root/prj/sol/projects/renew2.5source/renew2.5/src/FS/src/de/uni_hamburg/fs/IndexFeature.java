package de.uni_hamburg.fs;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


class IndexFeature extends JavaFeature {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(IndexFeature.class);
    private Method getCount;
    private Method get;
    private Method add;
    private Class<?> returnType;

    IndexFeature(Method getCount, Method get, Method add) {
        this.getCount = getCount;
        this.get = get;
        this.add = add;
        returnType = Array.newInstance(get.getReturnType(), 0).getClass();
    }

    Class<?> getJavaClass() {
        return returnType;
    }

    Object getObjectValue(Object javaObject) {
        Throwable e = null;
        try {
            int count = ((Integer) getCount.invoke(javaObject,
                                                   JavaConcept.NOPARAM))
                            .intValue();
            Object[] result = (Object[]) Array.newInstance(get.getReturnType(),
                                                           count);
            for (int i = 0; i < count; ++i) {
                result[i] = get.invoke(javaObject,
                                       new Object[] { new Integer(i) });
            }
            return result;
        } catch (IllegalAccessException e1) {
            e = e1;
            // should not happen
        } catch (IllegalArgumentException e2) {
            e = e2;
            // should not happen
        } catch (InvocationTargetException e3) {
            e = e3.getTargetException();
        }
        logger.error("Could not get index values of Object " + javaObject + ":");
        logger.error(e.getMessage(), e);
        //throw new RuntimeException("Exception during feature extraction:");
        return null;
    }

    boolean canSet() {
        return add != null;
    }

    void setObjectValue(Object javaObject, Object value) {
        if (add == null) {
            // no add method
            String msg = "Cannot set attribute of objects of "
                         + javaObject.getClass().getName()
                         + ": No add method for\n" + get;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        Throwable e = null;
        try {
            Object[] valarray = (Object[]) value;
            for (int i = 0; i < valarray.length; ++i) {
                add.invoke(javaObject, new Object[] { valarray[i] });
            }
            return;
        } catch (IllegalAccessException e1) {
            e = e1;
            // should not happen
        } catch (IllegalArgumentException e2) {
            e = e2;
            // should not happen
        } catch (InvocationTargetException e3) {
            e = e3.getTargetException();
        } catch (Throwable t) {
            e = t;
        }
        logger.error(e.getMessage(), e);
        throw new RuntimeException("Exception during feature setting:");
    }
}