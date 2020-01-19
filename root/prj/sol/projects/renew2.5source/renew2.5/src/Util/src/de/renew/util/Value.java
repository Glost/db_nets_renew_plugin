package de.renew.util;

import java.io.Serializable;


// I wrap an object corresponding to a primitive value
// into a special layer so that primitive values and
// the corresponding objects are not confused inside
// a place instance.
public final class Value implements TextToken, Serializable {
    public final Object value;

    public Value(Object value) {
        if (value instanceof Byte || value instanceof Short
                    || value instanceof Integer || value instanceof Long
                    || value instanceof Float || value instanceof Double
                    || value instanceof Character || value instanceof Boolean) {
            this.value = value;
        } else {
            if (value instanceof Value) {
                throw new RuntimeException("Tried to make a nested value. Strange.");
            } else if (value instanceof Throwable) {
                throw new RuntimeException("Tried to make a Throwable a value. Strange. "
                                           + "Throwable is: " + value,
                                           (Throwable) value);
            } else {
                throw new RuntimeException("Tried to make object " + value
                                           + " a value. Strange.");
            }
        }
    }

    public int hashCode() {
        // Add a constant to distinguish a value from its
        // wrapped object.
        return value.hashCode() + 517293561;
    }

    public boolean equals(Object that) {
        if (that instanceof Value) {
            return value.equals(((Value) that).value);
        } else {
            return false;
        }
    }

    public boolean booleanValue() {
        return ((Boolean) value).booleanValue();
    }

    public char charValue() {
        return ((Character) value).charValue();
    }

    public byte byteValue() {
        return ((Number) value).byteValue();
    }

    public short shortValue() {
        return ((Number) value).shortValue();
    }

    public int intValue() {
        return ((Number) value).intValue();
    }

    public long longValue() {
        return ((Number) value).longValue();
    }

    public float floatValue() {
        return ((Number) value).floatValue();
    }

    public double doubleValue() {
        return ((Number) value).doubleValue();
    }

    public String toTokenText() {
        return value.toString();
    }

    public String toString() {
        return de.renew.util.Types.typify(value.getClass()) + "("
               + value.toString() + ")";
    }

    /**
     * Wrap an object in a value, if neccessary.
     */
    static public Object possiblyWrap(Object obj, boolean wrap) {
        if (wrap) {
            obj = new Value(obj);
        }
        return obj;
    }

    /**
     * Convert primitive values represented by objects, allowing for
     * widening conversion.
     *
     * @param value  the object to convert into a primitive value
     *               wrapper object
     * @param clazz  the expected primitive type (should be one of
     *               {@link Character.TYPE},
     *               {@link Double.TYPE},
     *               {@link Float.TYPE},
     *               {@link Long.TYPE},
     *               {@link Integer.TYPE},
     *               {@link Short.TYPE}, or
     *               {@link Byte.TYPE}).
     * @return the converted value as object according to the
     *         expected type.
     * @throws IllegalArgumentException
     *     if the value cannot be converted losslessly into the
     *     expected primitive type
     */
    static public Object convertPrimitive(Object value, Class<?> clazz) {
        // Try to return characters, if possible.
        if (value instanceof Character) {
            if (clazz == Character.TYPE) {
                return value;
            } else {
                // No, cast the character to an integer and
                // process it later.
                value = new Integer(((Character) value).charValue());
            }
        }

        if (value instanceof Number) {
            Number number = (Number) value;
            if (clazz == Double.TYPE) {
                return new Double(number.doubleValue());
            }
            if (number instanceof Double) {
                throw new IllegalArgumentException();
            }
            if (clazz == Float.TYPE) {
                return new Float(number.floatValue());
            }
            if (number instanceof Float) {
                throw new IllegalArgumentException();
            }
            if (clazz == Long.TYPE) {
                return new Long(number.longValue());
            }
            if (number instanceof Long) {
                throw new IllegalArgumentException();
            }
            if (clazz == Integer.TYPE) {
                return new Integer(number.intValue());
            }
            if (number instanceof Integer) {
                throw new IllegalArgumentException();
            }
            if (clazz == Short.TYPE) {
                return new Short(number.shortValue());
            }
            if (number instanceof Short) {
                throw new IllegalArgumentException();
            }
            if (clazz == Byte.TYPE) {
                return new Byte(number.byteValue());
            }
            throw new IllegalArgumentException();
        } else if (value instanceof Boolean) {
            if (clazz == Boolean.TYPE) {
                return value;
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new RuntimeException("Encountered a bad value.");
        }
    }

    /**
     * Remove the valueness of this object and convert it
     * to the desired class, if that is possible.
     */
    public Object unvalueAndCast(Class<?> clazz)
            throws IllegalArgumentException {
        if (clazz.isPrimitive()) {
            return convertPrimitive(value, clazz);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Remove the valueness of the argument and convert it
     * to the desired class, if that is possible.
     */
    static public Object unvalueAndCast(Object obj, Class<?> clazz)
            throws IllegalArgumentException {
        if (obj instanceof Value) {
            return ((Value) obj).unvalueAndCast(clazz);
        } else {
            if (obj == null || clazz.isInstance(obj)) {
                return obj;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Unvalue and cast a complete array.
     */
    static public Object[] unvalueAndCast(Object[] objs, Class<?>[] clazzes)
            throws IllegalArgumentException {
        Object[] results = new Object[objs.length];
        for (int i = 0; i < objs.length; i++) {
            results[i] = unvalueAndCast(objs[i], clazzes[i]);
        }
        return results;
    }

    /**
     * A unique return value for {@link #castOrReturnImpossible(Class, Object)}.
     */
    static public final Object IMPOSSIBLE_CAST = new Object();

    /**
     * Cast arg to class, possibly wrapping or unwrapping values.
     *
     * @param clazz the target class
     * @param arg the argument value
     * @return the casted value or {@link #IMPOSSIBLE_CAST} when the cast is forbidden
     */
    static public Object castOrReturnImpossible(Class<?> clazz, Object arg) {
        if (clazz.isPrimitive()) {
            if (arg instanceof Value) {
                Object value = ((Value) arg).value;
                if (value instanceof Boolean) {
                    if (clazz == Boolean.TYPE) {
                        return arg;
                    }
                } else {
                    Number number;
                    if (value instanceof Character) {
                        char charVal = ((Character) value)
                                           .charValue();
                        number = new Integer(charVal);
                    } else {
                        number = (Number) value;
                    }
                    if (clazz == Character.TYPE) {
                        return new Value(new Character((char) number.intValue()));
                    } else if (clazz == Byte.TYPE) {
                        return new Value(new Byte(number.byteValue()));
                    } else if (clazz == Short.TYPE) {
                        return new Value(new Short(number.shortValue()));
                    } else if (clazz == Integer.TYPE) {
                        return new Value(new Integer(number.intValue()));
                    } else if (clazz == Long.TYPE) {
                        return new Value(new Long(number.longValue()));
                    } else if (clazz == Float.TYPE) {
                        return new Value(new Float(number.floatValue()));
                    } else if (clazz == Double.TYPE) {
                        return new Value(new Double(number.doubleValue()));
                    }
                }
            }
        } else {
            if (arg == null || clazz.isInstance(arg)) {
                return arg;
            }
        }
        return IMPOSSIBLE_CAST;
    }
}