package de.uni_hamburg.fs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


class BeanFeature extends JavaFeature {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BeanFeature.class);
    private Method get;
    private Method set;

    BeanFeature(Method get, Method set) {
        this.get = get;
        this.set = set;
    }

    Class<?> getJavaClass() {
        return get.getReturnType();
    }

    Object getObjectValue(Object javaObject) {
        Throwable e = null;
        try {
            return get.invoke(javaObject, JavaConcept.NOPARAM);
        } catch (IllegalAccessException e1) {
            e = e1;
            // should not happen
        } catch (IllegalArgumentException e2) {
            e = e2;
            // should not happen
        } catch (InvocationTargetException e3) {
            e = e3.getTargetException();
        }


        //throw new RuntimeException("Exception during feature extraction:");
        //System.out.pprintln("Exception during feature extraction:\n"+e);
        return e; //null;
    }

    boolean canSet() {
        return set != null;
    }

    void setObjectValue(Object javaObject, Object value) {
        if (set == null) {
            // no set method, just check existing value:
            //if (!Null.equals(getObjectValue(javaObject),value))
            String msg = "Cannot set attribute of objects of "
                         + javaObject.getClass().getName()
                         + ": No set method for\n" + get;
            logger.error(msg);
            throw new RuntimeException(msg);
            //return;
        }
        try {
            set.invoke(javaObject, new Object[] { value });
            return;
        } catch (IllegalAccessException e1) {
            // should not happen
        } catch (IllegalArgumentException e2) {
            // should not happen
        } catch (InvocationTargetException e3) {
        }

        //System.out.pprintln("Exception during feature setting:\n"+e);
        return;


        //throw new RuntimeException("Exception during feature setting:");
    }
}