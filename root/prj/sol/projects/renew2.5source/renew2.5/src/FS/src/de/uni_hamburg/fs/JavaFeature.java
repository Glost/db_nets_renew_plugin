package de.uni_hamburg.fs;

import de.renew.util.Value;


abstract class JavaFeature {
    abstract Class<?> getJavaClass();

    Object getValue(Object javaObject) {
        Object value = getObjectValue(javaObject);
        if (getJavaClass().isPrimitive() && value != null
                    && !(value instanceof Exception)) {
            value = new Value(value); // wrap as Value object
        }
        return value;
    }

    abstract Object getObjectValue(Object javaObject);

    abstract boolean canSet();

    void setValue(Object javaObject, Object value) {
        if (getJavaClass().isPrimitive()) {
            if (value == null) {
                return; // noramlly, primiteves can't be null, but it may
            }


            // happen when a get method failed.
            value = ((Value) value).value;
        }
        setObjectValue(javaObject, value);
    }

    abstract void setObjectValue(Object javaObject, Object value);
}