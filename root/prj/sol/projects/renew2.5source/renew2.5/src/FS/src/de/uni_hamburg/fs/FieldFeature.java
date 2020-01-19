package de.uni_hamburg.fs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


class FieldFeature extends JavaFeature {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FieldFeature.class);
    private Field field;

    FieldFeature(Field field) {
        this.field = field;
    }

    Class<?> getJavaClass() {
        return field.getType();
    }

    Object getObjectValue(Object javaObject) {
        try {
            return field.get(javaObject);
        } catch (IllegalAccessException e) {
            // should not happen
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Exception during feature setting:");
        }
    }

    boolean canSet() {
        return !Modifier.isFinal(field.getModifiers());
    }

    void setObjectValue(Object javaObject, Object value) {
        try {
            field.set(javaObject, value);
        } catch (IllegalAccessException e) {
            // should not happen
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Exception during feature setting:");
        }
    }
}